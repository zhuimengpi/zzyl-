package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 角色表
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Role {

    /** 角色ID */
    private Long id;

    /** 角色名称 */
    private String roleName;

    /** 权限标识 */
    private String label;

    /** 排序 */
    private Integer sortNo;

    /** 数据状态（0正常 1停用） */
    private String dataState;

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

    /** 数据范围（0自定义 1本人 2本部门及以下 3本部门 4全部） */
    private String dataScope;

    /** 旧更新时间（乐观锁） */
    private LocalDateTime oldUpdateTime;
}
