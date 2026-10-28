package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.BillPayDTO;
import com.zhuimeng.entity.Bill;

import java.util.Map;

public interface BillService {

    /** 分页查询账单（家庭端） */
    Result billList(Integer pageNum, Integer pageSize, Integer transactionStatus, Integer elderId);

    /** 分页查询账单（员工端，多条件） */
    Result billPage(Integer pageNum, Integer pageSize, Integer transactionStatus,
                    String billNo, String elderName, String elderIdCard);

    /** 账单详情 */
    Result billDetail(Long id);

    /** 支付账单 */
    Result payBill(BillPayDTO billPayDTO);

    /** 创建账单 */
    Result createBill(Bill bill);

    /** 取消账单 */
    Result cancelBill(Long id);

    /** 欠费查询 */
    Result arrearList(Integer pageNum, Integer pageSize, String elderName);

    /** 分页查询预付费充值记录 */
    Result prepaidRechargePage(Integer pageNum, Integer pageSize, String elderName);

    /** 创建预付费充值记录 */
    Result createPrepaidRecharge(Map<String, Object> params);

    /** 余额分页查询 */
    Result balancePage(Integer pageNum, Integer pageSize, String elderName);

    /** 退住费用清算 */
    Result retreatSettlement(Long elderId, String checkOutTime);
}
