package com.zhuimeng.vo;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 来访管理-预约记录VO
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReservationVO {
    /** 主键ID */
    private Long id;

    /** 预约人姓名 */
    private String name;

    /** 预约人手机号 */
    private String mobile;

    /** 预约时间 */
    private LocalDateTime time;

    /** 探访人 */
    private String visitor;

    /** 预约类型：0 参观预约，1 探访预约 */
    private Integer type;

    /** 预约状态：0 待报道，1 已完成，2 取消，3 过期 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 备注 */
    private String remark;
}
