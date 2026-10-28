package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.ProjectPlan;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProjectPlanMapper {
    /** 查询所有项目计划关联
     */
    List<ProjectPlan> listAll();

    /** 根据计划ID查询关联项目列表
     */
    List<ProjectPlan> listByPlanId(Long planId);

    /** 批量插入项目计划
     */
    void insertBatch(List<ProjectPlan> list);

    /** 根据计划ID删除所有关联项目
     */
    void deleteByPlanId(Long planId);
}
