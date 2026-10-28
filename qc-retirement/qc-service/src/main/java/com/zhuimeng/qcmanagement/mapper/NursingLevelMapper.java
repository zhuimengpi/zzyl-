package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.dto.NursingLevelDTO;
import com.zhuimeng.entity.NursingLevel;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import com.zhuimeng.qcmanagement.annotation.AutoUpdata;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NursingLevelMapper {
    /** 查询护理级别
    * */
    List<NursingLevel> listAll();

    /** 根据ID查询护理级别
     */
    NursingLevel selectById(Long id);

    /** 根据护理级别名称查询护理级别
     */
    @AutoCreate
    void insert(NursingLevel nursingLevel);

    /** 根据护理级别名称更新护理级别
     */
    @AutoUpdata
    void updateById(NursingLevel nursingLevel);

    /** 根据ID删除护理级别
     */
    void deleteById(Long id);

    /** 根据护理级别名称查询护理级别
     */
    NursingLevel selectByName(String name);
}
