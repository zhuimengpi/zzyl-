package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.User;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import com.zhuimeng.qcmanagement.annotation.AutoUpdata;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 系统用户相关查询
 * */

@Mapper
public interface UserMapper {
    /** 通过用户名查询用户
     */
    @Select("SELECT id, username, open_id, password, user_type, avatar, nick_name, " +
            "email, real_name, mobile, sex, data_state, dept_no, post_no, " +
            "create_time, update_time, remark, create_by, update_by, is_leader, is_delete " +
            "FROM sys_user WHERE username = #{username} AND password = #{password}")
    User selectUserByUsername(String username, String password);

    /** 根据ID获取用户信息
     * */
    User selectUserById(String id);

    /** 修改个人信息
     *  姓名、手机号、性别、头像
     * */
    @AutoUpdata
    void updateUserById(User user);

    /** 校验密码
     * */
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User getPass(String id);

    /** 修改密码
     * */
    @AutoUpdata
    void updatePassword(User user);

    /** 查询全量用户列表
     * */
    List<User> listAll();

    /** 新增用户
    * */
    @AutoCreate
    void insert(User user);

    /** 管理员更新用户（全字段 + 乐观锁）
    * */
    @AutoUpdata
    void updateById(User user);

    /** 批量软删除用户
    * */
    void softDeleteByIds(@Param("ids") List<Long> ids);

    /** 删除用户的角色关联
    * */
    void deleteUserRoles(Long userId);

    /** 批量插入用户角色关联
    * */
    void insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}
