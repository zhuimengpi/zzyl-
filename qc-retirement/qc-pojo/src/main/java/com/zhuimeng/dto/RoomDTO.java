package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 房间DTO
* */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomDTO {

    /** 房间ID（编辑时必填） */
    private Long id;

    /** 楼层ID */
    private Long floorId;

    /** 房间类型名称 */
    private String typeName;

    /** 房间编号 */
    private String code;

    /** 排序号 */
    private Integer sort;
}
