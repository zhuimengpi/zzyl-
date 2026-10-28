package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 用户请求
* */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    /**
     * 用户id */
    private Long id;

    /**
     * 真实姓名 */
    private String realName;

    /**
     * 手机号码 */
    private String mobile;

    /**
     * 头像地址 */
    private String avatar;

    /**
     * 用户性别 (0男 1女 2未知) */
    private Integer sex;

    /**
     * 邮箱 */
    private String email;

    /**
     * 部门编号 */
    private String deptNo;

    /**
     * 岗位编号 */
    private String postNo;

    /**
     * 数据状态（0正常 1停用） */
    private String dataState;

    /**
     * 角色ID */
    private String roleId;

    /**
     * 角色ID列表（新增/编辑时多选） */
    private List<Long> roleVoIds;
}
