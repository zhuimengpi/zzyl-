package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 入住申请 - 入住配置
* */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckInConfigDTO {

    /** 床位ID */
    private Long bedId;

    /** 床位费用 */
    private BigDecimal bedCost;

    /** 入住开始时间 */
    private String checkInStartTime;

    /** 入住结束时间 */
    private String checkInEndTime;

    /** 房间编号 */
    private String code;

    /** 房间ID */
    private Long roomId;

    /** 楼层ID */
    private Long floorId;

    /** 楼层名称 */
    private String floorName;

    /** 费用开始时间 */
    private String costStartTime;

    /** 费用结束时间 */
    private String costEndTime;

    /** 押金 */
    private BigDecimal depositAmount;

    /** 政府补贴 */
    private BigDecimal governmentSubsidy;

    /** 医保支付 */
    private BigDecimal medicalInsurancePayment;

    /** 护理费用 */
    private BigDecimal nursingCost;

    /** 护理等级ID */
    private Long nursingLevelId;

    /** 护理等级名称 */
    private String nursingLevelName;

    /** 其他费用 */
    private BigDecimal otherCost;
}
