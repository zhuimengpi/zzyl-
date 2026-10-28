package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 岗位表
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Post {

    /**
     * 岗位ID
     */
    private Long id;

    /**
     * 部门编号
     */
    private String deptNo;

    /**
     * 岗位编码（父部门编号+01【2位】）
     */
    private String postNo;

    /**
     * 岗位名称
     */
    private String postName;

    /**
     * 显示顺序
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
     * 备注
     */
    private String remark;

    /**
     * 创建者
     */
    private Long createBy;

    /**
     * 更新者
     */
    private Long updateBy;

    /**
     * 旧更新时间（乐观锁）
     */
    private LocalDateTime oldUpdateTime;

    /**
     * 所属部门信息（非数据库字段，内存填充）
     */
    private DeptVo deptVo;
}
