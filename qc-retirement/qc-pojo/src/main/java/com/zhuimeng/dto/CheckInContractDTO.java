package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 入住申请 - 签约合同
* */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckInContractDTO {

    /** 合同名称 */
    private String name;

    /** 签约日期 */
    private String signDate;

    /** 丙方姓名 */
    private String memberName;

    /** 丙方手机号 */
    private String memberPhone;

    /** 合同PDF地址 */
    private String pdfUrl;

    /** 备注 */
    private String remark;
}
