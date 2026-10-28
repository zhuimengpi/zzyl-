package com.zhuimeng.qcmanagement.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhuimeng.Result.Result;
import com.zhuimeng.Thread.UserHolder;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.FamilyMapper;
import com.zhuimeng.qcmanagement.mapper.NursingTaskMapper;
import com.zhuimeng.qcmanagement.mapper.OrderMapper;
import com.zhuimeng.qcmanagement.service.NursingTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/** 护理任务 Service 实现
* */
@Service
public class NursingTaskServiceImpl implements NursingTaskService {
    @Autowired
    private NursingTaskMapper nursingTaskMapper;
    @Autowired
    private FamilyMapper familyMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    public RedisTemplate redisTemplate;

    //护理任务key
    private static final String key = RedisKeyProperties.NURSING_TASK;

    /** 分页查询护理任务列表 */
    @Override
    public Result page(Integer pageNum, Integer pageSize, Integer status,
                       String elderName, Long nurseId, Long projectId,
                       String startTime, String endTime) {
        // 时间戳字符串转 LocalDateTime
        if (startTime != null && !startTime.isEmpty()) {
            startTime = convertTimestamp(startTime);
        }
        if (endTime != null && !endTime.isEmpty()) {
            endTime = convertTimestamp(endTime);
        }

        PageHelper.startPage(pageNum, pageSize);
        List<Map<String, Object>> list = nursingTaskMapper.selectPage(
                status, elderName, nurseId, projectId, startTime, endTime);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);

        // 批量查询护理员姓名
        fillNursingNames(list);

        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("pages", pageInfo.getPages());
        data.put("total", pageInfo.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        return Result.ok(data);
    }

    /** 为任务列表填充护理员姓名 */
    private void fillNursingNames(List<Map<String, Object>> list) {
        if (list == null || list.isEmpty()) return;

        // 收集所有 elderName 对应的 elderId
        // 通过 elder 姓名查询 elder ID（批量）
        Set<String> elderNames = list.stream()
                .map(m -> (String) m.get("elderName"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (elderNames.isEmpty()) return;

        // 查询所有在住老人
        List<com.zhuimeng.entity.Elder> allElders = familyMapper.selectAllElders(null, null);
        Map<String, Long> nameToIdMap = new HashMap<>();
        List<Long> elderIds = new ArrayList<>();
        for (com.zhuimeng.entity.Elder elder : allElders) {
            if (elderNames.contains(elder.getName())) {
                nameToIdMap.put(elder.getName(), elder.getId());
                elderIds.add(elder.getId());
            }
        }

        if (elderIds.isEmpty()) return;

        // 批量查询护理员
        List<Map<String, Object>> nursingRows = familyMapper.selectNursingByElderIds(elderIds);
        Map<Long, List<String>> elderNurseNamesMap = new HashMap<>();
        for (Map<String, Object> row : nursingRows) {
            Long elderId = Long.valueOf(row.get("elderId").toString());
            String realName = (String) row.get("realName");
            elderNurseNamesMap.computeIfAbsent(elderId, k -> new ArrayList<>()).add(realName);
        }

        // 按 elderName 回填 nursingName
        Map<String, List<String>> nameToNurseMap = new HashMap<>();
        for (Map.Entry<String, Long> entry : nameToIdMap.entrySet()) {
            List<String> names = elderNurseNamesMap.get(entry.getValue());
            if (names != null) {
                nameToNurseMap.put(entry.getKey(), names);
            }
        }

        for (Map<String, Object> row : list) {
            String elderName = (String) row.get("elderName");
            row.put("nursingName", nameToNurseMap.getOrDefault(elderName, Collections.emptyList()));
        }
    }

    /** 执行/完成护理任务 */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result doTask(Long taskId, Long estimatedServerTime, String mark, String taskImage) {
        if (taskId == null) {
            return Result.fail("任务ID不能为空");
        }

        String userId = UserHolder.getId();

        // 1. 更新 nursing_task（执行信息）
        nursingTaskMapper.updateTaskDone(taskId, estimatedServerTime, mark, taskImage, Long.valueOf(userId));

        // 2. 同步更新订单状态为已执行（status=2）
        nursingTaskMapper.updateOrderStatusByTask(taskId, 2);

        // 3. 清除缓存
        redisTemplate.delete(key + "*");

        return Result.ok("执行成功");
    }

    /** 取消护理任务（自动退款） */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result cancel(Long taskId, String reason) {
        if (taskId == null) {
            return Result.fail("任务ID不能为空");
        }
        if (reason == null || reason.isBlank()) {
            return Result.fail("取消原因不能为空");
        }

        String userId = UserHolder.getId();

        // 1. 查询任务关联的订单号
        Map<String, Object> taskInfo = nursingTaskMapper.selectDetail(taskId);
        if (taskInfo == null) {
            return Result.fail("任务不存在");
        }
        String relNo = (String) taskInfo.get("relNo");

        // 2. 取消任务并自动退款（订单status→5，refund=amount，is_refund='YES'）
        nursingTaskMapper.updateTaskCancel(taskId, reason, Long.valueOf(userId));

        // 3. 清除订单Redis缓存
        if (relNo != null) {
            clearOrderCache(relNo);
        }
        // 4. 清除任务缓存
        redisTemplate.delete(key + "*");

        return Result.ok("取消成功，已自动退款");
    }

    /** 清除订单相关Redis缓存 */
    private void clearOrderCache(String orderNo) {
        // 清除详情缓存
        Set<String> keys = redisTemplate.keys(RedisKeyProperties.ORDER_LIST + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /** 修改执行日期 */
    @Override
    public Result updateTime(Long taskId, Long estimatedServerTime) {
        if (taskId == null) {
            return Result.fail("任务ID不能为空");
        }
        if (estimatedServerTime == null) {
            return Result.fail("执行日期不能为空");
        }

        nursingTaskMapper.updateTaskTime(taskId, estimatedServerTime);

        redisTemplate.delete(key + "*");

        return Result.ok("修改成功");
    }

    /** 任务详情 */
    @Override
    public Result detail(Long taskId) {
        if (taskId == null) {
            return Result.fail("任务ID不能为空");
        }

        Map<String, Object> detail = nursingTaskMapper.selectDetail(taskId);
        if (detail == null) {
            return Result.fail("任务不存在");
        }

        // 查询护理员姓名
        String elderName = (String) detail.get("elderName");
        if (elderName != null) {
            List<com.zhuimeng.entity.Elder> elders = familyMapper.selectAllElders(elderName, null);
            if (!elders.isEmpty()) {
                Long elderId = elders.get(0).getId();
                List<Map<String, Object>> nursingRows = familyMapper.selectNursingByElderIds(
                        Collections.singletonList(elderId));
                List<String> nurseNames = nursingRows.stream()
                        .map(r -> (String) r.get("realName"))
                        .collect(Collectors.toList());
                detail.put("nursingName", nurseNames);
            }
        }

        return Result.ok(detail);
    }

    /** 毫秒时间戳转 yyyy-MM-dd HH:mm:ss 字符串 */
    private String convertTimestamp(String timestamp) {
        try {
            long ms = Long.parseLong(timestamp);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault())
                    .toString().replace("T", " ");
        } catch (NumberFormatException e) {
            return timestamp;
        }
    }
}
