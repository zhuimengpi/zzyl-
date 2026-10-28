package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 岗位DTO
* */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {

    /** 岗位ID */
    private Long id;

    /** 部门编号 */
    private String deptNo;

    /** 岗位名称 */
    private String postName;

    /** 数据状态（0正常 1停用） */
    private String dataState;

    /** 说明
    * */
    private String remark;
}
