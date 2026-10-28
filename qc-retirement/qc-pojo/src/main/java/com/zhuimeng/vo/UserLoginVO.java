package com.zhuimeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 系统员工登录VO
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserLoginVO {
    private String username;
    private String password;
}
