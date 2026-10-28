package com.zhuimeng.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhuimeng.properties.WechatProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/** 微信小程序接口工具
* */

@Slf4j
@Component
public class WechatUtil {

    private final WechatProperties wechatProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /** access_token 内存缓存 */
    private volatile String cachedAccessToken;
    private volatile long accessTokenExpireTime;

    private static final String JSCODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
    private static final String ACCESS_TOKEN_URL =
            "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    private static final String GET_PHONE_URL =
            "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=%s";

    public WechatUtil(WechatProperties wechatProperties) {
        this.wechatProperties = wechatProperties;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /** 通过 wx.login() 的 code 换取 openId */
    public String getOpenId(String code) {
        if (code == null || code.isBlank()) {
            log.warn("getOpenId 入参 code 为空");
            return null;
        }
        String appId = wechatProperties.getAppId();
        String secret = wechatProperties.getSecret();
        if (appId == null || appId.isBlank() || secret == null || secret.isBlank()) {
            log.warn("微信 appId 或 secret 未配置");
            return null;
        }
        String url = String.format(JSCODE2SESSION_URL, appId, secret, code);
        log.info("jscode2session 请求: appId={}, code={}", appId, code);
        try {
            String response = restTemplate.getForObject(url, String.class);
            log.info("jscode2session 响应: {}", response);
            JsonNode json = objectMapper.readTree(response);
            if (json.has("errcode") && json.get("errcode").asInt() != 0) {
                log.error("jscode2session 失败: {}", response);
                return null;
            }
            return json.get("openid").asText();
        } catch (Exception e) {
            log.error("jscode2session 异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /** 获取 access_token（带内存缓存，提前5分钟刷新） */
    private String getAccessToken() {
        if (cachedAccessToken != null && System.currentTimeMillis() < accessTokenExpireTime) {
            return cachedAccessToken;
        }
        synchronized (this) {
            if (cachedAccessToken != null && System.currentTimeMillis() < accessTokenExpireTime) {
                return cachedAccessToken;
            }
            String appId = wechatProperties.getAppId();
            String secret = wechatProperties.getSecret();
            if (appId == null || appId.isBlank() || secret == null || secret.isBlank()) {
                log.warn("微信 appId 或 secret 未配置，无法获取 access_token");
                return null;
            }
            String url = String.format(ACCESS_TOKEN_URL, appId, secret);
            log.info("获取 access_token 请求...");
            try {
                String response = restTemplate.getForObject(url, String.class);
                log.info("access_token 响应: {}", response);
                JsonNode json = objectMapper.readTree(response);
                if (json.has("errcode") && json.get("errcode").asInt() != 0) {
                    log.error("获取 access_token 失败: {}", response);
                    return null;
                }
                cachedAccessToken = json.get("access_token").asText();
                int expiresIn = json.get("expires_in").asInt() - 300;
                accessTokenExpireTime = System.currentTimeMillis() + expiresIn * 1000L;
                return cachedAccessToken;
            } catch (Exception e) {
                log.error("获取 access_token 异常: {}", e.getMessage(), e);
                return null;
            }
        }
    }
}
