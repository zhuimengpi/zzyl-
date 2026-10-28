package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.Bill;
import com.zhuimeng.entity.Balance;
import com.zhuimeng.qcmanagement.annotation.AutoUpdata;
import com.zhuimeng.vo.BillVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface BillMapper {

    /** 查询账单列表 */
    ArrayList<Bill> billList(Integer transactionStatus, Integer elderId);

    /** 查询账单列表（含老人姓名） */
    ArrayList<BillVO> selectBillVOList(Integer transactionStatus, Integer elderId);

    /** 分页查询账单（含老人姓名+身份证号，支持多条件筛选） */
    List<Map<String, Object>> selectBillPage(@Param("transactionStatus") Integer transactionStatus,
                                              @Param("billNo") String billNo,
                                              @Param("elderName") String elderName,
                                              @Param("elderIdCard") String elderIdCard);

    /** 查询账单详情（含入住配置） */
    BillVO selectBillVODetail(Long id);

    /** 根据ID查询账单 */
    Bill selectById(Long id);

    /** 插入账单 */
    void insertBill(Bill bill);

    /** 更新账单 */
    @AutoUpdata
    void updateBill(Bill bill);

    // ==================== 欠费查询 ====================

    /** 查询欠费老人列表（聚合欠费金额） */
    List<Map<String, Object>> selectArrearsList(@Param("elderName") String elderName);

    // ==================== 预付费充值记录 ====================

    /** 分页查询预付费充值记录 */
    List<Map<String, Object>> selectPrepaidRechargePage(@Param("elderName") String elderName);

    /** 插入预付费充值记录 */
    void insertPrepaidRecharge(Map<String, Object> params);

    // ==================== 余额 ====================

    /** 分页查询老人账户余额 */
    List<Map<String, Object>> selectBalancePage(@Param("elderName") String elderName);

    // ==================== 退住清算 ====================

    /** 查询老人未支付的月度账单（欠费） */
    List<Map<String, Object>> selectArrearsBills(@Param("elderId") Long elderId);

    /** 查询老人未支付的服务账单（未缴） */
    List<Map<String, Object>> selectUnpaidBills(@Param("elderId") Long elderId);

    /** 查询老人可退款的月度账单（已付但未住满天数的） */
    List<Map<String, Object>> selectDueBackBills(@Param("elderId") Long elderId,
                                                   @Param("checkOutTime") String checkOutTime);

    /** 查询老人余额 */
    Map<String, Object> selectBalanceByElderId(@Param("elderId") Long elderId);

    /** 更新余额-预缴款充值 */
    int updatePrepaidBalance(@Param("elderId") Long elderId,
                             @Param("elderName") String elderName,
                             @Param("amount") java.math.BigDecimal amount);
}
