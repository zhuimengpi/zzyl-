package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** 房型DTO
* */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomTypesDTO {

    /** 房型名称 */
    private String name;

    /** 床位数量 */
    private Integer bedCount;

    /** 床位费用 */
    private BigDecimal price;

    /** 介绍 */
    private String introduction;

    /** 照片 */
    private String photo;

    /** 状态*/
    private Integer status = 1;
}
