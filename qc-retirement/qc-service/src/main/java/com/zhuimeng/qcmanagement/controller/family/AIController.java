package com.zhuimeng.qcmanagement.controller.family;

import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.zhuimeng.Result.Result;
import com.zhuimeng.qcmanagement.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** AI助手
* */

@RestController
@RequestMapping("/customer/ai/generateStreamAsString")
public class AIController {
    @Autowired
    private AIService aiService;

    @GetMapping
    public String generateStreamAsString(String message) throws NoApiKeyException, InputRequiredException {
        return aiService.generateStreamAsString(message);
    }
}
