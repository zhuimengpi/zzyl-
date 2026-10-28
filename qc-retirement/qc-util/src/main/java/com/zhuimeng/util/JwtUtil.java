package com.zhuimeng.util;

import com.zhuimeng.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** JWT令牌生成
* */
@Component
public class JwtUtil {

    private static JwtProperties jwtP;

    /** 通过构造器注入 JWT 配置（由 Spring 自动绑定 application.yml 中的 jwt.* 配置） */
    public JwtUtil(JwtProperties jwtP) {
        this.jwtP = jwtP;
    }

    /** 生成JWT令牌
    * */
    public String createJwt(String userId, String userName) {
        //自定义令牌信息，Map<String,Object>格式
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", userId);
        dataMap.put("username", userName);

        String ID = UUID.randomUUID().toString();   //JWT唯一标识

        String jwt = Jwts.builder()
                //设置JWT头信息
                .setId(ID)
                //密钥可以输入base64编码
                .signWith(SignatureAlgorithm.HS256, jwtP.getEmployeeKey())
                //添加自定义信息
                .addClaims(dataMap)
                //压缩生成JWT（生成令牌）
                .compact();

        //返回生成的JWT
        return jwt;
    }

    /** 解析JWT令牌
    * */
    public static Map<String, Object> parseJwt(String jwt) {
        //Claims令牌信息，Map<String,Object>格式
        Claims claims = Jwts.parser()
                //setSigningKey() 设置密钥
                .setSigningKey(jwtP.getEmployeeKey())
                //parseClaimsJws() 解析JWT令牌
                .parseClaimsJws(jwt)
                //getBody() 获取令牌信息
                .getBody();

        //返回令牌信息
        return claims;
    }
}
