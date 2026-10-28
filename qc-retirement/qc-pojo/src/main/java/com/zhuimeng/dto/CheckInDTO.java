package com.zhuimeng.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 入住申请DTO
* */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckInDTO {

    /** id */
    private Long id;

    /** 编号 */
    private String checkInCode;

    /** 标题 */
    private String title;

    /** 老人id */
    private Long elderId;

    /** 养老顾问 */
    private String counselor;

    /** 入住时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;

    /** 原因 */
    private String reason;

    /** 备注 */
    private String remark;

    /** 申请人 */
    private String applicat;

    /** 部门编号 */
    private String deptNo;

    /** 申请人id */
    private Long applicatId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 流程状态 */
    private Integer flowStatus;

    /** 审核状态 */
    private Integer status;

    /** 其他申请信息 */
    private String otherApplyInfo;

    /** 评估信息 */
    private String reviewInfo;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
