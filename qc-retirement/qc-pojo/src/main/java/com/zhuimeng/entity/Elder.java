package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 老人
* */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Elder {
    /**
     * id
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 图片
     */
    private String image;

    /**
     * 身份证号
     */
    private String idCardNo;

    /**
     * 欠费
     */
    private String age;

    /**
     * 支付截止时间
     */
    private String sex;

    /**
     * 状态（0：禁用，1:启用  2:请假 3:退住中 4入住中 5已退住）
     */
    private Integer status;

    /**
     * 手机号
     */
    private String phone;

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

    /**
     * 床位编号
     */
    private String bedNumber;

    /**
     * 床位id
     */
    private Long bedId;

    /**
     * 关联记录id（member_elder表id）
     */
    private Long mid;
}
