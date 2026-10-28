package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 菜单资源表
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Resource {

    /** 资源ID */
    private Long id;

    /** 资源编号 */
    private String resourceNo;

    /** 父资源编号 */
    private String parentResourceNo;

    /** 资源名称 */
    private String resourceName;

    /** 资源类型（s平台 c目录 m菜单 r按钮） */
    private String resourceType;

    /** 请求地址 */
    private String requestPath;

    /** 权限标识 */
    private String label;

    /** 数据状态（0正常 1停用） */
    private String dataState;

    /** 排序 */
    private Integer sortNo;

    /** 图标 */
    private String icon;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;

    /** 创建者 */
    private Long createBy;

    /** 更新者 */
    private Long updateBy;

    /** 旧更新时间（乐观锁） */
    private LocalDateTime oldUpdateTime;
}
