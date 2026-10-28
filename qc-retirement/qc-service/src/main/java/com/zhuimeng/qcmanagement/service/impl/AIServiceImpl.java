package com.zhuimeng.qcmanagement.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import com.zhuimeng.Result.Result;
import com.zhuimeng.Thread.UserHolder;
import com.zhuimeng.entity.AIMessage;
import com.zhuimeng.qcmanagement.mapper.AIMapper;
import com.zhuimeng.qcmanagement.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AIServiceImpl implements AIService {
    @Autowired
    private AIMapper aiMapper;

    // 调用AI模型
    static {
        Constants.baseHttpApiUrl="https://ws-uymr7cgxf1c50pp0.cn-beijing.maas.aliyuncs.com/api/v1";
    }

    // APIkey
    @Value("${spring.aliyun.accessKeyId}")
    private String apiKey;

    @Transactional(rollbackFor = {Exception.class})        //所有异常都进行回滚
    @Override
    public String generateStreamAsString(String message){
        // 1.查询历史对话
        String userId = UserHolder.getId();
        List<AIMessage> aiMessages = aiMapper.selectList(userId);

        // 2.回忆对话
        List<Message> messages = new ArrayList<>();
        for (AIMessage aiMessage : aiMessages) {
            Message smg = Message.builder()
                    .role(aiMessage.getRole())
                    .content(aiMessage.getContent())
                    .build();
            messages.add(smg);
        }

        // 3.传入新对话
        AIMessage newUserMessage = new AIMessage();
        newUserMessage.setRole("user");
        newUserMessage.setContent(message);
        newUserMessage.setUserId(Long.valueOf(userId));

        Message newMessage = Message.builder()
                .role("user")
                .content(message)
                .build();
        messages.add(newMessage);

        // 4.调用模型
        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey(apiKey)
                // 此处以qwen-plus为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
                .model("qwen-plus")
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        // 5.生成对话
        Generation gen = new Generation();
        GenerationResult call = null;
        try {
            call = gen.call(param);
        } catch (NoApiKeyException | InputRequiredException e) {
            return "AI出现错误";
        }

        // 6.对话存入数据库
        String aiReply = call
                .getOutput()    // 获取模型的输出结果对象
                .getChoices()   // 获取候选回复列表（可能有多个）
                .get(0)         // 取第一个候选回复（通常只有这一个）
                .getMessage()   // 获取该回复的消息对象（含角色和内容）
                .getContent();  // 获取消息的文本内容

        AIMessage newAIMessage = new AIMessage();
        newAIMessage.setRole("assistant");
        newAIMessage.setContent(aiReply);
        newAIMessage.setUserId(Long.valueOf(userId));

        aiMapper.insertAIMessage(newUserMessage);
        aiMapper.insertAIMessage(newAIMessage);

        // 7.返回结果
        return aiReply;
    }
}
