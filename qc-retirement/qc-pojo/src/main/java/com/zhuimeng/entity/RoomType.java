package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 房型信息
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoomType {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 房型名称
     */
    private String name;

    /**
     * 床位数量
     */
    private Integer bedCount;

    /**
     * 床位费用
     */
    private BigDecimal price;

    /**
     * 介绍
     */
    private String introduction;

    /**
     * 照片
     */
    private String photo;

    /**
     * 状态（0：禁用，1：启用）
     */
    private Integer status;

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
