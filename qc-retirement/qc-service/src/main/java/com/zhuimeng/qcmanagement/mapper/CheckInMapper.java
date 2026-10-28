package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.CheckIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/** 入住管理 Mapper
* */
@Mapper
public interface CheckInMapper {

    /** 分页查询入住申请列表（V2） */
    List<Map<String, Object>> selectPageQuery(@Param("status") Integer status,
                                              @Param("elderName") String elderName,
                                              @Param("startTime") String startTime,
                                              @Param("endTime") String endTime);

    /** 根据ID查询入住详情 */
    Map<String, Object> selectDetailById(@Param("id") Long id);

    /** 插入入住申请表 */
    void insertCheckIn(CheckIn checkIn);

    /** 插入入住配置 */
    void insertCheckInConfig(@Param("elderId") Long elderId,
                             @Param("bedNo") String bedNo,
                             @Param("nursingLevelId") Long nursingLevelId,
                             @Param("checkInStartTime") String checkInStartTime,
                             @Param("checkInEndTime") String checkInEndTime,
                             @Param("costStartTime") String costStartTime,
                             @Param("costEndTime") String costEndTime,
                             @Param("depositAmount") java.math.BigDecimal depositAmount,
                             @Param("nursingCost") java.math.BigDecimal nursingCost,
                             @Param("bedCost") java.math.BigDecimal bedCost,
                             @Param("otherCost") java.math.BigDecimal otherCost,
                             @Param("medicalInsurancePayment") java.math.BigDecimal medicalInsurancePayment,
                             @Param("governmentSubsidy") java.math.BigDecimal governmentSubsidy);

    /** 插入合同 */
    void insertContract(@Param("elderId") Long elderId,
                        @Param("elderName") String elderName,
                        @Param("name") String name,
                        @Param("contractNo") String contractNo,
                        @Param("signDate") String signDate,
                        @Param("startTime") String startTime,
                        @Param("endTime") String endTime,
                        @Param("memberName") String memberName,
                        @Param("memberPhone") String memberPhone,
                        @Param("pdfUrl") String pdfUrl,
                        @Param("levelDesc") String levelDesc,
                        @Param("checkInNo") String checkInNo,
                        @Param("status") Integer status,
                        @Param("createBy") Long createBy);

    // ==================== 退住 ====================

    /** 分页查询退住申请列表（V2） */
    List<Map<String, Object>> selectRetreatPageQuery(@Param("elderName") String elderName,
                                                      @Param("elderIdCardNo") String elderIdCardNo,
                                                      @Param("retreatStartTime") String retreatStartTime,
                                                      @Param("retreatEndTime") String retreatEndTime);

    /** 根据ID查询退住详情 */
    Map<String, Object> selectRetreatDetailById(@Param("id") Long id);

    /** 插入退住记录 */
    void insertRetreat(Map<String, Object> params);

    /** 插入退住账单 */
    void insertRetreatBill(@Param("retreatId") Long retreatId,
                           @Param("billJson") String billJson,
                           @Param("refundVoucherUrl") String refundVoucherUrl,
                           @Param("tradingChannel") String tradingChannel,
                           @Param("elderId") Long elderId,
                           @Param("isRefund") Integer isRefund);

    /** 更新合同解除信息 */
    void updateContractRelease(@Param("elderId") Long elderId,
                               @Param("releaseDate") String releaseDate,
                               @Param("releasePdfUrl") String releasePdfUrl,
                               @Param("releaseSubmitter") String releaseSubmitter);
}
