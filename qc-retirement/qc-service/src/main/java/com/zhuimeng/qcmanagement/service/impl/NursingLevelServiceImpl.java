package com.zhuimeng.qcmanagement.service.impl;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.NursingLevelDTO;
import com.zhuimeng.entity.NursingLevel;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.NursingLevelMapper;
import com.zhuimeng.qcmanagement.service.NursingLevelService;
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
public class NursingLevelServiceImpl implements NursingLevelService {
    @Autowired
    private NursingLevelMapper nursingLevelMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    //护理等级key
    private static final String key = RedisKeyProperties.NURSING_LEVEL;

    private static final Object roomListLock = new Object(); // 独立的锁对象

    /** 获取所有等级
     * */
    @Override
    public Result listAll() {
        // 1.查询Redis
        List<NursingLevel> nursingLevelList = (List<NursingLevel>) redisTemplate.opsForValue().get(key);

        // 2.判断Redis中是否存在
        if (nursingLevelList != null && !nursingLevelList.isEmpty()) {
            //缓存命中
            return Result.ok(nursingLevelList);
        }

        // 本地锁
        synchronized(roomListLock){
            // 3.双重检查（减少访问DB）
            nursingLevelList = (List<NursingLevel>) redisTemplate.opsForValue().get(key);
            if (nursingLevelList != null && nursingLevelList.size() > 0) {
                //缓存命中
                return Result.ok(nursingLevelList);
            }

            // 4.查询数据库
            nursingLevelList = nursingLevelMapper.listAll();

            // 5.存入Redis
            redisTemplate.opsForValue().set(key, nursingLevelList, 1, TimeUnit.HOURS);
        }

        return Result.ok(nursingLevelList);
    }

    /** 等级分页查询（Redis缓存全量，内存过滤+分页）
     * */
    @Override
    public Result listByPage(Integer pageNum, Integer pageSize, String name, Integer status) {
        // 获取全量等级列表（优先Redis）
        List<NursingLevel> allList = getAllFromCache();

        // 内存过滤
        List<NursingLevel> filteredList = allList.stream()
                .filter(item -> name == null || name.isEmpty() || (item.getName() != null && item.getName().contains(name)))
                .filter(item -> status == null || (item.getStatus() != null && item.getStatus().equals(status)))
                .collect(Collectors.toList());

        // 内存分页
        int total = filteredList.size();
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<NursingLevel> pageRecords = (fromIndex < total)
                ? filteredList.subList(fromIndex, toIndex)
                : new ArrayList<>();

        Map<String, Object> data = new HashMap<>();
        data.put("records", pageRecords);
        data.put("pages", pages);
        data.put("total", total);

        return Result.ok(data);
    }

    /** 从Redis获取全量护理等级列表，缓存未命中则查DB
     * */
    private List<NursingLevel> getAllFromCache() {
        // 1.查询Redis
        List<NursingLevel> list = (List<NursingLevel>) redisTemplate.opsForValue().get(key);

        // 2.判断Redis是否存储
        if (list != null && !list.isEmpty()) {
            return list;
        }

        // 3.缓存未命中，直接复用 listAll 的逻辑（走双重检查+DB+缓存）
        Result result = listAll();
        return (List<NursingLevel>) result.getData();
    }

    /** 等级详情
     * */
    @Override
    //护理等级ID
    public Result detail(Long id) {
        // 1.查询Redis中是否存在
        NursingLevelDTO nursingLevelDTO = new NursingLevelDTO();
        nursingLevelDTO.setId(id);
        NursingLevel nursingLevel = readNursingLevelRedis(nursingLevelDTO);

        // 2.判断Redis中是否存在
        if (nursingLevel != null) {
            return Result.ok(nursingLevel);
        }

        //本地锁
        synchronized (roomListLock){
            // 3.双重检查
            nursingLevel = readNursingLevelRedis(nursingLevelDTO);
            if (nursingLevel != null) {
                return Result.ok(nursingLevel);
            }

            // 4.缓存未命中，查询DB
            nursingLevel = nursingLevelMapper.selectById(id);

            // 5.判断DB是否存在
            if (nursingLevel == null) {
                return Result.fail("护理等级不存在");
            }
        }

        return Result.ok(nursingLevel);
    }

