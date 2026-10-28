package com.zhuimeng.qcmanagement;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan("com.zhuimeng")
@MapperScan("com.zhuimeng.qcmanagement.mapper")
@EnableScheduling
public class QcManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(QcManagementApplication.class, args);
    }

}
