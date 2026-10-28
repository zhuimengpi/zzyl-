package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.qcmanagement.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 合同管理
* */

@RestController
@RequestMapping("/sysUser/contract")
public class ContractManagementController {
    @Autowired
    private ContractService contractService;

    /** 合同列表
    * */
    @GetMapping("/list")
    public Result contractList(
            @RequestParam(required = true) Integer pageNum,
            @RequestParam(required = true) Integer pageSize,
            @RequestParam(required = false) String contractNo,
            @RequestParam(required = false) String elderName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime
    ) {
        return contractService.contractList(pageNum, pageSize, contractNo, elderName, status, startTime, endTime);
    }

    /** 合同详情
    * */
    @GetMapping("{id}")
    public Result contractDetail(@PathVariable Long id) {
        return contractService.contractDetail(id);
    }
}
