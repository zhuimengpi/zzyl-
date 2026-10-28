package com.zhuimeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 合同VO
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ContractVO {

    /** 主键 */
    private Long id;

    /** 合同名称 */
    private String name;

    /** 合同编号 */
    private String contractNo;

    /** 协议地址 */
    private String pdfUrl;

    /** 丙方手机号 */
    private String memberPhone;

    /** 老人id */
    private Long elderId;

    /** 老人姓名 */
    private String elderName;

    /** 老人信息 */
    private ElderVO elderVo;

    /** 丙方名称 */
    private String memberName;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 状态，0：未生效，1：已生效，2：已过期, 3：已失效 */
    private Integer status;

    /** 排序字段 */
    private Integer sort;

    /** 级别描述 */
    private String levelDesc;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 创建人 */
    private String createBy;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 更新人 */
    private String updateBy;

    /** 备注 */
    private String remark;

    /** 入住单号 */
    private String checkInNo;

    /** 签约日期 */
    private LocalDateTime signDate;

    /** 解除提交人 */
    private String releaseSubmitter;

    /** 解除日期 */
    private LocalDateTime releaseDate;

    /** 解除协议url */
    private String releasePdfUrl;

    /** 退住详情 - 老人信息 VO
    * */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RetreatElderVO {

        /** 老人姓名 */
        private String name;

        /** 退住日期 */
        private String checkOutTime;

        /** 退住原因 */
        private String reason;

        /** 身份证号 */
        private String idCardNo;

        /** 入住开始时间 */
        private String checkInStartTime;

        /** 入住结束时间 */
        private String checkInEndTime;

        /** 费用开始时间 */
        private String costStartTime;

        /** 费用结束时间 */
        private String costEndTime;

        /** 护理等级名称 */
        private String nursingLevelName;

        /** 入住床位号 */
        private String bedNumber;

        /** 护理员姓名 */
        private String nursingName;

        /** 联系方式 */
        private String phone;

        /** 家庭住址 */
        private String address;

        /** 申请人 */
        private String applicat;
    }
}
