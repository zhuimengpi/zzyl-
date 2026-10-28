package com.zhuimeng.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 预约提交
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReservationDTO {
    private Long id;

    /** 预约人姓名 */
    private String name;

    /** 手机号 */
    private String mobile;

    /** 探访人姓名 */
    private String visitor;

    /** 预约类型（0：参观预约，1：探访预约） */
    private Integer type;

    /** 预约时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    /** 老人ID（可选） */
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
}
