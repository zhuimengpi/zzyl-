package com.zhuimeng.dto;

import com.zhuimeng.entity.ProjectPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** 护理计划DTO
 * */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NursingPlanDTO {

    /** 计划名称 */
    private String planName;

    /** 项目计划列表 */
    private List<ProjectPlan> projectPlans;

    /** 排序号 */
    private Integer sortNo;
}
