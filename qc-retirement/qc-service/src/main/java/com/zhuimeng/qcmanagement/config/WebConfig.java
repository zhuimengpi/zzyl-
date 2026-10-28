package com.zhuimeng.qcmanagement.config;

import com.zhuimeng.qcmanagement.interceptor.JwtTokenEmployee;
import com.zhuimeng.qcmanagement.interceptor.JwtTokenFamily;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 拦截器配置类
* */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private JwtTokenEmployee jwtTokenEmployee;
    @Autowired
    private JwtTokenFamily jwtTokenFamily;
    @Autowired
    private RedisTemplate redisTemplate;

    //注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 客户登录拦截器
        registry.addInterceptor(new JwtTokenFamily(redisTemplate))  //添加拦截器
                .addPathPatterns("/customer/**") //拦截所有请求
                .excludePathPatterns("/customer/user/login","/customer/user/loginY","/customer/roomTypes"); //排除登录接口和登录校验接口

        //用户登录拦截器
        registry.addInterceptor(new JwtTokenEmployee(redisTemplate))  //添加拦截器
                .addPathPatterns("/sysUser/**") //拦截所有请求
                .excludePathPatterns("/sysUser/security/login"); //排除登录接口
    }

}
