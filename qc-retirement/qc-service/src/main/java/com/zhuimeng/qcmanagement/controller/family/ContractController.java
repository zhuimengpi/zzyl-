package com.zhuimeng.qcmanagement.controller.family;

import com.zhuimeng.Result.Result;
import com.zhuimeng.qcmanagement.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 合同管理（家属端）
* */

@RestController
@RequestMapping("/customer/contract")
public class ContractController {
    @Autowired
    private ContractService contractService;

    /** 合同分页查询
    * */
    @GetMapping("/list")
    public Result contractList(Integer page, Integer pageSize) {
        return contractService.contractList(page, pageSize, null, null, null, null, null);
    }

    /** 合同详情（含老人信息、房间床位、护理等级、合同日期、PDF地址）
    * */
    @GetMapping("/{id}")
    public Result contractDetail(@PathVariable Long id) {
        return contractService.contractDetail(id);
    }
}
