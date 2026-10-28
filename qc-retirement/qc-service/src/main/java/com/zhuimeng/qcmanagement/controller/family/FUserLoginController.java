package com.zhuimeng.qcmanagement.controller.family;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.FUserDTO;
import com.zhuimeng.qcmanagement.service.FUserLoginService;
import com.zhuimeng.qcmanagement.service.FamilyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 客户登录接口
* */

@RestController
@RequestMapping("/customer/user")
public class FUserLoginController {
    @Autowired
    private FUserLoginService fUserLoginService;
    @Autowired
    private FamilyService familyService;

    /** 客户登录
    * */
    @PostMapping("/login")
    public Result login(@RequestBody FUserDTO fUserDTO) {
        return fUserLoginService.login(fUserDTO);
    }

    /** 退出登录
    * */
    @PostMapping("/logout")
    public Result logout() {
        return fUserLoginService.logout();
    }

    /** 校验登录状态（查询Redis中的客户登录信息）
    * */
    @GetMapping("/loginY")
    public Result loginY(HttpServletRequest request) {
        String token = request.getHeader("authorization");
        return fUserLoginService.checkLogin(token);
    }

    /** 家人列表（分页查询）
    * */
    @GetMapping("/list-by-page")
    public Result listByPage(Integer pageNum, Integer pageSize) {
        return familyService.listByPage(pageNum, pageSize);
    }

    /** 全部家人列表
    * */
    @GetMapping("/my")
    public Result listAll() {
        return familyService.listAll();
    }

}
