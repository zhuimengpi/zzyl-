package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.CheckInConfig;
import com.zhuimeng.entity.Elder;
import com.zhuimeng.entity.Member;
import com.zhuimeng.entity.MemberElder;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import com.zhuimeng.vo.ElderDeviceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface FamilyMapper {

    /** 根据身份证号查询老人信息
     * */
    Elder selectByIdCard(String idCard);

    /** 保存老人信息
    * */
    @AutoCreate
    void insert(Elder elder);

    /** 更新老人基本信息（已存在老人入住时更新） */
    void updateElderInfo(@Param("elderId") Long elderId,
                         @Param("name") String name,
                         @Param("phone") String phone,
                         @Param("sex") String sex,
                         @Param("age") String age,
                         @Param("status") Integer status);

    /** 更新老人床位信息
    * */
    void updateElderBed(@Param("elderId") Long elderId,
                        @Param("bedNumber") String bedNumber,
                        @Param("bedId") Long bedId);

    /** 更新老人状态（0：禁用，1:启用，2:请假，3:退住中，4:入住中，5:已退住）
    * */
    void updateElderStatus(@Param("elderId") Long elderId,
                           @Param("status") Integer status);

    /** 更新床位状态（入住申请中）
    * */
    void updateBedStatus(@Param("bedId") Long bedId,
                         @Param("bedStatus") Integer bedStatus);

    /** 根据老人ID查询家属列表 */
    List<Map<String, Object>> selectFamilyByElderId(@Param("elderId") Long elderId);

    /** 根据手机号查询客户
    * */
    Member selectMemberByPhone(@Param("phone") String phone);

    /** 创建客户
    * */
    @AutoCreate
    void insertMember(Member member);

    /** 初始化老人余额（默认0元）
    * */
    void insertBalance(@Param("elderId") Long elderId,
                       @Param("elderName") String elderName);

    /** 根据身份证号查询老人id
      */
    String selectId(String idCard);

    /** 保存家庭成员信息
     */
    @AutoCreate
    void insertMemberElder(MemberElder memberElder);

    /** 根据用户id查询所有老人信息（含设备ID）
     */
    List<ElderDeviceVO> listAll(String userId);

    /** 分页查询家人列表（含设备ID）
    * */
    List<ElderDeviceVO> selectPage(String userId);

    /** 根据老人id查询用户与老人关联信息
     * */
    MemberElder selectMemberElder(Long elderId);

    /** 根据用户id和老人id查询绑定关系（防重复绑定）
     * */
    MemberElder selectMemberElderByMemberAndElder(@Param("memberId") Long memberId,
                                                   @Param("elderId") Long elderId);

    /** 根据老人id删除用户与老人关联信息
     */
    void deleteMemberElder(Long elderId);

    /** 根据老人id删除老人信息
     */
    void delete(Long elderId);

    /** 根据床位ID列表批量查询老人
     * */
    List<Elder> selectByBedIds(@Param("bedIds") List<Long> bedIds);

    // ==================== 员工端老人管理 ====================

    /** 查询所有老人列表（支持姓名、身份证号模糊匹配）
     * */
    List<Elder> selectAllElders(@Param("name") String name, @Param("idCard") String idCard);

    /** 根据老人ID查询老人信息
     * @param id 老人ID
     */
    Elder selectElderById(@Param("id") Long id);

    /** 根据老人ID查询入住配置信息
     * @param elderId 老人ID
     */
    CheckInConfig selectCheckInConfigByElderId(@Param("elderId") Long elderId);

    /** 根据老人ID删除护理员绑定
     * @param elderId 老人ID
     */
    void deleteNursingByElderId(@Param("elderId") Long elderId);

    /** 批量插入护理员绑定
     * @param elderId    老人ID
     * @param nursingIds 护理员ID列表
     */
    void insertNursing(@Param("elderId") Long elderId, @Param("nursingIds") List<Long> nursingIds);

    /** 根据老人ID列表批量查询护理员
     * @param elderIds 老人ID列表
     * @return Map<elderId, List<UserVO>>
     */
    List<Map<String, Object>> selectNursingByElderIds(@Param("elderIds") List<Long> elderIds);
}
