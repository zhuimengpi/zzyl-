package com.zhuimeng.qcmanagement.service.impl;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.RoomTypesDTO;
import com.zhuimeng.entity.RoomTypes;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.RoomTypesMapper;
import com.zhuimeng.qcmanagement.service.RoomTypesService;
import com.zhuimeng.util.Lock.RedisLock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomTypesServiceImpl implements RoomTypesService {
    @Autowired
    private RoomTypesMapper roomTypesMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    // Redis key
    private static String key = RedisKeyProperties.FLOOR_ROOM_TYPE_LIST;

    /** 获取所有房型
     * */
    @Override
    public Result getAllRoomTypes() {
        // 1. 查询Redis
        List<RoomTypes> roomTypes = (List<RoomTypes>) redisTemplate.opsForValue().get(key);

        // 2. 判断缓存是否命中
        if (roomTypes != null && roomTypes.size() != 0) {
            // 缓存命中
            return Result.ok(roomTypes);
        }

        // 创建锁
        RedisLock redisLock = new RedisLock(redisTemplate, key);
        // -获取锁（防止缓存击穿）
        if(!redisLock.tryGetLock()){
            // -获取锁失败，直接返回  （逻辑不可能触发）
            return Result.fail("请刷新重试");
        }

        // 3. 查询数据库
        roomTypes = roomTypesMapper.getAllRoomTypes();

        // 4. 存入Redis
        redisTemplate.opsForValue().set(key, roomTypes);

        // -释放锁
        redisLock.releaseLock();

        return Result.ok(roomTypes);
    }

    /** 按状态查询房型
     * */
    @Override
    public Result getRoomTypesByStatus(Integer status) {
        // 1.查询Redis
        List<RoomTypes> roomTypes = (List<RoomTypes>) redisTemplate.opsForValue().get(key);

        // 2. 判断缓存是否命中
        if (roomTypes != null && roomTypes.size() != 0) {
            // 缓存命中
            // 3. 按状态查询
            List<RoomTypes> filteredList = roomTypes.stream()
                    .filter(r -> r.getStatus() != null && r.getStatus() == status) // 注意判空
                    .collect(Collectors.toList());

            return Result.ok(filteredList);
        }

        // 4. 查询数据库
        roomTypes = roomTypesMapper.getRoomTypesByStatus(status);

        return Result.ok(roomTypes);
    }

    /** 按房型名称查询房型
     * */
    @Override
    public Result getRoomTypesByTypeName(String typeName) {
        // 1.查询Redis
        List<RoomTypes> roomTypes = (List<RoomTypes>) redisTemplate.opsForValue().get(key);

        // 2. 判断缓存是否命中
        if (roomTypes != null && roomTypes.size() != 0) {
            // 缓存命中
            // 3. 按状态查询
            List<RoomTypes> filteredList = roomTypes.stream()
                    .filter(r -> r.getName() != null && r.getName() == typeName) // 注意判空
                    .collect(Collectors.toList());

            return Result.ok(filteredList);
        }

        // 4. 查询数据库
        roomTypes = roomTypesMapper.getRoomTypesByTypeName(typeName);

        return Result.ok(roomTypes);
    }

    /** 房型详情
     * */
    @Override
    public Result getRoomTypesById(Long id) {
        // 1.查询Redis
        List<RoomTypes> roomTypes = (List<RoomTypes>) redisTemplate.opsForValue().get(key);

        // 2. 判断缓存是否命中
        if (roomTypes != null && roomTypes.size() != 0) {
            // 缓存命中
            // 3. 按状态查询
            List<RoomTypes> filteredList = roomTypes.stream()
                    .filter(r -> r.getId() != null && r.getId() == id) // 注意判空
                    .collect(Collectors.toList());

            return Result.ok(filteredList.get(0));
        }

        // 4. 查询数据库
        roomTypes = roomTypesMapper.getRoomTypesById(id);

        return Result.ok(roomTypes.get(0));
    }

    /** 添加房型
     * */
    @Override
    public Result addRoomTypes(RoomTypesDTO roomTypesDTO) {
        RoomTypes roomTypes = new RoomTypes();
        BeanUtils.copyProperties(roomTypesDTO, roomTypes);
        roomTypes.setBedCount(0);

        //1. 添加房型
        Boolean types = roomTypesMapper.addRoomTypes(roomTypes);

        //2. 判断是否添加失败
        if (!types) {
            return Result.fail("添加失败");
        }

        //3. 添加成功，删除Redis缓存
        redisTemplate.delete(key);

        return Result.ok();
    }

    /** 修改房型
     */
    @Override
    public Result updateRoomTypes(Long id, RoomTypesDTO roomTypesDTO) {
        RoomTypes roomTypes = new RoomTypes();
        BeanUtils.copyProperties(roomTypesDTO, roomTypes);
        roomTypes.setId(id);

        //1. 修改房型
        Boolean types = roomTypesMapper.updateRoomTypes(roomTypes);

        //2. 判断是否添加失败
        if (!types) {
            return Result.fail("修改失败");
        }

        //3. 添加成功，删除Redis缓存
        redisTemplate.delete(key);

        return Result.ok();
    }

    /** 删除房型
     * */
    @Override
    public Result deleteRoomTypes(Long id) {
        // 1. 删除房型
        Boolean result = roomTypesMapper.deleteRoomTypes(id);

        // 2. 判断是否删除失败
        if (!result) {
            return Result.fail("删除失败");
        }

        // 3. 删除Redis缓存
        redisTemplate.delete(key);

        return Result.ok();
    }

    /** 房型启用/禁用
     * */
    @Override
    public Result updateRoomTypesStatus(Long id, Integer status) {
        RoomTypes roomTypes = new RoomTypes();
        roomTypes.setStatus(status);
        roomTypes.setId(id);

        // 复用 updateRoomTypes，动态SQL只更新非null字段
        Boolean result = roomTypesMapper.updateRoomTypes(roomTypes);

        if (!result) {
            return Result.fail("操作失败");
        }

        redisTemplate.delete(key);

        return Result.ok();
    }
}
