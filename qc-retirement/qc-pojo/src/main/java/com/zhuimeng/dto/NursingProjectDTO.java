package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 护理项目DTO
 * */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NursingProjectDTO {

    /** 主键ID */
    private Long id;

    /** 名称 */
    private String name;

    /** 价格 */
    private BigDecimal price;

    /** 单位 */
    private String unit;

    /** 护理要求 */
    private String nursingRequirement;

    /** 排序号
    * */
    private Integer orderNo;

    /** 图片 */
    private String image;
}
