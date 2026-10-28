package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 入住申请 - 老人基本信息
* */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckInElderDTO {

    /** 老人姓名 */
    private String name;

    /** 身份证号 */
    private String idCardNo;

    /** 手机号 */
    private String phone;

    /** 性别 */
    private String sex;

    /** 年龄 */
    private Integer age;

    /** 出生日期 */
    private String birthday;

    /** 地址 */
    private String address;

    /** 一寸照片 */
    private String oneInchPhoto;

    /** 身份证人像面 */
    private String idCardPortraitImg;

    /** 身份证国徽面 */
    private String idCardNationalEmblemImg;
}
