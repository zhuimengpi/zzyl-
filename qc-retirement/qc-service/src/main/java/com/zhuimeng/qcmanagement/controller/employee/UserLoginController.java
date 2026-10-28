package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.ResourceDTO;
import com.zhuimeng.qcmanagement.service.ResourceService;
import com.zhuimeng.qcmanagement.service.UserLoginService;
import com.zhuimeng.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 员工登录 + 资源管理
* */
@RestController
@RequestMapping("/sysUser")
public class UserLoginController {
    @Autowired
    private UserLoginService userLoginService;
    @Autowired
    private ResourceService resourceService;

    // ==================== 登录相关 ====================

    /** 账号登录
    * */
    @PostMapping("/security/login")
    public Result login(@RequestBody UserLoginVO userLoginVO) {
        return userLoginService.login(userLoginVO.getUsername(), userLoginVO.getPassword());
    }

    /** 获取路由菜单
    * */
    @GetMapping("/resource/menus")
    public Result getMenus() {
        return userLoginService.getMenus();
    }

    /** 获取按钮权限列表
    * */
    @GetMapping("/resource/myButten")
    public Result getMyButten() {
        return userLoginService.getMyButtons();
    }

    // ==================== 菜单资源管理 ====================

    /** 菜单列表
    * */
    @PostMapping("/resource/list")
    public Result list(@RequestBody ResourceDTO resourceDTO) {
        return resourceService.list(resourceDTO);
    }

    /** 菜单树形列表
    * */
    @PostMapping("/resource/tree")
    public Result tree(@RequestBody ResourceDTO resourceDTO) {
        return resourceService.tree(resourceDTO);
    }

    /** 新增菜单
    * */
    @PutMapping("/resource")
    public Result add(@RequestBody ResourceDTO resourceDTO) {
        return resourceService.add(resourceDTO);
    }

    /** 修改菜单
    * */
    @PatchMapping("/resource")
    public Result update(@RequestBody ResourceDTO resourceDTO) {
        return resourceService.update(resourceDTO);
    }

    /** 禁用菜单
    * */
    @PostMapping("/resource/enable")
    public Result enable(@RequestBody ResourceDTO resourceDTO) {
        return resourceService.enable(resourceDTO);
    }

    /** 删除菜单
    * */
    @DeleteMapping("/resource/{menuId}")
    public Result delete(@PathVariable Long menuId) {
        return resourceService.delete(menuId);
    }
}
