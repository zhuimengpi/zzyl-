package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 部门表
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Dept {

    /**
     * 部门id
     */
    private Long id;

    /**
     * 父部门编号
     */
    private String parentDeptNo;

    /**
     * 部门编号
     */
    private String deptNo;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 排序
     */
    private Integer sortNo;

    /**
     * 数据状态（0正常 1停用）
     */
    private String dataState;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    private Long createBy;

    /**
     * 更新者
     */
    private Long updateBy;

    /**
     * 负责人Id
     */
    private Long leaderId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 旧更新时间（乐观锁）
     */
    private LocalDateTime oldUpdateTime;
}
