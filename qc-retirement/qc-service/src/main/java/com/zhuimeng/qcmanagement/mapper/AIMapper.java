package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.AIMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AIMapper {

    // 1.查询历史对话
    List<AIMessage> selectList(String userId);

    void insertAIMessage(AIMessage newUserMessage);
}
