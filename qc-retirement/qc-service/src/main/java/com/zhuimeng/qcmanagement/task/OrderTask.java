package com.zhuimeng.qcmanagement.task;

import com.zhuimeng.qcmanagement.mapper.NursingTaskMapper;
import com.zhuimeng.qcmanagement.mapper.OrderMapper;
import com.zhuimeng.util.OrderNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 订单 & 护理任务 - 定时任务
* */

@Component
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private NursingTaskMapper nursingTaskMapper;

    /**
     * 每日零点：重置订单序号 + 处理超时订单 + 刷新未执行的月度任务
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetOrderCounter() {
        // 重置订单序号计数器
        OrderNumberGenerator.resetCounter();

        // 处理超时订单
        orderMapper.updateOrderStatus();

        // 刷新未执行的月度护理任务（将过期任务延至当日）
        try {
            int count = nursingTaskMapper.refreshUnexecutedTasks();
            if (count > 0) {
                System.out.println("刷新未执行月度任务: " + count + " 条");
            }
        } catch (Exception e) {
            System.err.println("刷新月度任务失败: " + e.getMessage());
        }
    }

    /**
     * 每月1号零点：为入住老人批量生成当月护理计划任务（task_type=2）
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void generateMonthlyNursingTasks() {
        try {
            int count = nursingTaskMapper.insertMonthlyNursingTasks();
            System.out.println("生成月度护理任务: " + count + " 条");
        } catch (Exception e) {
            System.err.println("生成月度护理任务失败: " + e.getMessage());
        }
    }

}
