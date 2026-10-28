package com.zhuimeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 菜单树节点VO，返回给前端用于动态路由渲染
* */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MenuVO {
    /**
     * 菜单类型：M 目录, C 菜单
     */
    private String type;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 子菜单
     */
    private List<MenuVO> children;
}
