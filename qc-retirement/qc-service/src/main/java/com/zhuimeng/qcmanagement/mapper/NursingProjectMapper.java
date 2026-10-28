package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.NursingProject;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import com.zhuimeng.qcmanagement.annotation.AutoUpdata;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NursingProjectMapper {
    /** 查询所有护理项目
    * */
    List<NursingProject> listAll();

    /** 根据ID查询护理项目
     */
    NursingProject selectById(Long id);

    /** 添加护理项目
     */
    @AutoCreate
    void insert(NursingProject nursingProject);

    /** 更新护理项目
     */
    @AutoUpdata
    void updateById(NursingProject nursingProject);

    /** 删除护理项目
     */
    void deleteById(Long id);
}
