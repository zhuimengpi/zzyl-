package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 护理等级DTO
 * */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NursingLevelDTO {

    /** 主键ID */
    private Long id;

    /** 等级名称 */
    private String name;

    /** 护理费用 */
    private BigDecimal fee;

    /** 等级说明 */
    private String description;

    /** 护理计划ID */
    private Integer planId;
}
