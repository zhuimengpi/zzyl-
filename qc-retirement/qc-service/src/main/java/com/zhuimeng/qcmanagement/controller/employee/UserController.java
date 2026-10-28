package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.PasswordCheckDTO;
import com.zhuimeng.dto.UserDTO;
import com.zhuimeng.qcmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 用户管理 + 个人中心
* */

@RestController
@RequestMapping("/sysUser")
public class UserController {
    @Autowired
    private UserService userService;

    // ==================== 个人中心 ====================

    /** 获取个人信息
    * */
    @GetMapping("/user/current-user")
    public Result getCurrentUser() {
        return userService.getCurrentUser();
    }

    /** 修改个人信息（姓名、手机号、性别、头像）
    * */
    @PatchMapping("/user/self")
    public Result updateSelf(@RequestBody UserDTO userDTO) {
        return userService.updateUser(userDTO);
    }

    /** 修改密码
    * */
    @PutMapping("/user/modify-passwords")
    public Result modifyPasswords(@RequestBody PasswordCheckDTO oldPw) {
        return userService.modifyPasswords(oldPw.getOldPw(), oldPw.getPw());
    }

    /** 校验密码
    * */
    @PutMapping("/user/cmodify-passwords")
    public Result checkPassword(@RequestBody PasswordCheckDTO oldPw) {
        return userService.checkPassword(oldPw.getOldPw());
    }

    // ==================== 用户管理（CRUD） ====================

    /** 用户分页列表
    * */
    @PostMapping("/user/page/{pageNum}/{pageSize}")
    public Result page(
            @PathVariable Integer pageNum,
            @PathVariable Integer pageSize,
            @RequestBody UserDTO userDTO) {
        return userService.page(pageNum, pageSize, userDTO);
    }

    /** 用户列表（下拉选择等）
    * */
    @PostMapping("/user/list")
    public Result list(@RequestBody UserDTO userDTO) {
        return userService.list(userDTO);
    }

    /** 添加用户
    * */
    @PutMapping("/user")
    public Result add(@RequestBody UserDTO userDTO) {
        return userService.add(userDTO);
    }

    /** 修改用户（管理员编辑任意用户）
    * */
    @PatchMapping("/user")
    public Result update(@RequestBody UserDTO userDTO) {
        return userService.update(userDTO);
    }

    /** 删除用户
    * */
    @DeleteMapping("/user/remove/{userIds}")
    public Result delete(@PathVariable String userIds) {
        return userService.delete(userIds);
    }

    /** 重置密码
    * */
    @PostMapping("/user/reset-passwords/{userId}")
    public Result resetPassword(@PathVariable Long userId) {
        return userService.resetPassword(userId);
    }

    /** 启用/禁用用户
    * */
    @PutMapping("/user/is-enable/{id}/{dataState}")
    public Result updateIsEnable(
            @PathVariable Long id,
            @PathVariable String dataState) {
        return userService.updateIsEnable(id, dataState);
    }

    /** 用户信息分页（C端）
    * */
    @GetMapping("/c-user/page")
    public Result cUserPage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String idCard) {
        return userService.cUserPage(pageNum, pageSize, name, idCard);
    }
}
