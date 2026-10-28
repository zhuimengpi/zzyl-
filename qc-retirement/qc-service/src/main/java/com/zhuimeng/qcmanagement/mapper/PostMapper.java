package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.Post;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import com.zhuimeng.qcmanagement.annotation.AutoUpdata;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostMapper {

    /** 查询全量岗位列表
    * */
    List<Post> listAll();

    /** 根据ID查询岗位
    * */
    Post selectById(Long id);

    /** 新增岗位
    * */
    @AutoCreate
    void insert(Post post);

    /** 更新岗位
    * */
    @AutoUpdata
    void updateById(Post post);

    /** 删除岗位
    * */
    void deleteById(Long id);
}
