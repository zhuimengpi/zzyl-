package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.vo.NursingTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/** 护理任务 Mapper
* */
@Mapper
public interface NursingTaskMapper {

    /** 分页查询护理任务列表 */
    List<Map<String, Object>> selectPage(@Param("status") Integer status,
                                         @Param("elderName") String elderName,
                                         @Param("nurseId") Long nurseId,
                                         @Param("projectId") Long projectId,
                                         @Param("startTime") String startTime,
                                         @Param("endTime") String endTime);

    /** 执行任务：更新 nursing_task */
    void updateTaskDone(@Param("taskId") Long taskId,
                        @Param("estimatedServerTime") Long estimatedServerTime,
                        @Param("mark") String mark,
                        @Param("taskImage") String taskImage,
                        @Param("userId") Long userId);

    /** 执行任务：同步更新关联订单状态 */
    void updateOrderStatusByTask(@Param("taskId") Long taskId, @Param("status") Integer status);

    /** 取消任务 */
    void updateTaskCancel(@Param("taskId") Long taskId,
                          @Param("reason") String reason,
                          @Param("userId") Long userId);

    /** 修改执行日期 */
    void updateTaskTime(@Param("taskId") Long taskId,
                        @Param("estimatedServerTime") Long estimatedServerTime);

    /** 任务详情 */
    Map<String, Object> selectDetail(@Param("taskId") Long taskId);

    /** 根据订单号查询任务 */
    Map<String, Object> selectByOrderNo(@Param("orderNo") String orderNo);

    /** 插入护理任务 */
    void insertNursingTask(Map<String, Object> params);

    /** 根据老人ID查询护理员ID */
    Long selectNursingIdByElderId(@Param("elderId") Long elderId);

    /** 每月1号：为入住中老人批量生成月度护理任务（task_type=2） */
    int insertMonthlyNursingTasks();

    /** 每日刷新：将已过期未执行的月度任务调整为当日待执行 */
    int refreshUnexecutedTasks();
}
