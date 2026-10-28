package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;

/** 护理任务 Service
* */
public interface NursingTaskService {

    /** 分页查询护理任务列表 */
    Result page(Integer pageNum, Integer pageSize, Integer status, String elderName,
                Long nurseId, Long projectId, String startTime, String endTime);

    /** 执行/完成护理任务 */
    Result doTask(Long taskId, Long estimatedServerTime, String mark, String taskImage);

    /** 取消护理任务 */
    Result cancel(Long taskId, String reason);

    /** 修改执行日期 */
    Result updateTime(Long taskId, Long estimatedServerTime);

    /** 任务详情 */
    Result detail(Long taskId);
}
