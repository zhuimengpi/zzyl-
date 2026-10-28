package com.zhuimeng.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 阿里云OSS配置类
* */

@Component
@ConfigurationProperties(prefix = "aliyun.oss")
@Data
public class AliOssProperties {

    private String endpoint;    //阿里云OSS服务所在的位置
    private String accessKeyId; //阿里云OSS账号
    private String accessKeySecret; //阿里云OSS密码
    private String bucketName;  //阿里云OSS的Bucket

}
