package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.dto.DeptDTO;
import com.zhuimeng.entity.Dept;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import com.zhuimeng.qcmanagement.annotation.AutoUpdata;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DeptMapper {
    /** 部门列表
     */
    List<Dept> list(DeptDTO deptDTO);

    /** 部门下拉树
     * */
    List<Dept> tree(String level);

    /** 根据ID查询 */
    Dept selectById(Long id);

    /** 新增部门 */
    @AutoCreate
    void insert(Dept dept);

    /** 更新部门 */
    @AutoUpdata
    void updateById(Dept dept);

    /** 删除部门 */
    void deleteById(Long id);

    /** 查询子部门 */
    List<Dept> selectByParentDeptNo(String parentDeptNo);
}
