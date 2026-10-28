package com.zhuimeng.qcmanagement.service;

import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.zhuimeng.Result.Result;

public interface AIService {
    String generateStreamAsString(String message) throws NoApiKeyException, InputRequiredException;
}
