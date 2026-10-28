package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.Role;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import com.zhuimeng.qcmanagement.annotation.AutoUpdata;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {

    /** 查询全量角色列表
    * */
    List<Role> listAll();

    /** 根据ID查询角色
    * */
    Role selectById(Long id);

    /** 新增角色
    * */
    @AutoCreate
    void insert(Role role);

    /** 更新角色
    * */
    @AutoUpdata
    void updateById(Role role);

    /** 批量删除角色
    * */
    void deleteByIds(@Param("ids") List<Long> ids);

    /** 查询角色的菜单权限编号列表
    * */
    List<Long> findResourceNosByRoleId(Long roleId);

    /** 删除角色的所有菜单权限
    * */
    void deleteResourcesByRoleId(Long roleId);

    /** 批量插入角色菜单权限
    * */
    void insertRoleResources(@Param("roleId") Long roleId,
                             @Param("resourceNos") List<Long> resourceNos);
}
