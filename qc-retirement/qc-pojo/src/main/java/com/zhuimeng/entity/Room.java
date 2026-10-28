package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** 房间表
* */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Room {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 房间编号
     */
    private String code;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 房间类型名称
     */
    private String typeName;

    /**
     * 楼层id
     */
    private Long floorId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    private Integer isDeleted;

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

    /** 床位数据
    * */
    private List<Bed> bedVoList;

    /** 房间绑定的设备列表
    * */
    private List<Device> deviceVos;
}
