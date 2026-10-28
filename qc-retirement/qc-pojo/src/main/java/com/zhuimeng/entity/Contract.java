package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 合同
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Contract {

    /**
     * 主键
     */
    private Long id;

    /**
     * 合同名称
     */
    private String name;

    /**
     * 合同编号
     */
    private String contractNo;

    /**
     * 协议地址
     */
    private String pdfUrl;

    /**
     * 丙方手机号
     */
    private String memberPhone;

    /**
     * 老人id
     */
    private Long elderId;

    /**
     * 老人姓名
     */
    private String elderName;

    /**
     * 丙方名称
     */
    private String memberName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态（0：未生效，1：已生效，2：已过期，3：已失效）
     */
    private Integer status;

    /**
     * 排序字段
     */
    private Integer sort;

    /**
     * 级别描述
     */
    private String levelDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 备注
     */
    private String remark;

    /**
     * 入住单号
     */
    private String checkInNo;

    /**
     * 签约日期
     */
    private LocalDateTime signDate;

    /**
     * 解除提交人
     */
    private String releaseSubmitter;

    /**
     * 解除日期
     */
    private LocalDateTime releaseDate;

    /**
     * 解除协议url
     */
    private String releasePdfUrl;

    /**
    * */
}
