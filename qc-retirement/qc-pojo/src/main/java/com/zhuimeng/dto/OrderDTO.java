package com.zhuimeng.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 下单
* */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

    /** 项目ID */
    private Long projectId;

    /** 老人ID */
    private Long elderId;

    /** 老人姓名 */
    private String name;

    /** 订单金额 */
    private BigDecimal amount;

    /** 期望服务时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedArrivalTime;

    /** 备注（可选） */
    private String remark;
}
