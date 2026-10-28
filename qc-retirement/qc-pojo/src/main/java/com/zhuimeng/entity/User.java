package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** 系统用户
* */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    /**
     * 用户ID（主键）
     */
    private Long id;

    /**
     * 用户账号
     */
    private String username;

    /**
     * open_id标识
     */
    private String openId;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户类型 (0:系统用户,1:客户)
     */
    private Integer userType;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 用户性别 (0男 1女 2未知)
     */
    private Integer sex;

    /**
     * 数据状态 (0正常 1停用)
     */
    private String dataState;

    /**
     * 部门编号
     */
    private String deptNo;

    /**
     *  部门名称*/
    private String deptName;

    /**
     * 岗位编号
     */
    private String postNo;

    /** 岗位名称
    * */
    private String postName;

    /** 角色名称（逗号分隔）
    * */
    private String roleName;

    /** 角色标签列表（前端渲染用，由 roleName 拆分）
    * */
    private List<String> roleLabels;

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
     * 是否是部门leader (0:否, 1:是)
     */
    private Integer isLeader;

    /**
     * 是否删除 (逻辑删除)
     */
    private Integer isDelete;

    /** 老修改时间
    * */
    private LocalDateTime oldUpdateTime;
}
