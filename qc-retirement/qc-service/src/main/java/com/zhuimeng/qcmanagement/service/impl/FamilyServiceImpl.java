package com.zhuimeng.qcmanagement.service.impl;

import com.github.pagehelper.PageHelper;
import com.zhuimeng.Result.Result;
import com.zhuimeng.Thread.UserHolder;
import com.zhuimeng.dto.DevicePageDTO;
import com.zhuimeng.dto.ElderDTO;
import com.zhuimeng.entity.DeviceData;
import com.zhuimeng.entity.Elder;
import com.zhuimeng.entity.MemberElder;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.DeviceMapper;
import com.zhuimeng.qcmanagement.mapper.FUserMapper;
import com.zhuimeng.qcmanagement.mapper.FamilyMapper;
import com.zhuimeng.qcmanagement.service.FamilyService;
import com.zhuimeng.vo.DeviceDataVO;
import com.zhuimeng.vo.ElderDeviceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FamilyServiceImpl implements FamilyService {
    @Autowired
    private FamilyMapper familyMapper;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    //老人余额key
    private static final String ELDER_BALANCE = RedisKeyProperties.FAMILY_BALANCE;

    /** 家人列表（分页查询）
     * */
    @Override
    public Result listByPage(Integer pageNum, Integer pageSize) {
        // 1.获取用户id
        String userId = UserHolder.getId();

        // 2.查询Redis
        String key = RedisKeyProperties.FAMILY_ELDER + userId;
        //查询数据总数
        Long total = redisTemplate.opsForList().size(key);
        if (total != null && total != 0) {
            //Redis存在数据
            // 2.1 设置分页参数
            // pageNum 从 1 开始，Redis 索引从 0 开始
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize - 1, total.intValue() - 1);

            // 2.2 获取分页数据
            List<Object> list = redisTemplate.opsForList().range(key, start, end);

            // 2.3 返回列表
            return Result.ok(list);
        }

        // 3.设置分页参数
        PageHelper.startPage(pageNum, pageSize);
        List<ElderDeviceVO> list = familyMapper.selectPage(userId);

        // 4.返回列表（前端data期望为数组）
        return Result.ok(list);
    }

    /** 全部家人列表
     * */
    @Override
    public Result listAll() {
        // 1.获取用户id
        String userId = UserHolder.getId();

        // 2.查询Redis存储
        String key = RedisKeyProperties.FAMILY_ELDER + userId;
        List<ElderDeviceVO> elderList = redisTemplate.opsForList().range(key, 0, -1);

        // 3.判断Redis中是否存在
        if (elderList != null && !elderList.isEmpty()) {
            // 3.1Redis中存在
            return Result.ok(elderList);
        }

        // 4.查询家人列表
        elderList = familyMapper.listAll(userId);

        // 5.将家人列表存入Redis
        redisTemplate.opsForList().rightPushAll(key, elderList);
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
        return Result.ok(elderList);
    }

    /** 绑定家人
     * */
    @Transactional(rollbackFor = {Exception.class})        //所有异常都进行回滚
    @Override
    public Result addFamily(ElderDTO elderDTO) {
        // 1.参数校验
        if (elderDTO.getName() == null || elderDTO.getName().isBlank()) {
            return Result.fail("老人姓名不能为空");
        }
        if (elderDTO.getIdCard() == null || elderDTO.getIdCard().isBlank()) {
            return Result.fail("身份证号不能为空");
        }
        if (elderDTO.getRemark() == null || elderDTO.getRemark().isBlank()) {
            return Result.fail("与老人关系不能为空");
        }

        // 2.获取当前用户id
        String userId = UserHolder.getId();

        // 3.查询老人是否已存在
        Elder elder = familyMapper.selectByIdCard(elderDTO.getIdCard());
        if (elder != null) {
            // 老人已存在，检查当前用户是否已绑定该老人
            MemberElder existBinding = familyMapper.selectMemberElderByMemberAndElder(
                    Long.valueOf(userId), elder.getId());
            if (existBinding != null) {
                return Result.fail("已绑定该老人");
            }

            // 向member_elder插入绑定关系
            MemberElder memberElder = new MemberElder();
            memberElder.setElderId(elder.getId());
            memberElder.setMemberId(Long.valueOf(userId));
            memberElder.setRemark(elderDTO.getRemark());
            familyMapper.insertMemberElder(memberElder);

            // 删除Redis缓存
            String elderKey = RedisKeyProperties.FAMILY_ELDER + userId;
            redisTemplate.delete(elderKey);

            return Result.ok("绑定成功");
        }

        // 4.老人不存在，保存老人信息
        Elder newElder = new Elder();
        newElder.setIdCardNo(elderDTO.getIdCard());
        newElder.setName(elderDTO.getName());
        familyMapper.insert(newElder);

        // 5.保存用户与老人关联
        String elderId = familyMapper.selectId(elderDTO.getIdCard());
        MemberElder memberElder = new MemberElder();
        memberElder.setElderId(Long.valueOf(elderId));
        memberElder.setMemberId(Long.valueOf(userId));
        memberElder.setRemark(elderDTO.getRemark());
        familyMapper.insertMemberElder(memberElder);

        // 6.初始化老人余额（预交款、押金、欠费均为0，状态正常）
        familyMapper.insertBalance(Long.valueOf(elderId), elderDTO.getName());

        // 7.删除Redis缓存
        String elderKey = RedisKeyProperties.FAMILY_ELDER + userId;
        redisTemplate.delete(elderKey);
        String balanceKey = RedisKeyProperties.FAMILY_BALANCE + elderId;
        redisTemplate.delete(balanceKey);

        return Result.ok("绑定成功");
    }

    /** 解绑家人
     * */
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public Result deleteById(Long elderId) {
        // 1. 查询与老人关联
        MemberElder memberElder = familyMapper.selectMemberElder(elderId);

        // 2. 判断关联是否存在
        if(memberElder == null){
            return Result.fail("关联不存在");
        }

        // 3. 删除关联
        familyMapper.deleteMemberElder(elderId);

        // 4. 删除老人
        familyMapper.delete(elderId);

        // 5.删除Redis缓存
        String elderKey = RedisKeyProperties.FAMILY_ELDER + UserHolder.getId();
        redisTemplate.delete(elderKey);
        String balanceKey = RedisKeyProperties.FAMILY_BALANCE + elderId;
        redisTemplate.delete(balanceKey);

        return Result.ok("解绑成功");
    }

    /** 查询设备数据(最新)
     * */
    @Override
    public Result queryServiceProperties(Long iotId) {
        // 1.查询设备数据
        ArrayList<DeviceData> dataList = deviceMapper.selectById(iotId);
        if(dataList == null || dataList.isEmpty()){
            return Result.fail("设备数据不存在");
        }
        return Result.ok(dataList);
    }

    /** 健康数据按日查询
     * */
    @Override
    public Result queryDeviceDataListByDay(DevicePageDTO devicePageDTO) {
        ArrayList<DeviceDataVO> dataVOS = deviceMapper.queryDeviceDataListByDay(devicePageDTO);
        return Result.ok(dataVOS);
    }

    /** 健康数据按周查询
     * */
    @Override
    public Result queryDeviceDataListByWeek(DevicePageDTO devicePageDTO) {
        ArrayList<DeviceDataVO> dataVOS = deviceMapper.queryDeviceDataListByDay(devicePageDTO);
        return Result.ok(dataVOS);
    }

    /** 异常数据查询
     * */
    @Override
    public Result queryDeviceDataListAbnormal(DevicePageDTO devicePageDTO) {
        PageHelper.startPage(devicePageDTO.getPageNum(), devicePageDTO.getPageSize());
        ArrayList<DeviceDataVO> dataVOS = deviceMapper.queryDeviceDataListAbnormal(devicePageDTO);
        return Result.ok(dataVOS);
    }
}
