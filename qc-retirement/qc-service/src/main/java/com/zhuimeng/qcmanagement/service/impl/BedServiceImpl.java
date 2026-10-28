package com.zhuimeng.qcmanagement.service.impl;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.BedDTO;
import com.zhuimeng.entity.Bed;
import com.zhuimeng.entity.Room;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.BedMapper;
import com.zhuimeng.qcmanagement.mapper.RoomMapper;
import com.zhuimeng.qcmanagement.service.BedService;
import com.zhuimeng.util.Lock.RedisLock;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class BedServiceImpl implements BedService {
    @Autowired
    private BedMapper bedMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    //床位key
    private static final String key = RedisKeyProperties.FLOOR_ROOM_BED_LIST;

    /** 床位详情（本地锁查询）
     * */
    @NotNull
    private List<Bed> getBedListWithLocalLock(Long id) {
        // 1.查询Redis
        List<Bed> bedList = (List<Bed>) redisTemplate.opsForValue().get(key + id);

        // 2.判断Redis
        if(bedList != null && !bedList.isEmpty()){
            // Redis中有数据
            return bedList;
        }

        // 本地锁
        synchronized (id.toString().intern()) {
            // 3.双重检查（减少访问DB）
            bedList = (List<Bed>) redisTemplate.opsForValue().get(key + id);
            if(bedList != null && !bedList.isEmpty()){
                // Redis中有数据
                return bedList;
            }

            // 4.查询数据库
            bedList = bedMapper.readRoom(id);

            // 5.存入Redis
            redisTemplate.opsForValue().set(key + id, bedList, 1, TimeUnit.HOURS);
        }

        return bedList;
    }

    /** 事务提交后删除缓存（确保只有成功才删除）
     * */
    private void transactionAfter(BedDTO bedDTO) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        redisTemplate.delete(key + bedDTO.getRoomId());
                    }
                }
        );
    }

    /** 房间下床位
     * */
    @Override
    //房间ID
    public Result readRoom(Long id) {
        return Result.ok(getBedListWithLocalLock(id));
    }

    /** 床位详情
     * */
    @Override
    //床位ID
    public Result read(Long id) {
        Bed bed = bedMapper.read(id);
        if (bed == null) {
            return Result.fail("床位不存在");
        }
        return Result.ok(bed);
    }

    /** 添加床位
     * */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public Result create(BedDTO bedDTO) {
        // 1.查询Redis
        List<Bed> bedList = getBedListWithLocalLock(bedDTO.getRoomId());

        // 2.判断房间是否存在
        if (bedList == null || bedList.isEmpty()) {
            return Result.fail("房间不存在");
        }

        // 3.判断床位是否已存在
        Bed oldBed = bedList
                .stream()
                .filter(bed -> bed.getBedNumber().equals(bedDTO.getBedNumber()))
                .findFirst()
                .orElse(null);
        if (oldBed != null) {
            return Result.fail("床位已存在");
        }

        // 4.添加床位
        Bed bed = new Bed();
        BeanUtils.copyProperties(bedDTO, bed);
        bed.setBedStatus(0);
        try {
            bedMapper.create(bed);
        } catch (DuplicateKeyException e) {
            // 极少数并发情况下，另一个请求抢先插入了相同床位号
            return Result.fail("床位号已被占用，请刷新后重试");
        }

        // 5. 事务提交后删除缓存（确保只有成功才删除）
        transactionAfter(bedDTO);

        return Result.ok("添加成功");
    }

    /** 编辑床位
     * */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public Result update(BedDTO bedDTO) {
        // 1.查询Redis
        List<Bed> bedList = getBedListWithLocalLock(bedDTO.getRoomId());

        // 2.判断房间是否存在
        if (bedList == null || bedList.isEmpty()) {
            return Result.fail("房间不存在");
        }

        // 3.判断床位是否已存在
        Bed oldBed = bedList
                .stream()
                .filter(bed -> bed.getBedNumber().equals(bedDTO.getBedNumber()))
                .findFirst()
                .orElse(null);
        if (oldBed == null) {
            return Result.fail("床位不存在");
        }

        oldBed.setOldDate(oldBed.getUpdateTime());  //老日期   （乐观锁）

        // 4.更新数据库
        oldBed.setBedNumber(bedDTO.getBedNumber());
        oldBed.setSort(bedDTO.getSort());
        oldBed.setRoomId(bedDTO.getRoomId());
        Integer rows = bedMapper.update(oldBed);

        // 5. 事务提交后删除缓存（确保只有成功才删除）
        transactionAfter(bedDTO);

        return Result.ok("更新成功");
    }

    /** 删除床位
     * */
    @Override
    //床位ID
    public Result delete(Long id) {
        // 1.查询床位详情
        Bed bed = bedMapper.read(id);
        if (bed == null) {
            return Result.fail("床位不存在");
        }

        // 2.删除床位
        bedMapper.delete(id);

        // 3.清除Redis缓存
        redisTemplate.delete(key + bed.getRoomId());

        return Result.ok("删除成功");
    }
}
