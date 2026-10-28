package com.zhuimeng.qcmanagement.service.impl;

import com.zhuimeng.Result.Result;
import com.zhuimeng.Thread.UserHolder;
import com.zhuimeng.dto.NursingPlanDTO;
import com.zhuimeng.entity.NursingPlan;
import com.zhuimeng.entity.ProjectPlan;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.NursingPlanMapper;
import com.zhuimeng.qcmanagement.mapper.ProjectPlanMapper;
import com.zhuimeng.qcmanagement.service.NursingPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class NursingPlanServiceImpl implements NursingPlanService {
    @Autowired
    private NursingPlanMapper nursingPlanMapper;
    @Autowired
    private ProjectPlanMapper projectPlanMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    //护理计划key
    private static final String key = RedisKeyProperties.NURSING_PLAN;

    private static final Object planListLock = new Object(); // 独立的锁对象

    /** 获取所有计划
     * */
    @Override
    public Result list() {
        // 1.查询Redis
        List<NursingPlan> nursingPlanList = (List<NursingPlan>) redisTemplate.opsForValue().get(key);

        // 2.判断Redis中是否存在
        if (nursingPlanList != null && !nursingPlanList.isEmpty()) {
            //缓存命中
            return Result.ok(nursingPlanList);
        }

        // 本地锁
        synchronized(planListLock){
            // 3.双重检查（减少访问DB）
            nursingPlanList = (List<NursingPlan>) redisTemplate.opsForValue().get(key);
            if (nursingPlanList != null && nursingPlanList.size() > 0) {
                //缓存命中
                return Result.ok(nursingPlanList);
            }

            // 4.查询数据库
            nursingPlanList = nursingPlanMapper.listAll();

            // 4.1 查询所有项目计划关联，按计划ID分组设置
            List<ProjectPlan> allProjectPlans = projectPlanMapper.listAll();
            if (allProjectPlans != null && !allProjectPlans.isEmpty()) {
                Map<Long, List<ProjectPlan>> projectPlanMap = allProjectPlans.stream()
                        .collect(Collectors.groupingBy(ProjectPlan::getPlanId));
                for (NursingPlan plan : nursingPlanList) {
                    plan.setProjectPlans(projectPlanMap.getOrDefault(plan.getId(), new ArrayList<>()));
                }
            }

            // 5.存入Redis
            redisTemplate.opsForValue().set(key, nursingPlanList, 1, TimeUnit.HOURS);
        }

        return Result.ok(nursingPlanList);
    }

    /** 计划分页查询（Redis缓存全量，内存过滤+分页）
     * */
    @Override
    public Result page(Integer pageSize, Integer pageNum, String name, Integer status) {
        // 获取全量计划列表（优先Redis）
        List<NursingPlan> allList = getAllFromCache();

        // 内存过滤
        List<NursingPlan> filteredList = allList.stream()
                .filter(item -> name == null || name.isEmpty() || (item.getPlanName() != null && item.getPlanName().contains(name)))
                .filter(item -> status == null || (item.getStatus() != null && item.getStatus().equals(status)))
                .collect(Collectors.toList());

        // 内存分页
        int total = filteredList.size();
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<NursingPlan> pageRecords = (fromIndex < total)
                ? filteredList.subList(fromIndex, toIndex)
                : new ArrayList<>();

        Map<String, Object> data = new HashMap<>();
        data.put("records", pageRecords);
        data.put("pages", pages);
        data.put("total", total);

        return Result.ok(data);
    }

    /** 从Redis获取全量护理计划列表，缓存未命中则查DB
     * */
    private List<NursingPlan> getAllFromCache() {
        // 1.查询Redis
        List<NursingPlan> list = (List<NursingPlan>) redisTemplate.opsForValue().get(key);

        // 2.判断Redis是否存储
        if (list != null && !list.isEmpty()) {
            return list;
        }

        // 3.缓存未命中，直接复用 list 的逻辑（走双重检查+DB+缓存）
        Result result = list();
        return (List<NursingPlan>) result.getData();
    }

    /** 计划详情
     * */
    @Override
    //护理计划ID
    public Result detail(Long id) {
        // 1.查询Redis中是否存在（缓存中已包含projectPlans）
        NursingPlan nursingPlan = readNursingPlanRedis(id);

        // 2.判断Redis中是否存在
        if (nursingPlan != null) {
            return Result.ok(nursingPlan);
        }

        //本地锁
        synchronized (planListLock){
            // 3.双重检查
            nursingPlan = readNursingPlanRedis(id);
            if (nursingPlan != null) {
                return Result.ok(nursingPlan);
            }

            // 4.缓存未命中，查询DB
            nursingPlan = nursingPlanMapper.selectById(id);

            // 5.判断DB是否存在
            if (nursingPlan == null) {
                return Result.fail("护理计划不存在");
            }

            // 6.缓存未命中时，单独查询该项目计划列表
            List<ProjectPlan> projectPlans = projectPlanMapper.listByPlanId(id);
            nursingPlan.setProjectPlans(projectPlans);
        }

        return Result.ok(nursingPlan);
    }

    /** 添加计划
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result add(NursingPlanDTO nursingPlanDTO) {
        // 1. 查重 — 从Redis查找同名计划
        NursingPlan existing = readNursingPlanRedisByName(nursingPlanDTO.getPlanName());

        // 2.判断Redis中是否存在
        if (existing != null) {
            return Result.fail("护理计划名称已存在");
        }

        // 3.缓存未命中，查询DB
        existing = nursingPlanMapper.selectByName(nursingPlanDTO.getPlanName());

        // 4.判断DB是否存在
        if (existing != null) {
            return Result.fail("护理计划名称已存在");
        }

        // 5.执行插入操作 — 护理计划
        NursingPlan nursingPlan = new NursingPlan();
        nursingPlan.setPlanName(nursingPlanDTO.getPlanName());
        nursingPlan.setSortNo(nursingPlanDTO.getSortNo());
        nursingPlan.setStatus(1);
        try {
            nursingPlanMapper.insert(nursingPlan);
        } catch (DuplicateKeyException e) {
            return Result.fail("护理计划名称已被占用，请刷新后重试");
        }

        // 6.获取返回的计划ID，写入关联的项目计划
        List<ProjectPlan> projectPlans = nursingPlanDTO.getProjectPlans();
        if (projectPlans != null && !projectPlans.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            Long userId = Long.valueOf(UserHolder.getId());
            for (ProjectPlan pp : projectPlans) {
                pp.setPlanId(nursingPlan.getId());
                pp.setCreateTime(now);
                pp.setCreateBy(userId);
            }
            projectPlanMapper.insertBatch(projectPlans);
        }

        // 7.清除Redis
        transactionAfter();

        return Result.ok("添加成功");
    }

    /** 编辑计划
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result edit(Long id, NursingPlanDTO nursingPlanDTO) {
        // 1. 查询Redis中是否存在
        NursingPlan nursingPlan = readNursingPlanRedis(id);

        // 2.判断Redis中是否存在
        if (nursingPlan == null) {
            // 2.1缓存未命中，查询DB
            nursingPlan = nursingPlanMapper.selectById(id);

            // 2.2判断DB是否存在
            if (nursingPlan == null) {
                return Result.fail("护理计划不存在");
            }
        }

        // 3.更新护理计划
        nursingPlan.setPlanName(nursingPlanDTO.getPlanName());
        nursingPlan.setSortNo(nursingPlanDTO.getSortNo());
        nursingPlan.setOldUpdateTime(nursingPlan.getUpdateTime());
        nursingPlanMapper.updateById(nursingPlan);

        // 4.删除旧的关联项目计划
        projectPlanMapper.deleteByPlanId(id);

        // 5.写入新的关联项目计划
        List<ProjectPlan> projectPlans = nursingPlanDTO.getProjectPlans();
        if (projectPlans != null && !projectPlans.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            Long userId = Long.valueOf(UserHolder.getId());
            for (ProjectPlan pp : projectPlans) {
                pp.setPlanId(id);
                pp.setCreateTime(now);
                pp.setCreateBy(userId);
            }
            projectPlanMapper.insertBatch(projectPlans);
        }

        // 6.清除Redis
        transactionAfter();

        return Result.ok("更新成功");
    }

    /** 删除计划
     * */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    //护理计划ID
    public Result delete(Long id) {
        // 1.查询Redis中是否存在
        NursingPlan nursingPlan = readNursingPlanRedis(id);

        // 2.判断Redis中是否存在
        if (nursingPlan == null) {
            //本地锁
            synchronized (planListLock){
                // 3.查询DB中是否存在
                nursingPlan = nursingPlanMapper.selectById(id);

                // 4.判断DB是否存在
                if (nursingPlan == null) {
                    return Result.fail("护理计划不存在");
                }
            }
        }

        // 5.先删除关联的项目计划，再删除计划
        projectPlanMapper.deleteByPlanId(id);
        nursingPlanMapper.deleteById(id);

        // 6.清除Redis（事务提交后）
        transactionAfter();

        return Result.ok("删除成功");
    }

    /** 计划启用/禁用
     * */
    @Override
    //护理计划ID
    public Result status(Long id, Integer status) {
        // 1. 查询Redis中是否存在
        NursingPlan nursingPlan = readNursingPlanRedis(id);

        // 2.判断Redis中是否存在
        if (nursingPlan == null) {
            // 2.1缓存未命中，查询DB
            nursingPlan = nursingPlanMapper.selectById(id);

            // 2.2判断DB是否存在
            if (nursingPlan == null) {
                return Result.fail("护理计划不存在");
            }
        }

        // 3.执行更新操作
        nursingPlan.setStatus(status);
        nursingPlan.setOldUpdateTime(nursingPlan.getUpdateTime());
        nursingPlanMapper.updateById(nursingPlan);

        // 4.清除Redis
        redisTemplate.delete(key);

        return Result.ok(status == 1 ? "启用成功" : "禁用成功");
    }

    /** 事务提交后删除缓存（确保只有成功才删除）
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

    /**
     * Redis查询护理计划（按ID）
     */
    private NursingPlan readNursingPlanRedis(Long id) {
        // 1.查询Redis
        List<NursingPlan> nursingPlanList = (List<NursingPlan>) redisTemplate.opsForValue().get(key);

        // 2.判断Redis是否存储
        if (nursingPlanList != null && !nursingPlanList.isEmpty()) {
            // 2.1获取护理计划
            return nursingPlanList
                    .stream()
                    .filter(n -> n.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }
        //不存在
        return null;
    }

    /**
     * Redis查询护理计划（按名称）
     */
    private NursingPlan readNursingPlanRedisByName(String planName) {
        // 1.查询Redis
        List<NursingPlan> nursingPlanList = (List<NursingPlan>) redisTemplate.opsForValue().get(key);

        // 2.判断Redis是否存储
        if (nursingPlanList != null && !nursingPlanList.isEmpty()) {
            // 2.1获取护理计划
            return nursingPlanList
                    .stream()
                    .filter(n -> planName.equals(n.getPlanName()))
                    .findFirst()
                    .orElse(null);
        }
        //不存在
        return null;
    }

}
