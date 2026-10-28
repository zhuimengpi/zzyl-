package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.Member;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FUserMapper {

    /** 插入用户信息
    * */
    @AutoCreate
    void insert(Member member);

    /** 根据用户名查询用户id
    * */
    Long selectIdByUserName(String userName);

    /** 根据openId查询用户id
    * */
    Long selectIdByOpenId(String openId);

    /**根据用户id查询用户信息
     */
    Member getMemberByUserId(Long userId);
}
