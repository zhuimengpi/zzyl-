package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.RoleDTO;
import com.zhuimeng.qcmanagement.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 角色管理
* */

@RestController
@RequestMapping("/sysUser")
public class RoleController {
    @Autowired
    private RoleService roleService;

    /** 角色分页列表
    * */
    @PostMapping("/role/page/{pageNum}/{pageSize}")
    public Result page(
            @PathVariable Integer pageNum,
            @PathVariable Integer pageSize,
            @RequestBody RoleDTO roleDTO) {
        return roleService.page(pageNum, pageSize, roleDTO);
    }

    /** 角色下拉列表
    * */
    @PostMapping("/role/init-roles")
    public Result initRoles() {
        return roleService.initRoles();
    }

    /** 查询角色的菜单权限
    * */
    @GetMapping("/role/find-checked-resources/{roleId}")
    public Result findCheckedResources(@PathVariable Long roleId) {
        return roleService.findCheckedResources(roleId);
    }

    /** 新增角色
    * */
    @PutMapping("/role")
    public Result add(@RequestBody RoleDTO roleDTO) {
        return roleService.add(roleDTO);
    }

    /** 修改角色（编辑信息 / 分配菜单权限 / 启用禁用 统一走此接口）
    * */
    @PatchMapping("/role")
    public Result update(@RequestBody RoleDTO roleDTO) {
        return roleService.update(roleDTO);
    }

    /** 删除角色
    * */
    @DeleteMapping("/role/{roleIds}")
    public Result delete(@PathVariable String roleIds) {
        return roleService.delete(roleIds);
    }
}
