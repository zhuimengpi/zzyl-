package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.ResourceDTO;

public interface ResourceService {

    /** 菜单列表 */
    Result list(ResourceDTO resourceDTO);

    /** 菜单树形列表 */
    Result tree(ResourceDTO resourceDTO);

    /** 新增菜单 */
    Result add(ResourceDTO resourceDTO);

    /** 修改菜单 */
    Result update(ResourceDTO resourceDTO);

    /** 禁用菜单 */
    Result enable(ResourceDTO resourceDTO);

    /** 删除菜单 */
    Result delete(Long menuId);
}
