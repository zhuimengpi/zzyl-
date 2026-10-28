package com.zhuimeng.qcmanagement.service.impl;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.ResourceDTO;
import com.zhuimeng.entity.Resource;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.ResourceMapper;
import com.zhuimeng.qcmanagement.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
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
public class ResourceServiceImpl implements ResourceService {
    @Autowired
    private ResourceMapper resourceMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    private static final String RESOURCE_KEY = RedisKeyProperties.RESOURCE;
    private static final Object resourceListLock = new Object();

    /** 菜单列表（全量查询，内存过滤）
     * */
    @Override
    public Result list(ResourceDTO resourceDTO) {
        List<Resource> allList = getAllFromCache();

        List<Resource> filteredList = allList.stream()
                .filter(item -> {
                    if (resourceDTO.getDataState() == null || resourceDTO.getDataState().isEmpty())
                        return true;
                    return resourceDTO.getDataState().equals(item.getDataState());
                })
                .filter(item -> {
                    if (resourceDTO.getResourceName() == null || resourceDTO.getResourceName().isEmpty())
                        return true;
                    return item.getResourceName() != null
                            && item.getResourceName().contains(resourceDTO.getResourceName());
                })
                .filter(item -> {
                    if (resourceDTO.getParentResourceNo() == null || resourceDTO.getParentResourceNo().isEmpty())
                        return true;
                    return resourceDTO.getParentResourceNo().equals(item.getParentResourceNo());
                })
                .filter(item -> {
                    if (resourceDTO.getResourceType() == null || resourceDTO.getResourceType().isEmpty())
                        return true;
                    return resourceDTO.getResourceType().equals(item.getResourceType());
                })
                .collect(Collectors.toList());

        return Result.ok(filteredList);
    }

    /** 菜单树形列表
     * */
    @Override
    public Result tree(ResourceDTO resourceDTO) {
        int maxDepth = resourceDTO.getLevel() != null ? resourceDTO.getLevel() : 0;
        List<Resource> allList = getAllFromCache();

        // 构建树：从根节点 "100000000000000" 开始
        List<Map<String, Object>> children = buildTree(allList, "100000000000000", 1, maxDepth);

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("items", children);
        return Result.ok(resultData);
    }

    /** 新增菜单
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result add(ResourceDTO resourceDTO) {
        Resource resource = new Resource();
        resource.setParentResourceNo(resourceDTO.getParentResourceNo());
        resource.setResourceName(resourceDTO.getResourceName());
        resource.setResourceType(resourceDTO.getResourceType() != null ? resourceDTO.getResourceType() : "m");
        resource.setRequestPath(resourceDTO.getRequestPath());
        resource.setIcon(resourceDTO.getIcon());
        resource.setSortNo(resourceDTO.getSortNo() != null ? resourceDTO.getSortNo() : 0);
        resource.setDataState(resourceDTO.getDataState() != null ? resourceDTO.getDataState() : "0");
        resource.setResourceNo(generateResourceNo());

        try {
            resourceMapper.insert(resource);
        } catch (DuplicateKeyException e) {
            return Result.fail("资源编号已存在");
        }

        transactionAfter();
        return Result.ok("新增成功");
    }

    /** 修改菜单
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result update(ResourceDTO resourceDTO) {
        Resource existing = findResourceById(resourceDTO.getId());
        if (existing == null) {
            return Result.fail("菜单不存在");
        }

        if (resourceDTO.getParentResourceNo() != null) existing.setParentResourceNo(resourceDTO.getParentResourceNo());
        if (resourceDTO.getResourceName() != null) existing.setResourceName(resourceDTO.getResourceName());
        if (resourceDTO.getResourceType() != null) existing.setResourceType(resourceDTO.getResourceType());
        if (resourceDTO.getRequestPath() != null) existing.setRequestPath(resourceDTO.getRequestPath());
        if (resourceDTO.getIcon() != null) existing.setIcon(resourceDTO.getIcon());
        if (resourceDTO.getSortNo() != null) existing.setSortNo(resourceDTO.getSortNo());
        if (resourceDTO.getDataState() != null) existing.setDataState(resourceDTO.getDataState());

        existing.setOldUpdateTime(existing.getUpdateTime());
        resourceMapper.updateById(existing);

        transactionAfter();
        return Result.ok("修改成功");
    }

    /** 禁用/启用菜单
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result enable(ResourceDTO resourceDTO) {
        Resource existing = resourceMapper.selectByResourceNo(resourceDTO.getResourceNo());
        if (existing == null) {
            return Result.fail("菜单不存在");
        }

        existing.setDataState(resourceDTO.getDataState());
        existing.setOldUpdateTime(existing.getUpdateTime());
        resourceMapper.updateById(existing);

        transactionAfter();
        return Result.ok("1".equals(resourceDTO.getDataState()) ? "禁用成功" : "启用成功");
    }

    /** 删除菜单
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result delete(Long menuId) {
        Resource existing = findResourceById(menuId);
        if (existing == null) {
            return Result.fail("菜单不存在");
        }

        // 检查是否有子菜单
        List<Resource> children = resourceMapper.selectByParentResourceNo(existing.getResourceNo());
        if (children != null && !children.isEmpty()) {
            return Result.fail("存在子菜单，不可删除");
        }

        resourceMapper.deleteById(menuId);
        transactionAfter();
        return Result.ok("删除成功");
    }

    // ==================== 私有方法 ====================

    private List<Resource> getAllFromCache() {
        List<Resource> list = (List<Resource>) redisTemplate.opsForValue().get(RESOURCE_KEY);
        if (list != null && !list.isEmpty()) {
            return list;
        }
        synchronized (resourceListLock) {
            list = (List<Resource>) redisTemplate.opsForValue().get(RESOURCE_KEY);
            if (list != null && !list.isEmpty()) {
                return list;
            }
            list = resourceMapper.listAll();
            redisTemplate.opsForValue().set(RESOURCE_KEY, list, 1, TimeUnit.HOURS);
        }
        return list;
    }

    private Resource findResourceById(Long id) {
        List<Resource> allList = (List<Resource>) redisTemplate.opsForValue().get(RESOURCE_KEY);
        if (allList != null && !allList.isEmpty()) {
            return allList.stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }
        return resourceMapper.selectById(id);
    }

    /** 生成资源编号
     * */
    private String generateResourceNo() {
        List<Resource> allList = getAllFromCache();
        long maxNo = allList.stream()
                .map(r -> {
                    try { return Long.parseLong(r.getResourceNo()); }
                    catch (NumberFormatException e) { return 0L; }
                })
                .max(Comparator.naturalOrder())
                .orElse(100000000000000L);
        return String.valueOf(maxNo + 1);
    }

    /** 递归构建菜单树
     * */
    private List<Map<String, Object>> buildTree(List<Resource> allResources, String parentResourceNo,
                                                 int currentDepth, int maxDepth) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (Resource res : allResources) {
            if (parentResourceNo.equals(res.getParentResourceNo())) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", res.getResourceNo());
                node.put("label", res.getResourceName());

                List<Map<String, Object>> children;
                if (maxDepth == 0 || currentDepth < maxDepth) {
                    children = buildTree(allResources, res.getResourceNo(), currentDepth + 1, maxDepth);
                } else {
                    children = new ArrayList<>();
                }
                node.put("children", children);
                nodes.add(node);
            }
        }
        return nodes;
    }

    private void transactionAfter() {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        redisTemplate.delete(RESOURCE_KEY);
                    }
                }
        );
    }
}
