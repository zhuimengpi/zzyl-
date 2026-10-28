package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.NursingProject;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;

@Mapper
public interface ProjectMapper {
    /** 服务项目列表
     * */
    ArrayList<NursingProject> projectPage();

    /** 服务项目详情
     * */
    NursingProject projectDetail(Long id);
}
