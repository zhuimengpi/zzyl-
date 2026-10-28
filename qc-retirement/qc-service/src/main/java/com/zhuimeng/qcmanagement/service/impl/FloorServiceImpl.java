package com.zhuimeng.qcmanagement.service.impl;

import com.zhuimeng.Result.Result;
import com.zhuimeng.entity.Floor;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.FloorMapper;
import com.zhuimeng.qcmanagement.service.FloorService;
import com.zhuimeng.util.Lock.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FloorServiceImpl implements FloorService {
    @Autowired
    private FloorMapper floorMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    // 楼层列表缓存Key
    private static final String FLOOR_LIST_KEY = RedisKeyProperties.FLOOR_LIST;

    /** 获取楼层列表
     * */
    @Override
    public Result getAll() {
        // 1.查询Redis
        List<Floor> floorList = (List<Floor>) redisTemplate.opsForValue().get(FLOOR_LIST_KEY);

        // 2.判断Redis中是否存在
        if (floorList != null && floorList.size() != 0) {
            // Redis中存在，直接返回
            return Result.ok(floorList);
        }

        // 创建锁
        RedisLock redisLock = new RedisLock(redisTemplate, FLOOR_LIST_KEY);
        // -获取锁（防止缓存击穿）
        if (!redisLock.tryGetLock()) {
            // -获取锁失败，直接返回
            return Result.fail("请刷新重试");
        }

        // 3.查询数据库
        floorList = floorMapper.getAll();

        // 4.将查询结果存入Redis
        redisTemplate.opsForValue().set(FLOOR_LIST_KEY, floorList);
        // 5.设置过期时间
        redisTemplate.expire(FLOOR_LIST_KEY, 1, TimeUnit.HOURS);

        // -释放锁
        redisLock.releaseLock();

        return Result.ok(floorList);
    }

    /** 楼层详情
     * */
    @Override
    public Result get(Long id) {
        // 1.查询Redis缓存
        List<Floor> floorList = (List<Floor>) redisTemplate.opsForValue().get(FLOOR_LIST_KEY);

        // 2.判断Redis是否命中
        if (floorList != null && !floorList.isEmpty()) {
            // 缓存命中，筛选指定楼层
            Floor floor = floorList.stream()
                    .filter(f -> f.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            if (floor == null) {
                return Result.fail("楼层不存在");
            }
            return Result.ok(floor);
        }

        // 3.缓存未命中，查询数据库
        Floor floor = floorMapper.getById(id);
        if (floor == null) {
            return Result.fail("楼层不存在");
        }
        return Result.ok(floor);
    }

    /** 添加楼层
     * */
    @Override
    public Result add(Floor floor) {
        // 1.添加楼层
        floorMapper.add(floor);

        // 2.删除Redis缓存
        redisTemplate.delete(FLOOR_LIST_KEY);

        return Result.ok("添加成功");
    }

    /** 编辑楼层
     * */
    @Override
    public Result update(Floor floor) {
        // 1.查询Redis缓存
        List<Floor> floorList = (List<Floor>) redisTemplate.opsForValue().get(FLOOR_LIST_KEY);

        // 2.判断Redis中是否存在
        if (floorList == null || floorList.isEmpty()) {
            return Result.fail("楼层不存在");
        }

        // 3.校验楼层是否存在
        Floor oldFloor = floorList.stream()
                .filter(f -> f.getId().equals(floor.getId()))
                .findFirst()
                .orElse(null);
        if (oldFloor == null) {
            return Result.fail("楼层不存在");
        }

        // 4.更新数据库
        Integer rows = floorMapper.update(floor);

        // 5.删除Redis缓存
        if (rows != null && rows > 0) {
            redisTemplate.delete(FLOOR_LIST_KEY);
            return Result.ok("编辑成功");
        }
        return Result.fail("编辑失败");
    }

    /** 删除楼层
     * */
    @Override
    public Result delete(Long id) {
        // 1.查询Redis缓存
        List<Floor> floorList = (List<Floor>) redisTemplate.opsForValue().get(FLOOR_LIST_KEY);

        // 2.判断Redis中是否存在
        if (floorList == null || floorList.isEmpty()) {
            return Result.fail("楼层不存在");
        }

        // 3.校验楼层是否存在
        Floor floor = floorList.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (floor == null) {
            return Result.fail("楼层不存在");
        }

        // 4.删除楼层
        floorMapper.delete(id);

        // 5.删除Redis缓存
        redisTemplate.delete(FLOOR_LIST_KEY);

        return Result.ok("删除成功");
    }

    /** 获取所有楼层及关联智能设备（智能床位用）
     * */
    @Override
    public Result getAllFloorsWithDevice() {
        // 1.查询Redis
        String key = FLOOR_LIST_KEY + "device";
        List<Floor> floorList = (List<Floor>) redisTemplate.opsForValue().get(key);

        // 2.判断Redis中是否存在
        if (floorList != null && floorList.size() != 0) {
            return Result.ok(floorList);
        }

        // 创建锁
        RedisLock redisLock = new RedisLock(redisTemplate, key);
        if (!redisLock.tryGetLock()) {
            return Result.fail("请刷新重试");
        }

        // 3.查询数据库
        floorList = floorMapper.getAllFloorsWithDevice();

        // 4.存入Redis
        redisTemplate.opsForValue().set(key, floorList);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        // -释放锁
        redisLock.releaseLock();

        return Result.ok(floorList);
    }

    /** 获取所有楼层及关联护理员（负责老人用）
     * */
    @Override
    public Result getAllFloorsWithNur() {
        // 1.查询Redis
        String key = FLOOR_LIST_KEY + "nur";
        List<Floor> floorList = (List<Floor>) redisTemplate.opsForValue().get(key);

        // 2.判断Redis中是否存在
        if (floorList != null && floorList.size() != 0) {
            return Result.ok(floorList);
        }

        // 创建锁
        RedisLock redisLock = new RedisLock(redisTemplate, key);
        if (!redisLock.tryGetLock()) {
            return Result.fail("请刷新重试");
        }

        // 3.查询数据库
        floorList = floorMapper.getAllFloorsWithNur();

        // 4.存入Redis
        redisTemplate.opsForValue().set(key, floorList);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        // -释放锁
        redisLock.releaseLock();

        return Result.ok(floorList);
    }

    /** 获取所有楼层、房间、床位完整结构（添加智能设备用）
     * */
    @Override
    public Result getAllWithRoomAndBed() {
        // 1.查询Redis
        String key = FLOOR_LIST_KEY + "roomBed";
        List<Floor> floorList = (List<Floor>) redisTemplate.opsForValue().get(key);

        // 2.判断Redis中是否存在
        if (floorList != null && floorList.size() != 0) {
            return Result.ok(floorList);
        }

        // 创建锁
        RedisLock redisLock = new RedisLock(redisTemplate, key);
        if (!redisLock.tryGetLock()) {
            return Result.fail("请刷新重试");
        }

        // 3.查询数据库
        floorList = floorMapper.getAllWithRoomAndBed();

        // 4.存入Redis
        redisTemplate.opsForValue().set(key, floorList);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        // -释放锁
        redisLock.releaseLock();

        return Result.ok(floorList);
    }

    /** 按床位状态筛选楼层-房间-床位（入住床位选择用）
     * */
    @Override
    public Result getRoomAndBedByBedStatus(Integer status) {
        // 1.查询Redis
        String key = FLOOR_LIST_KEY + "status:" + status;
        List<Floor> floorList = (List<Floor>) redisTemplate.opsForValue().get(key);

        // 2.判断Redis中是否存在
        if (floorList != null && floorList.size() != 0) {
            return Result.ok(floorList);
        }

        // 创建锁
        RedisLock redisLock = new RedisLock(redisTemplate, key);
        if (!redisLock.tryGetLock()) {
            return Result.fail("请刷新重试");
        }

        // 3.查询数据库
        floorList = floorMapper.getRoomAndBedByBedStatus(status);

        // 4.存入Redis
        redisTemplate.opsForValue().set(key, floorList);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        // -释放锁
        redisLock.releaseLock();

        return Result.ok(floorList);
    }

}
