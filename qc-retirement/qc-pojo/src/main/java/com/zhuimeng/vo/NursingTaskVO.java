package com.zhuimeng.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 服务任务信息 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NursingTaskVO {

    /** 服务人员 */
    private String creator;

    /** 服务时间 */
    private LocalDateTime updateTime;

    /** 服务备注 */
    private String mark;

    /** 服务照片 */
    private String taskImage;
}
