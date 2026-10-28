package com.zhuimeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 老人信息 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ElderVO {

    /** 老人ID */
    private Long id;

    /** 老人姓名 */
    private String name;

    /** 老人身份证号 */
    private String idCardNo;

    /** 老人头像 */
    private String image;
}
