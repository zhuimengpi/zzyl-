package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 老人数据传入
* */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElderDTO {

    /** 老人姓名 */
    private String name;

    /** 身份证号 */
    private String idCard;

    /** 与老人关系 */
    private String remark;
}
