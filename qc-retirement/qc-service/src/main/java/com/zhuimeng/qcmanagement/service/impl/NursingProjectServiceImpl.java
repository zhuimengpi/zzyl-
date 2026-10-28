package com.zhuimeng.qcmanagement.service.impl;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.NursingProjectDTO;
import com.zhuimeng.entity.NursingProject;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.NursingProjectMapper;
import com.zhuimeng.qcmanagement.service.NursingProjectService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class NursingProjectServiceImpl implements NursingProjectService {
    @Autowired
    private NursingProjectMapper nursingProjectMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    //护理项目key
    private static final String key = RedisKeyProperties.NURSING_PROJECTS;

    private static final Object projectListLock = new Object(); // 独立的锁对象

    /** 获取所有项目
     * */
    @Override
    public Result listAll() {
        // 1.查询Redis
        List<NursingProject> nursingProjectList = (List<NursingProject>) redisTemplate.opsForValue().get(key);

        // 2.判断Redis中是否存在
        if (nursingProjectList != null && !nursingProjectList.isEmpty()) {
            //缓存命中
            return Result.ok(nursingProjectList);
        }

        // 本地锁
        synchronized(projectListLock){
            // 3.双重检查（减少访问DB）
            nursingProjectList = (List<NursingProject>) redisTemplate.opsForValue().get(key);
            if (nursingProjectList != null && nursingProjectList.size() > 0) {
                //缓存命中
                return Result.ok(nursingProjectList);
            }

            // 4.查询数据库
            nursingProjectList = nursingProjectMapper.listAll();

            // 5.存入Redis
            redisTemplate.opsForValue().set(key, nursingProjectList, 1, TimeUnit.HOURS);
        }

        return Result.ok(nursingProjectList);
    }

    /** 项目分页查询（Redis缓存全量，内存过滤+分页）
     * */
    @Override
    public Result listByPage(Integer pageNum, Integer pageSize, String name, Integer status) {
        // 获取全量项目列表（优先Redis）
        List<NursingProject> allList = getAllFromCache();

        // 内存过滤
        List<NursingProject> filteredList = allList.stream()
                .filter(item -> name == null || name.isEmpty() || (item.getName() != null && item.getName().contains(name)))
                .filter(item -> status == null || (item.getStatus() != null && item.getStatus().equals(status)))
                .collect(Collectors.toList());

        // 内存分页
        int total = filteredList.size();
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<NursingProject> pageRecords = (fromIndex < total)
                ? filteredList.subList(fromIndex, toIndex)
                : new ArrayList<>();

        Map<String, Object> data = new HashMap<>();
        data.put("records", pageRecords);
        data.put("pages", pages);
        data.put("total", total);

        return Result.ok(data);
    }

    /** 从Redis获取全量护理项目列表，缓存未命中则查DB
     * */
    private List<NursingProject> getAllFromCache() {
        // 1.查询Redis
        List<NursingProject> list = (List<NursingProject>) redisTemplate.opsForValue().get(key);

        // 2.判断Redis是否存储
        if (list != null && !list.isEmpty()) {
            return list;
        }

        // 3.缓存未命中，直接复用 listAll 的逻辑（走双重检查+DB+缓存）
        Result result = listAll();
        return (List<NursingProject>) result.getData();
    }

    /** 项目详情
     * */
    @Override
    //护理项目ID
    public Result detail(Long id) {
        // 1.查询Redis中是否存在
        NursingProjectDTO nursingProjectDTO = new NursingProjectDTO();
        nursingProjectDTO.setId(id);
        NursingProject nursingProject = readNursingProjectRedis(nursingProjectDTO);

        // 2.判断Redis中是否存在
        if (nursingProject != null) {
            return Result.ok(nursingProject);
        }

        //本地锁
        synchronized (projectListLock){
            // 3.双重检查
            nursingProject = readNursingProjectRedis(nursingProjectDTO);
            if (nursingProject != null) {
                return Result.ok(nursingProject);
            }

            // 4.缓存未命中，查询DB
            nursingProject = nursingProjectMapper.selectById(id);

            // 5.判断DB是否存在
            if (nursingProject == null) {
                return Result.fail("护理项目不存在");
            }
        }

        return Result.ok(nursingProject);
    }

    /** 添加项目
     * */
    @Override
    public Result insert(NursingProjectDTO nursingProjectDTO) {
        // 1. 查询Redis中是否存在
        NursingProject nursingProject = readNursingProjectRedis(nursingProjectDTO);

        // 2.判断Redis中是否存在
        if (nursingProject != null) {
            return Result.fail("护理项目已存在");
        }

        // 3.缓存未命中，查询DB
        nursingProject = nursingProjectMapper.selectById(nursingProjectDTO.getId());

        // 4.判断DB是否存在
        if (nursingProject != null) {
            return Result.fail("护理项目已存在");
        }

        // 5.执行插入操作
        nursingProject = new NursingProject();
        BeanUtils.copyProperties(nursingProjectDTO, nursingProject);
        nursingProject.setStatus(1);
        try {
            nursingProjectMapper.insert(nursingProject);
        } catch (DuplicateKeyException e) {
            // 极少数并发情况下，另一个请求抢先插入了相同项目名
            return Result.fail("护理项目已被占用，请刷新后重试");
        }

        // 6.清除Redis
        redisTemplate.delete(key);

        return Result.ok("添加成功");
    }

    /** 编辑项目
     * */
    @Override
    public Result update(NursingProjectDTO nursingProjectDTO) {
        // 1. 查询Redis中是否存在
        NursingProject nursingProject = readNursingProjectRedis(nursingProjectDTO);

        // 2.判断Redis中是否存在
        if (nursingProject == null) {
            // 2.1缓存未命中，查询DB
            nursingProject = nursingProjectMapper.selectById(nursingProjectDTO.getId());

            // 2.2判断DB是否存在
            if (nursingProject == null) {
                return Result.fail("护理项目不存在");
            }
        }

        // 3.执行更新操作
        BeanUtils.copyProperties(nursingProjectDTO, nursingProject);
        nursingProject.setOldUpdateTime(nursingProject.getUpdateTime());
        nursingProjectMapper.updateById(nursingProject);

        // 4.清除Redis
        redisTemplate.delete(key);

        return Result.ok("更新成功");
    }

    /** 删除项目
     * */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    //护理项目ID
    public Result delete(Long id) {
        // 1.查询Redis中是否存在
        NursingProjectDTO nursingProjectDTO = new NursingProjectDTO();
        nursingProjectDTO.setId(id);
        NursingProject nursingProject = readNursingProjectRedis(nursingProjectDTO);

        // 2.判断Redis中是否存在
        if (nursingProject != null) {
            return Result.ok(nursingProject);
        }

        //本地锁
        synchronized (projectListLock){
            // 3.查询DB中是否存在
            nursingProject = nursingProjectMapper.selectById(id);

            // 4.判断DB是否存在
            if (nursingProject == null) {
                return Result.fail("护理项目不存在");
            }

            // 5.删除DB
            nursingProjectMapper.deleteById(id);
        }

        // 6.清除Redis
        transactionAfter();

        return Result.ok("删除成功");
    }

    /** 项目启用/禁用
     * */
    @Override
    //护理项目ID
    public Result updateStatus(Long id, Integer status) {
        // 1. 查询Redis中是否存在
        NursingProjectDTO nursingProjectDTO = new NursingProjectDTO();
        nursingProjectDTO.setId(id);
        NursingProject nursingProject = readNursingProjectRedis(nursingProjectDTO);

        // 2.判断Redis中是否存在
        if (nursingProject == null) {
            // 2.1缓存未命中，查询DB
            nursingProject = nursingProjectMapper.selectById(id);

            // 2.2判断DB是否存在
            if (nursingProject == null) {
                return Result.fail("护理项目不存在");
            }
        }

        // 3.执行更新操作
        nursingProject.setStatus(status);
        nursingProject.setOldUpdateTime(nursingProject.getUpdateTime());
        nursingProjectMapper.updateById(nursingProject);

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
     * Redis查询护理项目
     */
    private NursingProject readNursingProjectRedis(NursingProjectDTO nursingProjectDTO) {
        // 1.查询Redis
        List<NursingProject> nursingProjectList = (List<NursingProject>) redisTemplate.opsForValue().get(key);

        NursingProject nursingProject = new NursingProject();
        // 2.判断Redis是否存储
        if (nursingProjectList != null && !nursingProjectList.isEmpty()) {
            // 2.1获取护理项目
            nursingProject = nursingProjectList
                    .stream()
                    .filter(n -> n.getId().equals(nursingProjectDTO.getId()))
                    .findFirst()
                    .orElse(null);

            // 2.2判断ID对应护理项目是否存在
            if (nursingProject != null) {
                //存在
                return nursingProject;
            }else {
                //不存在
                return null;
            }
        }
        //不存在
        return null;
    }

}
