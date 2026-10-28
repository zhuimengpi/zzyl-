package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.Resource;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import com.zhuimeng.qcmanagement.annotation.AutoUpdata;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ResourceMapper {

    /** 查询全量资源列表 */
    List<Resource> listAll();

    /** 根据ID查询 */
    Resource selectById(Long id);

    /** 根据资源编号查询 */
    Resource selectByResourceNo(String resourceNo);

    /** 新增资源 */
    @AutoCreate
    void insert(Resource resource);

    /** 更新资源 */
    @AutoUpdata
    void updateById(Resource resource);

    /** 删除资源 */
    void deleteById(Long id);

    /** 查询子资源 */
    List<Resource> selectByParentResourceNo(String parentResourceNo);
}
