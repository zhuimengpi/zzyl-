package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 入住申请 - 家属信息
* */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElderFamilyDTO {

    /** 家属姓名 */
    private String name;

    /** 家属手机号 */
    private String phone;

    /** 与老人关系 */
    private String kinship;
}
