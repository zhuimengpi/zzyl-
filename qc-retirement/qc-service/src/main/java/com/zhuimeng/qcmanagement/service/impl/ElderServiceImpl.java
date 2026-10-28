package com.zhuimeng.qcmanagement.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhuimeng.Result.Result;
import com.zhuimeng.entity.CheckInConfig;
import com.zhuimeng.entity.Elder;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.ElderMapper;
import com.zhuimeng.qcmanagement.service.ElderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/** 老人管理 Service 实现
* */
@Service
public class ElderServiceImpl implements ElderService {
    @Autowired
    private ElderMapper elderMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    // 老人列表全量缓存key
    private static final String key = RedisKeyProperties.FAMILY_ELDER;

    /** 获取所有老人信息列表
     * 查询条件：老人姓名、老人身份证号（模糊匹配）
     */
    @Override
    public Result selectList(String name, String idCard) {
        // 无筛选条件时，优先从Redis获取全量缓存
        boolean noFilter = (name == null || name.isEmpty()) && (idCard == null || idCard.isEmpty());
        if (noFilter) {
            String cacheKey = key + "all";
            List<Elder> cachedList = (List<Elder>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedList != null && !cachedList.isEmpty()) {
                return Result.ok(cachedList);
            }
        }

        // 查询数据库
        List<Elder> elderList = elderMapper.selectList(name, idCard);

        // 无筛选条件时存入Redis缓存（1小时）
        if (noFilter && elderList != null) {
            String cacheKey = key + "all";
            redisTemplate.opsForValue().set(cacheKey, elderList, 1, TimeUnit.HOURS);
        }

        return Result.ok(elderList);
    }

    /** 分页查询老人信息
     * 查询条件：老人姓名、老人身份证号（模糊匹配）
     */
    @Override
    public Result selectListByPage(Integer pageNum, Integer pageSize, String name, String idCard) {
        PageHelper.startPage(pageNum, pageSize);
        List<Elder> elderList = elderMapper.selectList(name, idCard);
        PageInfo<Elder> pageInfo = new PageInfo<>(elderList);

        Map<String, Object> data = new HashMap<>();
        data.put("records", elderList);
        data.put("pages", pageInfo.getPages());
        data.put("total", pageInfo.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return Result.ok(data);
    }

    /** 分页查询老人信息（V2版本）
     * 查询条件：老人姓名、老人身份证号（模糊匹配）
     */
    @Override
    public Result pageQuery(Integer pageNum, Integer pageSize, String name, String idCard) {
        PageHelper.startPage(pageNum, pageSize);
        List<Elder> elderList = elderMapper.selectList(name, idCard);
        PageInfo<Elder> pageInfo = new PageInfo<>(elderList);

        Map<String, Object> data = new HashMap<>();
        data.put("records", elderList);
        data.put("pages", pageInfo.getPages());
        data.put("total", pageInfo.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return Result.ok(data);
    }

    /** 校验身份证号是否已登记
     * 返回老人信息（若存在），前端根据是否有idCardNo判断是否重复
     */
    @Override
    public Result selectByIdCard(String idCardNo) {
        if (idCardNo == null || idCardNo.isBlank()) {
            return Result.fail("身份证号不能为空");
        }

        // 通过全量查询过滤（idCard精确匹配）
        List<Elder> elders = elderMapper.selectList(null, idCardNo);
        Elder elder = elders.stream()
                .filter(e -> idCardNo.equals(e.getIdCardNo()))
                .findFirst()
                .orElse(null);

        return Result.ok(elder);
    }

    /** 查询指定老人入住信息
     * 包含老人基本信息和入住配置
     */
    @Override
    public Result checkInInfo(Long id) {
        if (id == null) {
            return Result.fail("老人ID不能为空");
        }

        // 1. 查询老人基本信息
        Elder elder = elderMapper.selectById(id);
        if (elder == null) {
            return Result.fail("老人不存在");
        }

        // 2. 查询入住配置
        CheckInConfig config = elderMapper.selectCheckInConfigByElderId(id);

        // 3. 组装返回数据
        Map<String, Object> data = new HashMap<>();
        // 老人基本信息
        data.put("id", elder.getId());
        data.put("name", elder.getName());
        data.put("idCardNo", elder.getIdCardNo());
        data.put("age", elder.getAge());
        data.put("sex", elder.getSex());
        data.put("phone", elder.getPhone());
        data.put("status", elder.getStatus());
        data.put("image", elder.getImage());
        data.put("bedNumber", elder.getBedNumber());
        data.put("bedId", elder.getBedId());
        data.put("remark", elder.getRemark());
        // 入住配置
        if (config != null) {
            data.put("checkInStartTime", config.getCheckInStartTime());
            data.put("checkInEndTime", config.getCheckInEndTime());
            data.put("costStartTime", config.getCostStartTime());
            data.put("costEndTime", config.getCostEndTime());
            data.put("nursingLevelId", config.getNursingLevelId());
            data.put("bedNo", config.getBedNo());
            data.put("depositAmount", config.getDepositAmount());
            data.put("nursingCost", config.getNursingCost());
            data.put("bedCost", config.getBedCost());
            data.put("otherCost", config.getOtherCost());
            data.put("medicalInsurancePayment", config.getMedicalInsurancePayment());
            data.put("governmentSubsidy", config.getGovernmentSubsidy());
        }

        return Result.ok(data);
    }

    /** 为老人分配护理员
     * 请求体格式：[{"elderId": 1, "nursingIds": [101, 102]}, ...]
     */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result setNursing(List<Map<String, Object>> nursingList) {
        if (nursingList == null || nursingList.isEmpty()) {
            return Result.fail("护理员绑定信息不能为空");
        }

        for (Map<String, Object> item : nursingList) {
            Object elderIdObj = item.get("elderId");
            if (elderIdObj == null) {
                return Result.fail("老人ID不能为空");
            }
            Long elderId = Long.valueOf(elderIdObj.toString());

            // 删除旧的护理员绑定
            elderMapper.deleteNursingByElderId(elderId);

            // 插入新的护理员绑定
            Object nursingIdsObj = item.get("nursingIds");
            if (nursingIdsObj instanceof List && !((List<?>) nursingIdsObj).isEmpty()) {
                List<Long> nursingIds = ((List<?>) nursingIdsObj).stream()
                        .map(Object::toString)
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
                elderMapper.insertNursing(elderId, nursingIds);
            }
        }

        // 清除老人列表缓存
        String cacheKey = key + "all";
        redisTemplate.delete(cacheKey);

        return Result.ok("设置成功");
    }
}
