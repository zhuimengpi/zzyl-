package com.zhuimeng.qcmanagement.controller.upload;

import com.zhuimeng.Result.Result;
import com.zhuimeng.util.AliOssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/** 文件上传
* */

@RestController
@RequestMapping("/common/upload")
public class UploadController {
    @Autowired
    AliOssUtil aliOssUtil;

    @PostMapping()
    public Result upload(MultipartFile file){   //MultipartFile Spring提供的文件上传接口
        try {
            //获取上传后文件名
            String originalFilename = file.getOriginalFilename();
            String substring = originalFilename.substring(originalFilename.lastIndexOf("."));//获取文件后缀
            String ObjectName = UUID.randomUUID().toString() + substring;

            //获取文件请求路径
            String filePath = aliOssUtil.upload(file.getBytes(), ObjectName);

            //返回文件上传路径
            return Result.ok(filePath);

        } catch (IOException e) {
            return Result.fail("上传失败");
        }
    }
}
