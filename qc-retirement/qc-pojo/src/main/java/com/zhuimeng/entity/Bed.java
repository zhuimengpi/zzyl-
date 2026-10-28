package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import com.zhuimeng.vo.UserVO;

/** 床位表
 * */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Bed {

    /**
     * 床位ID
     */
    private Long id;

    /**
     * 床位编号
     */
    private String bedNumber;

    /**
     * 床位状态: 未入住0, 已入住1 入住申请中2
     */
    private Integer bedStatus;

    /**
     * 床位号（排序）
     */
    private Integer sort;

    /**
     * 房间ID
     */
    private Long roomId;

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

    /** 老日期（乐观锁）
    * */
    private LocalDateTime oldDate;

    /** 床位绑定的设备列表
    * */
    private List<Device> deviceVos;

    /** 护理员列表
    * */
    private List<UserVO> userVos;

    /** 老人姓名
    * */
    private String name;

    /** 老人ID（冗余传递，不持久化）
    * */
    private Long elderId;
}
