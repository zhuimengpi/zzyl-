package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 护理计划表
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NursingPlan {

    /**
     * 编号
     */
    private Long id;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 名称
     */
    private String planName;

    /**
     * 状态（0：禁用，1：启用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /** 老修改时间
    * */
    private LocalDateTime oldUpdateTime;

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

    /** 项目计划列表 */
    private List<ProjectPlan> projectPlans;
}
