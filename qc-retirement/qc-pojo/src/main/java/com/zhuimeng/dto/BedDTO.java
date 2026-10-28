package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 床位DTO
 * */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BedDTO {

    /** 床位编号 */
    private String bedNumber;

    /** 排序号 */
    private Integer sort;

    /** 房间ID */
    private Long roomId;
}
