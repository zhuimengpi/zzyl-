package com.zhuimeng.qcmanagement.controller.family;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.BillPayDTO;
import com.zhuimeng.qcmanagement.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 账单管理
* */

@RestController
@RequestMapping("/customer/bill")
public class BillController {
    @Autowired
    private BillService billService;

    /** 账单列表
    * */
    @GetMapping({"/page", "/page/"})
    public Result billList(Integer pageNum, Integer pageSize, Integer transactionStatus, Integer elderId) {
        return billService.billList(pageNum, pageSize, transactionStatus, elderId);
    }

    /** 账单详情
     */
    @GetMapping("/{id}")
    public Result billDetail(@PathVariable Long id) {
        return billService.billDetail(id);
    }

    /** 账单支付
    * */
    @PutMapping()
    public Result payBill(@RequestBody BillPayDTO billPayDTO) {
        return billService.payBill(billPayDTO);
    }
}
