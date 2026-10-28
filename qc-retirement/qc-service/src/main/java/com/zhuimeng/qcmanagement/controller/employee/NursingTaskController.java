package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.qcmanagement.service.NursingTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 护理任务
* */

@RestController
@RequestMapping("/sysUser/nursingTask")
public class NursingTaskController {
    @Autowired
    private NursingTaskService nursingTaskService;

    /** 分页查询护理任务列表
     * 查询条件：elderName, nurseId, projectId, status, startTime, endTime
     */
    @GetMapping("/page")
    public Result page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String elderName,
            @RequestParam(required = false) Long nurseId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) {
        return nursingTaskService.page(pageNum, pageSize, status, elderName, nurseId, projectId, startTime, endTime);
    }

    /** 执行/完成指定护理任务，可上传任务图片
     */
    @PutMapping("/do")
    public Result doTask(
            @RequestParam Long taskId,
            @RequestParam Long estimatedServerTime,
            @RequestParam(required = false) String mark,
            @RequestParam(required = false) String taskImage
    ) {
        return nursingTaskService.doTask(taskId, estimatedServerTime, mark, taskImage);
    }

    /** 取消指定护理任务并填写原因
     */
    @PutMapping("/cancel")
    public Result cancel(
            @RequestParam Long taskId,
            @RequestParam String reason
    ) {
        return nursingTaskService.cancel(taskId, reason);
    }

    /** 修改护理任务的计划执行日期
     */
    @PutMapping("/updateTime")
    public Result updateTime(
            @RequestParam Long taskId,
            @RequestParam Long estimatedServerTime
    ) {
        return nursingTaskService.updateTime(taskId, estimatedServerTime);
    }

    /** 查看护理任务详情
     */
    @GetMapping
    public Result detail(@RequestParam(required = false) Long taskId) {
        return nursingTaskService.detail(taskId);
    }
}
