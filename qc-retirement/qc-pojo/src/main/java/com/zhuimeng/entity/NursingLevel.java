package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 护理等级表
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NursingLevel {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 等级名称
     */
    private String name;

    /**
     * 护理计划ID
     */
    private Integer lplanId;

    /**
     * 护理费用
     */
    private BigDecimal fee;

    /**
     * 状态（0：禁用，1：启用）
     */
    private Integer status;

    /**
     * 等级说明
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 备注
     */
    private String remark;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /** 老修改时间
    * */
    private LocalDateTime oldUpdateTime;
}
