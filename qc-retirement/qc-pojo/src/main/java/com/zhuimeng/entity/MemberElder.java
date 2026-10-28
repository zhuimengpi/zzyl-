package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 客户老人关联
* */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberElder {
    /**
     * id
     */
    private Long id;

    /**
     * 客户id
     */
    private Long memberId;

    /**
     * 老人id
     */
    private Long elderId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 备注
     */
    private String remark;
}
