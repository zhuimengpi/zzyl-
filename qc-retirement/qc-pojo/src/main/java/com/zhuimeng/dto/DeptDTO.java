package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 部门DTO
* */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeptDTO {

    /** 主键ID */
    private Long id;

    /** 部门编号 */
    private String deptNo;

    /** 上级部门编号 */
    private String parentDeptNo;

    /** 部门名称 */
    private String deptName;

    /** 数据状态 */
    private String dataStatus;

    /** 排序 */
    private Integer sortNo;

    /** 负责人Id */
    private Long leaderId;

    /** 备注 */
    private String remark;
}
