package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 退住申请 - 老人信息
* */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RetreatElderDTO {

    /** 老人ID */
    private Long id;

    /** 老人姓名 */
    private String name;

    /** 身份证号 */
    private String idCardNo;

    /** 手机号 */
    private String phone;

    /** 入住开始时间 */
    private String checkInStartTime;

    /** 入住结束时间 */
    private String checkInEndTime;

    /** 费用开始时间 */
    private String costStartTime;

    /** 费用结束时间 */
    private String costEndTime;

    /** 护理等级名称 */
    private String nursingLevelName;

    /** 床位编号 */
    private String bedNo;

    /** 床位号 */
    private String bedNumber;

    /** 护理员姓名 */
    private String nursingName;

    /** 退住日期 */
    private String checkOutTime;

    /** 退住原因 */
    private String reason;

    /** 家庭住址 */
    private String address;

    /** 床位ID */
    private Long bedId;
}
