package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** 入住申请（基本信息、家属信息、入住配置、合同信息）
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CheckIn {

    // ==================== 入住申请表（check_in） ====================

    /**
     * id
     */
    private Long id;

    /**
     * 编号
     */
    private String checkInCode;

    /**
     * 标题
     */
    private String title;

    /**
     * 老人id
     */
    private Long elderId;

    /**
     * 养老顾问
     */
    private String counselor;

    /**
     * 入住时间
     */
    private LocalDateTime checkInTime;

    /**
     * 原因
     */
    private String reason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 申请人
     */
    private String applicat;

    /**
     * 部门编号
     */
    private String deptNo;

    /**
     * 申请人id
     */
    private Long applicatId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 流程状态
     */
    private Integer flowStatus;

    /**
     * 审核状态
     */
    private Integer status;

    /**
     * 其他申请信息
     */
    private String otherApplyInfo;

    /**
     * 评估信息
     */
    private String reviewInfo;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // ==================== 老人基本信息（elder） ====================

    /**
     * 老人信息
     */
    private Elder elder;

    // ==================== 家属信息（member） ====================

    /**
     * 家属列表
     */
    private List<Member> familyMembers;

    // ==================== 入住配置（check_in_config） ====================

    /**
     * 入住配置
     */
    private CheckInConfig checkInConfig;

    // ==================== 合同信息（contract） ====================

    /**
     * 合同信息
     */
    private Contract contract;
}
