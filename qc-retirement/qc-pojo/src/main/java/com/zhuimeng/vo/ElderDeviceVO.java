package com.zhuimeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 老人信息 + 医疗设备ID（家人列表返回）
* */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ElderDeviceVO {

    // ========== Elder（老人）字段 ==========

    /** 老人ID */
    private Long id;

    /** 老人姓名 */
    private String name;

    /** 老人头像 */
    private String image;

    /** 身份证号 */
    private String idCardNo;

    /** 年龄 */
    private String age;

    /** 性别 */
    private String sex;

    /** 手机号 */
    private String phone;

    /** 状态（0：禁用，1:启用 2:请假 3:退住中 4入住中 5已退住） */
    private Integer status;

    /** 床位编号 */
    private String bedNumber;

    /** 床位ID */
    private Long bedId;

    /** 老人备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ========== MemberElder（关联）字段 ==========

    /** 关联记录ID（member_elder表主键） */
    private Long mid;

    /** 与老人关系（member_elder.remark） */
    private String mremark;

    // ========== Device（医疗设备）字段 ==========

    /** 医疗设备ID（device.id） */
    private Long iotId;
}
