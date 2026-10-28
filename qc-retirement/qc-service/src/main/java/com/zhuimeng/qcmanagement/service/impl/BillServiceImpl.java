package com.zhuimeng.qcmanagement.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhuimeng.Result.Result;
import com.zhuimeng.Thread.UserHolder;
import com.zhuimeng.dto.BillPayDTO;
import com.zhuimeng.entity.Bill;
import com.zhuimeng.entity.CheckInConfig;
import com.zhuimeng.entity.Order;
import com.zhuimeng.qcmanagement.mapper.BillMapper;
import com.zhuimeng.qcmanagement.mapper.FamilyMapper;
import com.zhuimeng.qcmanagement.mapper.OrderMapper;
import com.zhuimeng.qcmanagement.service.BillService;
import com.zhuimeng.vo.BillVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BillServiceImpl implements BillService {
    @Autowired
    private BillMapper billMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private FamilyMapper familyMapper;

    /** 分页查询账单（家庭端，按 elderId 筛选） */
    @Override
    public Result billList(Integer pageNum, Integer pageSize, Integer transactionStatus, Integer elderId) {
        PageHelper.startPage(pageNum, pageSize);
        ArrayList<BillVO> billPage = billMapper.selectBillVOList(transactionStatus, elderId);
        PageInfo<BillVO> billPageInfo = new PageInfo<>(billPage);
        Map<String, Object> data = new HashMap<>();
        data.put("records", billPage);
        data.put("pages", billPageInfo.getPages());
        return Result.ok(data);
    }

    /** 分页查询账单（员工端，多条件） */
    @Override
    public Result billPage(Integer pageNum, Integer pageSize, Integer transactionStatus,
                           String billNo, String elderName, String elderIdCard) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map<String, Object>> list = billMapper.selectBillPage(
                transactionStatus, billNo, elderName, elderIdCard);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);

        // 组装嵌套 elderVo 供前端 colKey="elderVo.name" 取值
        for (Map<String, Object> row : list) {
            Map<String, Object> elderVo = new HashMap<>();
            elderVo.put("name", row.remove("elderName"));
            elderVo.put("idCardNo", row.remove("elderIdCardNo"));
            row.put("elderVo", elderVo);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("pages", pageInfo.getPages());
        data.put("total", pageInfo.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.ok(data);
    }

    /** 账单详情 */
    @Override
    public Result billDetail(Long id) {
        BillVO bill = billMapper.selectBillVODetail(id);
        if (bill == null) {
            return Result.fail("账单不存在");
        }
        return Result.ok(bill);
    }

    /** 支付账单 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result payBill(BillPayDTO billPayDTO) {
        Long billId = billPayDTO.getEffectiveId();
        if (billId == null) {
            return Result.fail("账单ID不能为空");
        }
        Bill bill = billMapper.selectById(billId);
        if (bill == null) {
            return Result.fail("账单不存在");
        }
        if (bill.getTransactionStatus() != 0) {
            return Result.fail("账单状态异常，无法支付");
        }

        bill.setTransactionStatus(1);
        billMapper.updateBill(bill);

        // 如果是服务账单，同步更新关联订单
        if (bill.getBillType() == 1 && bill.getTradingOrderNo() != null && bill.getTradingOrderNo() > 0) {
            Order order = orderMapper.selectByTradingOrderNo(bill.getTradingOrderNo());
            if (order != null && order.getPaymentStatus() == 1) {
                order.setPaymentStatus(2);
                order.setStatus(1);
                orderMapper.updateOrder(order);
            }
        }

        return Result.ok("支付成功");
    }

    /** 创建账单（自动从入住配置计算金额） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createBill(Bill bill) {
        if (bill.getElderId() == null) {
            return Result.fail("老人ID不能为空");
        }
        if (bill.getBillMonth() == null || bill.getBillMonth().isBlank()) {
            return Result.fail("账单月份不能为空");
        }

        // 生成账单编号
        bill.setBillNo("BL-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%03d", new Random().nextInt(1000)));

        if (bill.getBillType() == null) {
            bill.setBillType(0);
        }

        // 从入住配置自动计算账单金额
        CheckInConfig config = familyMapper.selectCheckInConfigByElderId(bill.getElderId());
        if (config != null) {
            BigDecimal nursingCost = config.getNursingCost() != null ? config.getNursingCost() : BigDecimal.ZERO;
            BigDecimal bedCost = config.getBedCost() != null ? config.getBedCost() : BigDecimal.ZERO;
            BigDecimal otherCost = config.getOtherCost() != null ? config.getOtherCost() : BigDecimal.ZERO;
            BigDecimal medicalPayment = config.getMedicalInsurancePayment() != null ? config.getMedicalInsurancePayment() : BigDecimal.ZERO;
            BigDecimal subsidy = config.getGovernmentSubsidy() != null ? config.getGovernmentSubsidy() : BigDecimal.ZERO;
            BigDecimal deposit = config.getDepositAmount() != null ? config.getDepositAmount() : BigDecimal.ZERO;

            BigDecimal currentCost = nursingCost.add(bedCost).add(otherCost)
                    .subtract(medicalPayment).subtract(subsidy);
            bill.setCurrentCost(currentCost);
            bill.setDepositAmount(deposit);
            bill.setBillAmount(currentCost.add(deposit));
            bill.setPayableAmount(currentCost.add(deposit));
            bill.setPrepaidAmount(BigDecimal.ZERO);

            // 根据账单月份计算起止日期和天数
            try {
                java.time.YearMonth ym = java.time.YearMonth.parse(bill.getBillMonth());
                LocalDateTime startTime = ym.atDay(1).atStartOfDay();
                LocalDateTime endTime = ym.atEndOfMonth().atTime(23, 59, 59);
                bill.setBillStartTime(startTime);
                bill.setBillEndTime(endTime);
                bill.setTotalDays(ym.lengthOfMonth());
                bill.setPaymentDeadline(startTime.plusDays(15));
            } catch (Exception e) {
                bill.setPaymentDeadline(LocalDateTime.now().plusDays(15));
            }

            bill.setLname(config.getNursingLevelId() != null ? String.valueOf(config.getNursingLevelId()) : null);
            bill.setTypeName(config.getBedNo());
        } else {
            // 无入住配置时设默认0
            bill.setCurrentCost(BigDecimal.ZERO);
            bill.setDepositAmount(BigDecimal.ZERO);
            bill.setBillAmount(BigDecimal.ZERO);
            bill.setPayableAmount(BigDecimal.ZERO);
            bill.setPrepaidAmount(BigDecimal.ZERO);
            bill.setPaymentDeadline(LocalDateTime.now().plusDays(15));
        }

        bill.setTransactionStatus(0);
        bill.setCreateTime(LocalDateTime.now());

        String userId = UserHolder.getId();
        bill.setCreateBy(userId != null ? Long.valueOf(userId) : null);

        billMapper.insertBill(bill);
        return Result.ok("创建成功");
    }

    /** 取消账单 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result cancelBill(Long id) {
        Bill bill = billMapper.selectById(id);
        if (bill == null) {
            return Result.fail("账单不存在");
        }
        if (bill.getTransactionStatus() == 2) {
            return Result.fail("账单已关闭");
        }

        bill.setTransactionStatus(2);
        billMapper.updateBill(bill);
        return Result.ok("取消成功");
    }

    /** 欠费查询 */
    @Override
    public Result arrearList(Integer pageNum, Integer pageSize, String elderName) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map<String, Object>> list = billMapper.selectArrearsList(elderName);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);

        // 组装嵌套对象供前端 colKey 取值
        for (Map<String, Object> row : list) {
            Map<String, Object> elderVo = new HashMap<>();
            elderVo.put("name", row.remove("elderName"));
            row.put("elderVo", elderVo);

            Map<String, Object> bedVo = new HashMap<>();
            bedVo.put("bedNumber", row.remove("bedNumber"));
            row.put("bedVo", bedVo);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("pages", pageInfo.getPages());
        data.put("total", pageInfo.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.ok(data);
    }

    /** 分页查询预付费充值记录 */
    @Override
    public Result prepaidRechargePage(Integer pageNum, Integer pageSize, String elderName) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map<String, Object>> list = billMapper.selectPrepaidRechargePage(elderName);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);

        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("pages", pageInfo.getPages());
        data.put("total", pageInfo.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.ok(data);
    }

    /** 创建预付费充值记录（同时更新余额） */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createPrepaidRecharge(Map<String, Object> params) {
        Object elderIdObj = params.get("elderId");
        if (elderIdObj == null) {
            return Result.fail("老人ID不能为空");
        }
        Long elderId = Long.valueOf(elderIdObj.toString());

        Object amountObj = params.get("rechargeAmount");
        if (amountObj == null) {
            return Result.fail("充值金额不能为空");
        }
        BigDecimal amount = new BigDecimal(amountObj.toString());

        // 1. 插入充值记录
        String rechargeNo = "P" + System.currentTimeMillis();
        params.put("prepaidRechargeNo", rechargeNo);
        String userId = UserHolder.getId();
        params.put("createBy", userId != null ? Long.valueOf(userId) : null);
        billMapper.insertPrepaidRecharge(params);

        // 2. 更新余额：不存在则先创建
        int updated = billMapper.updatePrepaidBalance(elderId, null, amount);
        if (updated == 0) {
            // 余额账户不存在，先创建再更新
            com.zhuimeng.entity.Elder elder = familyMapper.selectElderById(elderId);
            String elderName = elder != null ? elder.getName() : "";
            familyMapper.insertBalance(elderId, elderName);
            billMapper.updatePrepaidBalance(elderId, null, amount);
        }

        return Result.ok("充值成功");
    }

    /** 余额分页查询 */
    @Override
    public Result balancePage(Integer pageNum, Integer pageSize, String elderName) {
        PageHelper.startPage(pageNum, pageSize);
        List<Map<String, Object>> list = billMapper.selectBalancePage(elderName);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);

        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("pages", pageInfo.getPages());
        data.put("total", pageInfo.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.ok(data);
    }

    /** 退住费用清算 */
    @Override
    public Result retreatSettlement(Long elderId, String checkOutTime) {
        if (elderId == null) {
            return Result.fail("老人ID不能为空");
        }
        if (checkOutTime == null || checkOutTime.isBlank()) {
            return Result.fail("退住日期不能为空");
        }

        // 1. 欠费列表（未支付的月度账单）
        List<Map<String, Object>> arrearageList = billMapper.selectArrearsBills(elderId);

        // 2. 未缴列表（未支付的服务账单）
        List<Map<String, Object>> unpaidList = billMapper.selectUnpaidBills(elderId);

        // 3. 应退列表（已支付但未住满天数的月度账单）
        List<Map<String, Object>> dueBackList = billMapper.selectDueBackBills(elderId, checkOutTime);

        // 4. 余额
        Map<String, Object> balanceVo = billMapper.selectBalanceByElderId(elderId);
        if (balanceVo == null) {
            balanceVo = new HashMap<>();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("arrearageList", arrearageList != null ? arrearageList : Collections.emptyList());
        data.put("unpaidList", unpaidList != null ? unpaidList : Collections.emptyList());
        data.put("dueBackList", dueBackList != null ? dueBackList : Collections.emptyList());
        data.put("balanceVo", balanceVo);

        return Result.ok(data);
    }
}
