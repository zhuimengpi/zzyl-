package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 客户请求
* */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FUserDTO {

    /** 微信登录凭证 wx.login() 返回 */
    private String code;

    /** 微信手机号授权code */
    private String phoneCode;

    /** 用户微信昵称 */
    private String nickName;
}
