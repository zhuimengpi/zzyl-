package com.zhuimeng.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 微信小程序配置
* */


@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatProperties {
    /** 小程序 AppID */
    private String appId;

    /** 小程序 AppSecret */
    private String secret;
}
