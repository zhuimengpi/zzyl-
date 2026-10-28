package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.NursingPlan;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import com.zhuimeng.qcmanagement.annotation.AutoUpdata;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NursingPlanMapper {
    /** 查询所有护理计划
    * */
    List<NursingPlan> listAll();

    /** 根据ID查询护理计划
     */
    NursingPlan selectById(Long id);

    /** 根据名称查询护理计划
     */
    NursingPlan selectByName(String planName);

    /** 添加护理计划
     */
    @AutoCreate
    void insert(NursingPlan nursingPlan);

    /** 更新护理计划
     */
    @AutoUpdata
    void updateById(NursingPlan nursingPlan);

    /** 删除护理计划
     */
    void deleteById(Long id);
}
