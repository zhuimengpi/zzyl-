package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.CheckInConfig;
import com.zhuimeng.entity.Elder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 老人管理 Mapper（员工端）
* */
@Mapper
public interface ElderMapper {

    /** 根据条件查询所有老人列表
     * @param name   老人姓名（模糊匹配）
     * @param idCard 身份证号（模糊匹配）
     */
    List<Elder> selectList(@Param("name") String name, @Param("idCard") String idCard);

    /** 根据老人ID查询老人信息
     * @param id 老人ID
     */
    Elder selectById(@Param("id") Long id);

    /** 根据老人ID查询入住配置
     * @param elderId 老人ID
     */
    CheckInConfig selectCheckInConfigByElderId(@Param("elderId") Long elderId);

    /** 根据老人ID删除旧的护理员绑定关系
     * @param elderId 老人ID
     */
    void deleteNursingByElderId(@Param("elderId") Long elderId);

    /** 批量插入老人与护理员绑定关系
     * @param elderId    老人ID
     * @param nursingIds 护理员ID列表
     */
    void insertNursing(@Param("elderId") Long elderId, @Param("nursingIds") List<Long> nursingIds);
}
