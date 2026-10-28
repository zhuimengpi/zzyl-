package com.zhuimeng.qcmanagement.aspect;

import com.zhuimeng.Thread.UserHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/** 修改-数据库更新切面
* */

@Aspect
@Component
public class UpdataAspect {
    //Pointcut 抽象化切入点
    @Pointcut("execution(* com.zhuimeng.qcmanagement.mapper..*.*(..)) && @annotation(com.zhuimeng.qcmanagement.annotation.AutoUpdata)")
    public void pointcut(){}

    // 前置操作
    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        // 1.赋值准备
        LocalDateTime now = LocalDateTime.now();
        String userId = UserHolder.getId();

        // 2.获取参数
        Object[] args = joinPoint.getArgs();
        Object entity = args[0];

        // 3.赋值
        try {
            //反射 获取方法
            Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateBy", Long.class);

            //调用方法
            setUpdateTime.invoke(entity, now);
            setUpdateUser.invoke(entity, Long.valueOf(userId));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
