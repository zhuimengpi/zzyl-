package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** AI对话记录
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AIMessage {
    // 对话id
    Long id;

    // 角色
    String role;

    // 对话内容
    String content;

    // 用户ID
    Long userId;
}
