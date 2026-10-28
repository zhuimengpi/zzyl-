package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 密码校验
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PasswordCheckDTO {
    /** 旧密码
    * */
    private String oldPw;

    /** 新密码
    * */
    private String pw;
}
