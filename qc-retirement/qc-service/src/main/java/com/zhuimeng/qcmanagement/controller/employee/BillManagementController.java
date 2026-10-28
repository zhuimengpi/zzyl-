package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.BillPayDTO;
import com.zhuimeng.entity.Bill;
import com.zhuimeng.qcmanagement.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 财务管理 - 账单
* */

@RestController
@RequestMapping("/sysUser/bill")
public class BillManagementController {
    @Autowired
    private BillService billService;

    /** 账单分页查询 */
    @GetMapping("/page/")
    public Result billPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer transactionStatus,
            @RequestParam(required = false) String billNo,
            @RequestParam(required = false) String elderName,
            @RequestParam(required = false) String elderIdCard
    ) {
        return billService.billPage(pageNum, pageSize, transactionStatus, billNo, elderName, elderIdCard);
    }

    /** 欠费查询 */
    @GetMapping("/arrears/")
    public Result arrearList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String elderName
    ) {
        return billService.arrearList(pageNum, pageSize, elderName);
    }

    /** 预付费充值记录分页查询 */
    @GetMapping("/prepaidRechargeRecord/page")
    public Result prepaidRechargePage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String elderName
    ) {
        return billService.prepaidRechargePage(pageNum, pageSize, elderName);
    }

    /** 余额分页查询 */
    @GetMapping("/balance/")
    public Result balancePage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String elderName
    ) {
        return billService.balancePage(pageNum, pageSize, elderName);
    }

    /** 支付账单 */
    @PostMapping("/payRecord")
    public Result payBill(@RequestBody BillPayDTO billPayDTO) {
        return billService.payBill(billPayDTO);
    }

    /** 创建账单 */
    @PostMapping
    public Result createBill(@RequestBody Bill bill) {
        return billService.createBill(bill);
    }

    /** 取消账单 */
    @PutMapping("/cancel/{id}")
    public Result cancelBill(@PathVariable Long id) {
        return billService.cancelBill(id);
    }

    /** 账单详情 */
    @GetMapping("/{id}")
    public Result billDetail(@PathVariable Long id) {
        return billService.billDetail(id);
    }

    /** 创建预付费充值记录 */
    @PostMapping("/prepaidRechargeRecord")
    public Result createPrepaidRecharge(@RequestBody Map<String, Object> params) {
        return billService.createPrepaidRecharge(params);
    }

    /** 退住费用清算 */
    @GetMapping("/retreatSettlement")
    public Result retreatSettlement(
            @RequestParam Long elderId,
            @RequestParam String checkOutTime
    ) {
        return billService.retreatSettlement(elderId, checkOutTime);
    }
}