    /** 添加等级
     * */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public Result insert(NursingLevelDTO nursingLevelDTO) {
        // 1. 查询Redis中是否存在
        NursingLevel nursingLevel = readNursingLevelRedis(nursingLevelDTO);

        // 2.判断Redis中是否存在
        if (nursingLevel != null) {
            return Result.fail("护理等级已存在");
        }

        // 3.缓存未命中，查询DB
        nursingLevel = nursingLevelMapper.selectByName(nursingLevelDTO.getName());

        // 4.判断DB是否存在
        if (nursingLevel != null) {
            return Result.fail("护理等级已存在");
        }

        // 5.执行插入操作
        BeanUtils.copyProperties(nursingLevel, nursingLevelDTO);
        try {
            nursingLevelMapper.insert(nursingLevel);
        } catch (DuplicateKeyException e) {
            // 极少数并发情况下，另一个请求抢先插入了相同床位号
            return Result.fail("护理等级已被占用，请刷新后重试");
        }

        // 6.清除Redis
        transactionAfter();

        return Result.ok("添加成功");
    }

    /** 编辑等级
     * */
    @Override
    public Result update(NursingLevelDTO nursingLevelDTO) {
        // 1. 查询Redis中是否存在
        NursingLevel nursingLevel = readNursingLevelRedis(nursingLevelDTO);

        // 2.判断Redis中是否存在
        if (nursingLevel == null) {
            // 2.1缓存未命中，查询DB
            nursingLevel = nursingLevelMapper.selectByName(nursingLevelDTO.getName());

            // 2.2判断DB是否存在
            if (nursingLevel == null) {
                return Result.fail("护理等级不存在");
            }
        }

        // 3.执行更新操作
        BeanUtils.copyProperties(nursingLevel, nursingLevelDTO);
        nursingLevel.setOldUpdateTime(nursingLevel.getUpdateTime());
        nursingLevelMapper.updateById(nursingLevel);

        // 4.清除Redis
        redisTemplate.delete(key);

        return Result.ok("更新成功");
    }

    /** 删除等级
     * */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    //护理等级ID
    public Result delete(Long id) {
        // 1.查询Redis中是否存在
        NursingLevelDTO nursingLevelDTO = new NursingLevelDTO();
        nursingLevelDTO.setId(id);
        NursingLevel nursingLevel = readNursingLevelRedis(nursingLevelDTO);

        // 2.判断Redis中是否存在
        if (nursingLevel != null) {
            return Result.ok(nursingLevel);
        }

        //本地锁
        synchronized (roomListLock){
            // 3.查询DB中是否存在
            nursingLevel = nursingLevelMapper.selectById(id);

            // 4.判断DB是否存在
            if (nursingLevel == null) {
                return Result.fail("护理等级不存在");
            }

            // 5.删除DB
            nursingLevelMapper.deleteById(id);
        }

        // 6.清除Redis
        transactionAfter();

        return Result.ok("删除成功");
    }

    /** 等级启用/禁用
     * */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    //护理等级ID
    public Result updateStatus(Long id, Integer status) {
        // 1. 查询Redis中是否存在
        NursingLevelDTO nursingLevelDTO = new NursingLevelDTO();
        nursingLevelDTO.setId(id);
        NursingLevel nursingLevel = readNursingLevelRedis(nursingLevelDTO);

        // 2.判断Redis中是否存在
        if (nursingLevel == null) {
            // 2.1缓存未命中，查询DB
            nursingLevel = nursingLevelMapper.selectById(id);

            // 2.2判断DB是否存在
            if (nursingLevel == null) {
                return Result.fail("护理等级不存在");
            }
        }

        // 3.执行更新操作
        nursingLevel.setStatus(status);
        nursingLevel.setOldUpdateTime(nursingLevel.getUpdateTime());
        nursingLevelMapper.updateById(nursingLevel);

        // 4.清除Redis
        transactionAfter();

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
     * Redis查询护理等级
     */
    private NursingLevel readNursingLevelRedis(NursingLevelDTO nursingLevelDTO) {
        // 1.查询Redis
        List<NursingLevel> nursingLevelList = (List<NursingLevel>) redisTemplate.opsForValue().get(key);

        NursingLevel nursingLevel = new NursingLevel();
        // 2.判断Redis是否存储
        if (nursingLevelList != null && !nursingLevelList.isEmpty()) {
            // 2.1获取护理等级
            nursingLevel = nursingLevelList
                    .stream()
                    .filter(n -> n.getId().equals(nursingLevelDTO.getPlanId()) || n.getId().equals(nursingLevelDTO.getId()))
                    .findFirst()
                    .orElse(null);

            // 2.2判断ID对应护理等级是否存在
            if (nursingLevel != null) {
                //存在
                return nursingLevel;
            }else {
                //不存在
                return null;
            }
        }
        //不存在
        return null;
    }

}
