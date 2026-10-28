package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.CheckInApplyDTO;
import com.zhuimeng.dto.RetreatApplyDTO;
import com.zhuimeng.qcmanagement.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 入退住管理
* */

@RestController
@RequestMapping("/sysUser")
public class CheckInController {
    @Autowired
    private CheckInService checkInService;

    //--------------------------------入住---------------------------------------------

    /** 入住申请，分页查询（V2）
     * 查询条件：status, elderName, startTime, endTime
    * */
    @GetMapping("/check-in/pageQuery")
    public Result pageQuery(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String elderName,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) {
        return checkInService.pageQuery(pageNum, pageSize, status, elderName, startTime, endTime);
    }

    /** 申请入住（V2）
     * 包含：老人基本信息、家属信息、入住配置、合同信息
    * */
    @PostMapping("/check-in/apply")
    public Result apply(@RequestBody CheckInApplyDTO dto) {
        return checkInService.apply(dto);
    }

    /** 入住详情（V2）
    * */
    @GetMapping("/check-in/detail/{id}")
    public Result detail(@PathVariable Long id) {
        return checkInService.detail(id);
    }

    //--------------------------------退住---------------------------------------------

    /** 退住申请，分页查询（V2）
     * 查询条件：elderName, elderIdCardNo, retreatStartTime, retreatEndTime
    * */
    @GetMapping("/retreat/pageQuery")
    public Result retreatPageQuery(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String elderName,
            @RequestParam(required = false) String elderIdCardNo,
            @RequestParam(required = false) String retreatStartTime,
            @RequestParam(required = false) String retreatEndTime
    ) {
        return checkInService.retreatPageQuery(pageNum, pageSize, elderName, elderIdCardNo, retreatStartTime, retreatEndTime);
    }

    /** 退住详情（V2）
    * */
    @GetMapping("/retreat/detail/{id}")
    public Result retreatDetail(@PathVariable Long id) {
        return checkInService.retreatDetail(id);
    }

    /** 申请退住（V2）
     * 包含：退住老人信息、账单数据、解除合同、退款凭证
    * */
    @PostMapping("/retreat/apply")
    public Result retreatApply(@RequestBody RetreatApplyDTO dto) {
        return checkInService.retreatApply(dto);
    }

}
