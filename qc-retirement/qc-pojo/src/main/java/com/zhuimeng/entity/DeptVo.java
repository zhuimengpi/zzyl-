package com.zhuimeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 部门简要信息（嵌套在岗位等实体中用于前端展示）
* */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeptVo {

    /** 部门名称 */
    private String deptName;
}
