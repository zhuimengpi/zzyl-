package com.zhuimeng.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性类
 * <p>
 * 绑定配置文件中以 {@code jwt} 为前缀的配置项。
 *
 * <pre>{@code
 * jwt:
 *   employee-key: zhui
 *   employee-ttl: 3600000
 *   employee-refresh-ttl: 3600000
 *   employee-token-name: Authorization
 * }</pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** 员工端加密密钥 */
    private String employeeKey;

    /** 员工端 Token 刷新时间（毫秒），默认 1 小时 */
    private long employeeRefreshTtl;

    /** 前端传递 Token 的请求头名称 */
    private String employeeTokenName;
}
