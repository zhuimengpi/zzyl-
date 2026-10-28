package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 护理计划和项目关联表
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProjectPlan {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 计划ID
     */
    private Long planId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 计划执行时间
     */
    private String executeTime;

    /**
     * 执行周期（0：天，1：周，2：月）
     */
    private Integer executeCycle;

    /**
     * 执行频次
     */
    private Integer executeFrequency;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

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
}
