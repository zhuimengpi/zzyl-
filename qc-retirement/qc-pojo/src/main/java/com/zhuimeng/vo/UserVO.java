package com.zhuimeng.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 用户个人中心数据返回VO
 *  包含用户基本信息及关联的部门、职位、角色
* */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserVO {
    /** 用户ID */
    private Long id;

    /** 真实姓名 */
    private String realName;

    /** 用户昵称 */
    private String nickName;

    /** 邮箱 */
    private String email;

    /** 所属部门名称 (sys_dept.dept_name) */
    private String deptName;

    /** 所属职位名称 (sys_post.post_name) */
    private String postName;

    /** 角色名称列表 (sys_role.role_name) */
    private List<String> roleNames;

    /** 手机号码 */
    private String mobile;

    /** 性别 (0:男, 1:女, 2:未知) */
    private Integer sex;

    /** 头像地址 */
    private String avatar;
}
