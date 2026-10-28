package com.zhuimeng.qcmanagement.service.impl;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.RoomDTO;
import com.zhuimeng.entity.Bed;
import com.zhuimeng.entity.Device;
import com.zhuimeng.entity.DeviceData;
import com.zhuimeng.entity.Elder;
import com.zhuimeng.entity.Room;
import com.zhuimeng.entity.RoomType;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.BedMapper;
import com.zhuimeng.qcmanagement.mapper.DeviceMapper;
import com.zhuimeng.qcmanagement.mapper.FamilyMapper;
import com.zhuimeng.qcmanagement.mapper.RoomMapper;
import com.zhuimeng.qcmanagement.service.BedService;
import com.zhuimeng.qcmanagement.service.RoomService;
import com.zhuimeng.util.Lock.RedisLock;
import com.zhuimeng.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private BedMapper bedMapper;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private FamilyMapper familyMapper;

    //热门住房key
    private static final String hootRoomTypekey = RedisKeyProperties.ROOM_TYPE;
    //住房列表key
    private static final String roomListKey = RedisKeyProperties.FLOOR_ROOM_LIST;
    //床位key
    private static final String key = RedisKeyProperties.FLOOR_ROOM_BED_LIST;

    private static final Object roomListLock = new Object(); // 独立的锁对象

    /** 为每个房间填充床位数据
     * */
    private void getBedData(List<Room> roomByFloorId) {
        if (roomByFloorId == null || roomByFloorId.isEmpty()) return;

        // 1. 构造所有房间的缓存Key
        List<String> cacheKeys = roomByFloorId.stream()
                .map(room -> key + room.getId())
                .collect(Collectors.toList());

        // 2. 批量查Redis（一次网络请求，极快）
        List<List<Bed>> cachedBeds = (List<List<Bed>>) redisTemplate.opsForValue().multiGet(cacheKeys)
                .stream()
                .map(obj -> (List<Bed>) obj)
                .collect(Collectors.toList());

        // 3. 判断是否所有房间都命中了缓存
        boolean allHit = cachedBeds.stream().allMatch(beds -> beds != null);

        if (allHit) {
            // 全命中：直接填充
            for (int i = 0; i < roomByFloorId.size(); i++) {
                roomByFloorId.get(i).setBedVoList(cachedBeds.get(i));
            }
        }
        else {
            // 有未命中：批量查SQL
            List<Long> roomIds = roomByFloorId.stream().map(Room::getId).collect(Collectors.toList());
            List<Bed> allBeds = bedMapper.selectByRoomIds(roomIds);

            Map<Long, List<Bed>> bedMap = allBeds.stream()
                    .collect(Collectors.groupingBy(Bed::getRoomId));

            // 填充并回写缓存（把未命中的房间缓存补上）
            roomByFloorId.forEach(room -> {
                List<Bed> beds = bedMap.getOrDefault(room.getId(), new ArrayList<>());
                room.setBedVoList(beds);
                // 补写缓存（可选，防止下次再穿透）
                redisTemplate.opsForValue().set(key + room.getId(), beds, 1, TimeUnit.HOURS);
            });
        }
    }

    /** 获取热门住房
     * */
    @Override
    public Result getHotRoomTypes(String status) {
        // 1.查询Redis
        List<RoomType> roomType = (List<RoomType>) redisTemplate.opsForValue().get(hootRoomTypekey);

        // 2.判断Redis是否存储
        if (roomType != null && !roomType.isEmpty()) {
            //2.1 Redis存储，直接返回
            return Result.ok(roomType);
        }
        // 本地锁
        synchronized(roomListLock){
            // 3.双重检查（减少访问DB）
            roomType = (List<RoomType>) redisTemplate.opsForValue().get(hootRoomTypekey);
            if (roomType != null && !roomType.isEmpty()) {
                //2.1 Redis存储，直接返回
                return Result.ok(roomType);
            }

            // 4.查询数据库(status=1)
            roomType = roomMapper.getRoomTypes(status);

            // 5.存储Redis
            redisTemplate.opsForValue().set(hootRoomTypekey, roomType,1, TimeUnit.DAYS);
        }

        // 5.返回结果
        return Result.ok(roomType);
    }

    /** 获取所有房间
     * */
    @Override
    public Result getAll() {
        // 1.查询Redis
        List<Room> rooms = (List<Room>) redisTemplate.opsForValue().get(roomListKey);

        // 2.判断Redis是否存储
        if (rooms != null && !rooms.isEmpty()) {
            //2.1 Redis存储，直接返回
            return Result.ok(rooms);
        }

        // 本地锁
        synchronized(roomListLock){
            // 3.双重检查（减少访问DB）
            rooms = (List<Room>) redisTemplate.opsForValue().get(roomListKey);
            if (rooms != null && !rooms.isEmpty()) {
                //2.1 Redis存储，直接返回
                return Result.ok(rooms);
            }

            // 4.查询数据库
            rooms = roomMapper.getAll();

            // 5.存储Redis
            redisTemplate.opsForValue().set(roomListKey, rooms,1, TimeUnit.DAYS);
        }

        return Result.ok(rooms);
    }

    /** 房间详情
     * */
    @Override
    //房间ID
    public Result getDetail(Long id) {
        //TODO 更多房间信息查询

        // 1.查询Redis
        ArrayList<Room> rooms = (ArrayList<Room>) redisTemplate.opsForValue().get(roomListKey);

        // 2.判断Redis是否存储
        if (rooms != null && !rooms.isEmpty()) {
            //2.1 Redis存储，查询指定房间
            Room roomDetail = rooms
                    .stream()
                    .filter(room -> room.getId().equals(id))
                    .findFirst()    //获取第一个
                    .orElse(null);  //如果没有获取到，返回null

            return Result.ok(roomDetail);
        }

        // 3.查询数据库
        Room room = roomMapper.getDetail(id);

        return Result.ok(room);
    }

    /** 楼层下房间（含床位数据）
     * */
    @Override
    //楼层ID
    public Result getRoomByFloorId(Long id) {
        // 1.查询Redis
        ArrayList<Room> rooms = (ArrayList<Room>) redisTemplate.opsForValue().get(roomListKey);

        // 2.判断Redis是否存储
        if (rooms != null && !rooms.isEmpty()) {
            //2.1 Redis存储，查询指定楼层下的房间
            List<Room> roomByFloorId =  rooms
                    .stream()
                    .filter(room -> room.getFloorId().equals(id))
                    .toList();  //获取所有匹配的房间

            // 2.2 为每个房间填充床位数据
            getBedData(roomByFloorId);

            return Result.ok(roomByFloorId);
        }

        //本地锁
        synchronized (roomListLock) {
            // 3.查询数据库
            rooms = roomMapper.getRoomByFloorId(id);

            // 4.为每个房间填充床位数据
            getBedData(rooms);
        }

        return Result.ok(rooms);
    }

    /** 添加房间
     * */
    @Override
    public Result addRoom(RoomDTO roomDTO) {
        // 1.生成新房间
        Room room = new Room();
        room.setFloorId(roomDTO.getFloorId());
        room.setTypeName(roomDTO.getTypeName());
        room.setCode(roomDTO.getCode());
        room.setSort(roomDTO.getSort());

        // 2.添加房间
        Integer addRoom = roomMapper.addRoom(room);

        // 3.删除缓存
        if (addRoom != null && addRoom > 0) {
            redisTemplate.delete(roomListKey);
            return Result.ok("添加成功");
        }
        return Result.fail("添加失败");
    }

    /** 编辑房间
     * */
    //房间ID
    @Override
    public Result editRoom(RoomDTO roomDTO) {
        // 1.查询Redis
        List<Room> rooms = (List<Room>) redisTemplate.opsForValue().get(roomListKey);

        // 2.判断Redis中是否存在
        if (rooms == null || rooms.isEmpty()) {
            return Result.fail("房间不存在");
        }

        // 3. Redis中存在，查询房间
        Room room = rooms.stream().filter(r -> r.getId().equals(roomDTO.getId())).findFirst().orElse(null);
        if (room == null) {
            // 房间不存在
            return Result.fail("房间不存在");
        }

        // 4.修改房间信息
        room.setTypeName(roomDTO.getTypeName());
        room.setCode(roomDTO.getCode());
        room.setSort(roomDTO.getSort());
        room.setId(roomDTO.getId());
        Boolean editRoom = roomMapper.editRoom(room);

        // 5. 清除Redis
        if (editRoom) {
            redisTemplate.delete(roomListKey);
            return Result.ok("编辑成功");
        }

        return Result.fail("编辑失败");
    }

    /** 删除房间
     * */
    @Override
    public Result deleteRoom(Long id) {
        // 1.查询Redis
        List<Room> rooms = (List<Room>) redisTemplate.opsForValue().get(roomListKey);

        // 2.判断Redis中是否存在
        if (rooms == null || rooms.isEmpty()) {
            return Result.fail("房间不存在");
        }

        // 3. Redis中存在，查询房间
        Room room = rooms.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
        if (room == null) {
            // 房间不存在
            return Result.fail("房间不存在");
        }

        // 4.删除房间
        Boolean deleteRoom = roomMapper.deleteRoom(id);

        // 5. 清除Redis
        if (deleteRoom) {
            redisTemplate.delete(roomListKey);
            return Result.ok("删除成功");
        }

        return Result.fail("删除失败");
    }

    /** 为房间和床位填充设备数据
     * */
    private void fillDeviceData(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) return;

        // 1. 收集所有房间ID和床位ID
        List<String> roomIdStrs = rooms.stream()
                .map(r -> String.valueOf(r.getId()))
                .collect(Collectors.toList());

        List<Bed> allBeds = rooms.stream()
                .filter(r -> r.getBedVoList() != null)
                .flatMap(r -> r.getBedVoList().stream())
                .collect(Collectors.toList());

        List<String> bedIdStrs = allBeds.stream()
                .map(b -> String.valueOf(b.getId()))
                .collect(Collectors.toList());

        // 2. 批量查询房间绑定的设备 (physicalLocationType=1 房间)
        List<Device> roomDevices = roomIdStrs.isEmpty()
                ? Collections.emptyList()
                : deviceMapper.selectByLocationList(roomIdStrs, 1);
        Map<Long, List<Device>> roomDeviceMap = roomDevices.stream()
                .collect(Collectors.groupingBy(d -> Long.valueOf(d.getBindingLocation())));

        // 3. 批量查询床位绑定的设备 (physicalLocationType=2 床位)
        List<Device> bedDevices = bedIdStrs.isEmpty()
                ? Collections.emptyList()
                : deviceMapper.selectByLocationList(bedIdStrs, 2);
        Map<Long, List<Device>> bedDeviceMap = bedDevices.stream()
                .collect(Collectors.groupingBy(d -> Long.valueOf(d.getBindingLocation())));

        // 4. 收集所有设备的id（device_data.iot_id 对应的是 device.id），批量查询设备数据
        List<String> allDeviceIds = new ArrayList<>();
        roomDevices.forEach(d -> allDeviceIds.add(String.valueOf(d.getId())));
        bedDevices.forEach(d -> allDeviceIds.add(String.valueOf(d.getId())));

        Map<String, List<DeviceData>> deviceDataMap = new HashMap<>();
        if (!allDeviceIds.isEmpty()) {
            List<DeviceData> allDeviceData = deviceMapper.selectDataByIotIds(allDeviceIds);
            deviceDataMap = allDeviceData.stream()
                    .collect(Collectors.groupingBy(DeviceData::getIotId));
        }

        // 5. 为每个设备填充设备数据
        for (Device device : roomDevices) {
            device.setDeviceDataVos(deviceDataMap.getOrDefault(String.valueOf(device.getId()), Collections.emptyList()));
        }
        for (Device device : bedDevices) {
            device.setDeviceDataVos(deviceDataMap.getOrDefault(String.valueOf(device.getId()), Collections.emptyList()));
        }

        // 6. 将设备分配回房间和床位
        for (Room room : rooms) {
            room.setDeviceVos(roomDeviceMap.getOrDefault(room.getId(), Collections.emptyList()));
            if (room.getBedVoList() != null) {
                for (Bed bed : room.getBedVoList()) {
                    bed.setDeviceVos(bedDeviceMap.getOrDefault(bed.getId(), Collections.emptyList()));
                }
            }
        }
    }

    /** 智能床位-房间（关联查询房间绑定的设备，存在设备才返回）
     * */
    @Override
    //楼层ID
    public Result getRoomsWithDeviceByFloorId(Long id) {
        // 1.查询Redis缓存
        ArrayList<Room> rooms = (ArrayList<Room>) redisTemplate.opsForValue().get(roomListKey);

        // 2.判断Redis是否存储
        if (rooms != null && !rooms.isEmpty()) {
            // 2.1 Redis存储，查询指定楼层下的房间
            List<Room> roomByFloorId = rooms.stream()
                    .filter(room -> room.getFloorId().equals(id))
                    .collect(Collectors.toList());

            // 2.2 为每个房间填充床位数据
            getBedData(roomByFloorId);

            // 2.3 为房间和床位填充设备数据
            fillDeviceData(roomByFloorId);

            // 2.4 为床位填充老人数据
            fillElderAndNurseData(roomByFloorId);

            // 2.5 过滤：只保留存在设备的房间（房间本身有设备 或 任一床位有设备）
            List<Room> roomsWithDevice = roomByFloorId.stream()
                    .filter(r -> !r.getDeviceVos().isEmpty()
                            || (r.getBedVoList() != null
                                && r.getBedVoList().stream().anyMatch(b -> !b.getDeviceVos().isEmpty())))
                    .collect(Collectors.toList());

            return Result.ok(roomsWithDevice);
        }

        List<Room> roomsWithDevice = new ArrayList<Room>();
        // 3.缓存未命中，查询数据库
        synchronized (roomListLock) {
            rooms = roomMapper.getRoomByFloorId(id);

            // 4.为每个房间填充床位数据
            getBedData(rooms);

            // 5.为房间和床位填充设备数据
            fillDeviceData(rooms);

            // 6.为床位填充老人数据
            fillElderAndNurseData(rooms);

            // 7.过滤：只保留存在设备的房间
            roomsWithDevice = rooms.stream()
                    .filter(r -> !r.getDeviceVos().isEmpty()
                            || (r.getBedVoList() != null
                                && r.getBedVoList().stream().anyMatch(b -> !b.getDeviceVos().isEmpty())))
                    .collect(Collectors.toList());
        }

        return Result.ok(roomsWithDevice);
    }

    /** 负责老人-房间（含床位及老人数据）
     * */
    @Override
    //楼层ID
    public Result getRoomsWithNurByFloorId(Long id) {
        // 1.查询Redis缓存
        ArrayList<Room> rooms = (ArrayList<Room>) redisTemplate.opsForValue().get(roomListKey);

        // 2.判断Redis是否存储
        if (rooms != null && !rooms.isEmpty()) {
            // 2.1 Redis存储，查询指定楼层下的房间
            List<Room> roomByFloorId = rooms.stream()
                    .filter(room -> room.getFloorId().equals(id))
                    .collect(Collectors.toList());

            // 2.2 为每个房间填充床位数据
            getBedData(roomByFloorId);

            // 2.3 为床位填充老人数据
            fillElderAndNurseData(roomByFloorId);

            return Result.ok(roomByFloorId);
        }

        // 3.缓存未命中，查询数据库
        synchronized (roomListLock) {
            rooms = roomMapper.getRoomByFloorId(id);

            // 4.为每个房间填充床位数据
            getBedData(rooms);

            // 5.为床位填充老人数据
            fillElderAndNurseData(rooms);
        }

        return Result.ok(rooms);
    }

    /** 为床位填充老人姓名和护理员数据
     * */
    private void fillElderAndNurseData(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) return;

        // 1. 收集所有床位ID
        List<Long> bedIds = rooms.stream()
                .filter(r -> r.getBedVoList() != null)
                .flatMap(r -> r.getBedVoList().stream())
                .map(Bed::getId)
                .collect(Collectors.toList());

        if (bedIds.isEmpty()) return;

        // 2. 根据床位ID批量查询入住老人
        List<Elder> elders = familyMapper.selectByBedIds(bedIds);
        Map<Long, Elder> bedElderMap = new HashMap<>();
        for (Elder elder : elders) {
            if (elder.getBedId() != null) {
                bedElderMap.put(elder.getBedId(), elder);
            }
        }

        // 3. 将老人姓名、老人ID填充到对应床位
        for (Room room : rooms) {
            if (room.getBedVoList() != null) {
                for (Bed bed : room.getBedVoList()) {
                    Elder elder = bedElderMap.get(bed.getId());
                    if (elder != null) {
                        bed.setName(elder.getName());
                        bed.setElderId(elder.getId());
                    }
                }
            }
        }

        // 4. 收集所有有老人的elderId
        List<Long> elderIds = elders.stream()
                .map(Elder::getId)
                .collect(Collectors.toList());

        if (elderIds.isEmpty()) return;

        // 5. 批量查询护理员
        List<Map<String, Object>> nursingRows = familyMapper.selectNursingByElderIds(elderIds);

        // 6. 按 elderId 分组
        Map<Long, List<UserVO>> elderNurseMap = new HashMap<>();
        for (Map<String, Object> row : nursingRows) {
            Long elderId = Long.valueOf(row.get("elderId").toString());
            UserVO vo = new UserVO();
            vo.setId(Long.valueOf(row.get("id").toString()));
            vo.setRealName((String) row.get("realName"));
            elderNurseMap.computeIfAbsent(elderId, k -> new ArrayList<>()).add(vo);
        }

        // 7. 将护理员填充到对应床位
        for (Room room : rooms) {
            if (room.getBedVoList() != null) {
                for (Bed bed : room.getBedVoList()) {
                    Elder elder = bedElderMap.get(bed.getId());
                    if (elder != null) {
                        bed.setUserVos(elderNurseMap.getOrDefault(elder.getId(), Collections.emptyList()));
                    }
                }
            }
        }
    }

}
