package com.zhuimeng.qcmanagement.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.zhuimeng.Thread.UserHolder;
import com.zhuimeng.dto.UserDTO;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/** 客户登录拦截器
* */

@Component
@Slf4j
public class JwtTokenFamily implements HandlerInterceptor {
    @Autowired
    private RedisTemplate redisTemplate;

    //构造拦截器
    public JwtTokenFamily(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    //目标资源运行前执行，true表示 放行，false表示 不放行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取token
        String token = request.getHeader("authorization");

        // 2.判断token是否为空
        if (token == null || token.equals("")) {
            //无用户，返回401
            response.setStatus(401);
            //拦截
            return false;
        }

        // 3. 解析token
        Map<String, Object> map = JwtUtil.parseJwt(token);
        if (map == null || map.isEmpty()) {
            //无用户，返回401
            response.setStatus(401);
            //拦截
            return false;
        }
        String userId = map.get("id").toString();

        // 4.判断token是否有效
        String key = RedisKeyProperties.FAMILY_LOGIN_TOKEN + userId;
        Map userMap = redisTemplate.opsForHash().entries(key);
        if (userMap.isEmpty()) {
            //token无效，返回401
            response.setStatus(401);
            //拦截
            return false;
        }

        // 5.获取Redsi中存储的token
        String redisToken = userMap.get("token").toString();

        // 6.判断token是否一致
        if (!token.equals(redisToken)) {
            //token不一致，返回401
            response.setStatus(401);
            //拦截
            return false;
        }

        // 7.1获取UserDTO
        //将userMap转为UserDTO
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

        // 7.2将UserDTO放入ThreadLocal中
        UserHolder.set(userDTO);

        // 8.延长token的过期时间（50分钟后若用户继续操作系统时，token有效期续1个小时）
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl != null && ttl < 600) {
            // 续期1小时
            redisTemplate.expire(key, 1, TimeUnit.HOURS);
        }

        // 9.放行
        return true;
    }

    //目标资源运行后执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {

    }

    //视图渲染后执行，释放资源（前后端未分离的情况下）
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserHolder.remove();
    }
}
