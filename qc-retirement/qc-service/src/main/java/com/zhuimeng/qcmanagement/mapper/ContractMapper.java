package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.vo.ContractVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Mapper
public interface ContractMapper {

    /** 查询合同列表（支持筛选）
    * */
    ArrayList<ContractVO> selectContractVOList(
            @Param("contractNo") String contractNo,
            @Param("elderName") String elderName,
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /** 根据ID查询合同详情（含老人信息、房间床位、护理等级、合同日期、PDF地址）
    * */
    java.util.Map<String, Object> selectContractDetailById(@Param("id") Long id);
}
