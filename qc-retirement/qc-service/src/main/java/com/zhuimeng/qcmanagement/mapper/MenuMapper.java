package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.SysResource;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** 菜单权限相关查询
 * */
@Mapper
public interface MenuMapper {

    /** 查询用户有权限访问的菜单资源
     *  通过用户→角色→资源三表关联，获取启用的目录(c)和菜单(m)项
     */
    List<SysResource> selectMenusByUserId(Long userId);

    /** 查询用户有权限的按钮资源
     *  通过用户→角色→资源三表关联，获取启用的按钮(r)项
     */
    List<SysResource> selectButtonsByUserId(Long userId);
}
