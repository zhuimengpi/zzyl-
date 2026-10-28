package com.zhuimeng.qcmanagement.service.impl;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.RoleDTO;
import com.zhuimeng.entity.Role;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.RoleMapper;
import com.zhuimeng.qcmanagement.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    private static final String ROLE_KEY = RedisKeyProperties.ROLE;
    private static final Object roleListLock = new Object();

    /** 角色分页列表（Redis缓存全量，内存过滤+分页）
     * */
    @Override
    public Result page(Integer pageNum, Integer pageSize, RoleDTO roleDTO) {
        List<Role> allList = getAllFromCache();

        // 内存过滤：按角色名称模糊搜索
        List<Role> filteredList = allList.stream()
                .filter(item -> {
                    if (roleDTO.getRoleName() == null || roleDTO.getRoleName().isEmpty()) {
                        return true;
                    }
                    return item.getRoleName() != null
                            && item.getRoleName().contains(roleDTO.getRoleName());
                })
                .collect(Collectors.toList());

        // 内存分页
        int total = filteredList.size();
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Role> pageRecords = (fromIndex < total)
                ? filteredList.subList(fromIndex, toIndex)
                : new ArrayList<>();

        Map<String, Object> data = new HashMap<>();
        data.put("records", pageRecords);
        data.put("pages", pages);
        data.put("total", total);

        return Result.ok(data);
    }

    /** 角色下拉列表（仅启用状态）
     * */
    @Override
    public Result initRoles() {
        List<Role> allList = getAllFromCache();
        List<Role> enabledList = allList.stream()
                .filter(item -> "0".equals(item.getDataState()))
                .collect(Collectors.toList());
        return Result.ok(enabledList);
    }

    /** 查询角色的菜单权限编号列表
     * */
    @Override
    public Result findCheckedResources(Long roleId) {
        List<Long> resourceNos = roleMapper.findResourceNosByRoleId(roleId);
        return Result.ok(resourceNos);
    }

    /** 新增角色
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result add(RoleDTO roleDTO) {
        Role role = new Role();
        role.setRoleName(roleDTO.getRoleName());
        role.setDataState(roleDTO.getDataState() != null ? roleDTO.getDataState() : "0");

        try {
            roleMapper.insert(role);
        } catch (DuplicateKeyException e) {
            return Result.fail("角色名称已存在");
        }

        transactionAfter();
        return Result.ok("新增成功");
    }

    /** 修改角色（编辑信息 / 分配菜单权限 / 启用禁用 统一入口）
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result update(RoleDTO roleDTO) {
        // 分配菜单权限
        if (roleDTO.getCheckedResourceNos() != null) {
            // 先清空旧权限，再批量插入新权限
            roleMapper.deleteResourcesByRoleId(roleDTO.getId());
            if (!roleDTO.getCheckedResourceNos().isEmpty()) {
                roleMapper.insertRoleResources(roleDTO.getId(), roleDTO.getCheckedResourceNos());
            }
            transactionAfter();
            return Result.ok("菜单权限分配成功");
        }

        // 编辑角色信息或启用禁用
        Role existing = findRoleById(roleDTO.getId());
        if (existing == null) {
            return Result.fail("角色不存在");
        }

        if (roleDTO.getRoleName() != null) {
            existing.setRoleName(roleDTO.getRoleName());
        }
        if (roleDTO.getDataState() != null) {
            existing.setDataState(roleDTO.getDataState());
        }
        existing.setOldUpdateTime(existing.getUpdateTime());
        roleMapper.updateById(existing);

        transactionAfter();
        return Result.ok("修改成功");
    }

    /** 删除角色（支持批量，逗号分隔）
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result delete(String roleIds) {
        List<Long> ids = Arrays.stream(roleIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            return Result.fail("角色ID不能为空");
        }

        // 检查是否有启用状态的角色
        List<Role> allList = getAllFromCache();
        boolean hasEnabled = allList.stream()
                .anyMatch(r -> ids.contains(r.getId()) && "0".equals(r.getDataState()));
        if (hasEnabled) {
            return Result.fail("启用状态下不可删除");
        }

        roleMapper.deleteByIds(ids);

        transactionAfter();
        return Result.ok("删除成功");
    }

    // ==================== 私有方法 ====================

    private List<Role> getAllFromCache() {
        List<Role> list = (List<Role>) redisTemplate.opsForValue().get(ROLE_KEY);
        if (list != null && !list.isEmpty()) {
            return list;
        }
        synchronized (roleListLock) {
            list = (List<Role>) redisTemplate.opsForValue().get(ROLE_KEY);
            if (list != null && !list.isEmpty()) {
                return list;
            }
            list = roleMapper.listAll();
            redisTemplate.opsForValue().set(ROLE_KEY, list, 1, TimeUnit.HOURS);
        }
        return list;
    }

    private Role findRoleById(Long id) {
        List<Role> allList = (List<Role>) redisTemplate.opsForValue().get(ROLE_KEY);
        if (allList != null && !allList.isEmpty()) {
            return allList.stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }
        return roleMapper.selectById(id);
    }

    private void transactionAfter() {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        redisTemplate.delete(ROLE_KEY);
                    }
                }
        );
    }
}
