package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 分页查询DTO
* */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageDTO {
    private Integer page;

    private Integer size;

    private String name;

    private String phone;

    private Long startTime;
    
    private Long endTime;

    private Integer status;

    /** 查询类型，null表示全部，0表示参观，1表示探访
    * */
    private Integer type;
}
