package com.zhuimeng.qcmanagement.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 自动添加更新信息
* */

@Target(ElementType.METHOD)     // 注解作用在方法上
@Retention(RetentionPolicy.RUNTIME) // 注解保留在运行时
public @interface AutoUpdata {
}
