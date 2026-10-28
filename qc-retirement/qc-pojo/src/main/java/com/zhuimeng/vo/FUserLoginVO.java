package com.zhuimeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 客户登录返回
* */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FUserLoginVO {
    //令牌
    private String token;

    //用户昵称
    private String nickName;
}
