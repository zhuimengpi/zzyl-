package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** 医疗设备
* */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Device {
    /**
     * id
     */
    private Long id;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 绑定位置
     */
    private String bindingLocation;

    /**
     * 位置类型 0:老人 1:位置
     */
    private Integer locationType;

    /**
     * 物理位置类型 0:楼层 1:房间 2:床位
     */
    private Integer physicalLocationType;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 备注名称
     */
    private String noteName;

    /**
     * 产品key
     */
    private String productId;

    /**
     * 产品名称
     */
    private String produceName;

    /**
     * 位置备注
     */
    private String deviceDescription;

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
    private Boolean isDeleted;

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
     * 设备数据列表
     */
    private List<DeviceData> deviceDataVos;
}
