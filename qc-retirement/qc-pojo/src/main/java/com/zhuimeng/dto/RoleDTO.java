package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 角色DTO
* */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDTO {

    /** 角色ID */
    private Long id;

    /** 角色名称 */
    private String roleName;

    /** 数据状态（0正常 1停用） */
    private String dataState;

    /** 选中的菜单权限编号列表 */
    private List<Long> checkedResourceNos;

    /** 角色IDs（批量删除时逗号分隔） */
    private String roleIds;
}
