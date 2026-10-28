package com.zhuimeng.qcmanagement.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.DeptDTO;
import com.zhuimeng.entity.Dept;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.DeptMapper;
import com.zhuimeng.qcmanagement.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DeptServiceImpl implements DeptService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DeptMapper deptMapper;

    //部门key
    private static final String key = RedisKeyProperties.DEPT;

    private static final Object deptListLock = new Object(); // 独立的锁对象

    /** 部门列表
     * */
    @Override
    public Result list(DeptDTO deptDTO) {
        // 1.查询Redis
        List<Dept> deptList = (List<Dept>) redisTemplate.opsForValue().get(key);

        // 2.判断Redis是否有数据
        if (deptList != null) {
            return Result.ok(deptList);
        }

        //本地锁
        synchronized (deptListLock){
            // 3.双重查询
            deptList = (List<Dept>) redisTemplate.opsForValue().get(key);
            if (deptList != null) {
                return Result.ok(deptList);
            }

            // 4.查询部门列表
            deptList = deptMapper.list(deptDTO);

            // 5.存入Redis
            redisTemplate.opsForValue().set(key, deptList, 1, TimeUnit.HOURS);
        }

        return Result.ok(deptList);
    }

    /** 部门下拉树
     * */
    @Override
    public Result tree(String level) {
        // 1.解析层级深度
        int maxDepth = parseLevel(level);

        // 2.查询全量部门（仅启用状态）
        List<Dept> allDeptList = deptMapper.tree(level);

        // 3.构建子树（从 null 开始匹配所有 parent_dept_no 为空的顶级部门）
        List<Map<String, Object>> children = buildTree(allDeptList, null, 1, maxDepth);

        // 5.封装为前端期望格式: { items: [...] }
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("items", children);

        return Result.ok(resultData);
    }

    /** 解析前端传入的层级参数（支持 "3" 或 {"level":3} 两种格式）
     * */
    private int parseLevel(String level) {
        if (level == null || level.isEmpty()) {
            return 0; // 0 表示不限制深度
        }
        try {
            // 尝试直接解析为整数（如 "3"）
            return Integer.parseInt(level.trim());
        } catch (NumberFormatException e) {
            // 尝试解析 JSON 格式（如 {"level":3}）
            try {
                JSONObject json = JSONObject.parseObject(level);
                Integer lv = json.getInteger("level");
                return lv != null ? lv : 0;
            } catch (Exception ex) {
                return 0;
            }
        }
    }

    /** 递归构建部门树
     * */
    private List<Map<String, Object>> buildTree(
            List<Dept> allDepts,    // 全量部门列表
            String parentDeptNo,    // 当前父部门编号（null 表示根节点）
            int currentDepth,   // 当前递归深度
            int maxDepth    // 最大深度限制（0 表示不限制）
    ) {
        List<Map<String, Object>> nodes = new ArrayList<>();

        for (Dept dept : allDepts) {
            // 匹配父节点：根节点 parentDeptNo 为 null 或空字符串
            boolean isRoot = (parentDeptNo == null || parentDeptNo.isEmpty()) && (dept.getParentDeptNo() == null || dept.getParentDeptNo().isEmpty());
            boolean isChild = parentDeptNo != null && !parentDeptNo.isEmpty() && parentDeptNo.equals(dept.getParentDeptNo());

            if (!isRoot && !isChild) {
                continue;
            }

            Map<String, Object> node = new HashMap<>();
            node.put("id", dept.getDeptNo());
            node.put("label", dept.getDeptName());

            // 未达最大深度时递归构建子节点
            List<Map<String, Object>> children;
            if (maxDepth == 0 || currentDepth < maxDepth) {
                children = buildTree(allDepts, dept.getDeptNo(), currentDepth + 1, maxDepth);
            } else {
                children = new ArrayList<>();
            }
            node.put("children", children);

            nodes.add(node);
        }

        return nodes;
    }

    /** 新增部门
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result add(DeptDTO deptDTO) {
        Dept dept = new Dept();
        dept.setParentDeptNo(deptDTO.getParentDeptNo());
        dept.setDeptName(deptDTO.getDeptName());
        dept.setSortNo(deptDTO.getSortNo() != null ? deptDTO.getSortNo() : 0);
        dept.setDataState(deptDTO.getDataStatus() != null ? deptDTO.getDataStatus() : "0");
        dept.setLeaderId(deptDTO.getLeaderId());
        dept.setRemark(deptDTO.getRemark());
        dept.setDeptNo(generateDeptNo());

        try {
            deptMapper.insert(dept);
        } catch (DuplicateKeyException e) {
            return Result.fail("部门编号已存在");
        }

        transactionAfter();
        return Result.ok("新增成功");
    }

    /** 修改部门
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result update(DeptDTO deptDTO) {
        Dept existing = findDeptById(deptDTO.getId());
        if (existing == null) {
            return Result.fail("部门不存在");
        }

        if (deptDTO.getParentDeptNo() != null) existing.setParentDeptNo(deptDTO.getParentDeptNo());
        if (deptDTO.getDeptName() != null) existing.setDeptName(deptDTO.getDeptName());
        if (deptDTO.getSortNo() != null) existing.setSortNo(deptDTO.getSortNo());
        if (deptDTO.getDataStatus() != null) existing.setDataState(deptDTO.getDataStatus());
        if (deptDTO.getLeaderId() != null) existing.setLeaderId(deptDTO.getLeaderId());
        if (deptDTO.getRemark() != null) existing.setRemark(deptDTO.getRemark());

        existing.setOldUpdateTime(existing.getUpdateTime());
        deptMapper.updateById(existing);

        transactionAfter();
        return Result.ok("修改成功");
    }

    /** 启用/禁用部门
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result updateIsEnable(Long id, Integer status) {
        Dept existing = findDeptById(id);
        if (existing == null) {
            return Result.fail("部门不存在");
        }

        existing.setDataState(String.valueOf(status));
        existing.setOldUpdateTime(existing.getUpdateTime());
        deptMapper.updateById(existing);

        transactionAfter();
        return Result.ok(status == 1 ? "启用成功" : "禁用成功");
    }

    /** 删除部门
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result delete(Long id) {
        Dept existing = findDeptById(id);
        if (existing == null) {
            return Result.fail("部门不存在");
        }

        // 启用状态下不可删除
        if ("0".equals(existing.getDataState())) {
            return Result.fail("启用状态下不可删除");
        }

        // 检查是否有子部门
        List<Dept> children = deptMapper.selectByParentDeptNo(existing.getDeptNo());
        if (children != null && !children.isEmpty()) {
            return Result.fail("存在子部门，不可删除");
        }

        deptMapper.deleteById(id);
        transactionAfter();
        return Result.ok("删除成功");
    }

    // ==================== 私有辅助方法 ====================

    /** 根据ID查找部门（优先Redis缓存）
     * */
    private Dept findDeptById(Long id) {
        List<Dept> deptList = (List<Dept>) redisTemplate.opsForValue().get(key);
        if (deptList != null && !deptList.isEmpty()) {
            return deptList.stream()
                    .filter(d -> d.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }
        return deptMapper.selectById(id);
    }

    /** 生成部门编号
     * */
    private String generateDeptNo() {
        List<Dept> deptList = (List<Dept>) redisTemplate.opsForValue().get(key);
        if (deptList == null || deptList.isEmpty()) {
            deptList = deptMapper.list(null);
        }
        int maxNo = deptList.stream()
                .map(d -> {
                    try { return Integer.parseInt(d.getDeptNo()); }
                    catch (NumberFormatException e) { return 0; }
                })
                .max(Comparator.naturalOrder())
                .orElse(0);
        return String.valueOf(maxNo + 1);
    }

    /** 事务提交后清除缓存
     * */
    private void transactionAfter() {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        redisTemplate.delete(key);
                    }
                }
        );
    }
}
