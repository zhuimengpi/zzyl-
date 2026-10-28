package com.zhuimeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 服务项目信息 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NursingProjectVO {

    /** 项目名称 */
    private String name;

    /** 图片 */
    private String image;

    /** 单位 */
    private String unit;
}
