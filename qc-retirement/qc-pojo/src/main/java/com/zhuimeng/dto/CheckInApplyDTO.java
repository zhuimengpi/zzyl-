package com.zhuimeng.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** 入住申请 V2 提交数据
* */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckInApplyDTO {

    /** 老人基本信息 */
    private CheckInElderDTO checkInElderDto;

    /** 家属信息列表 */
    private List<ElderFamilyDTO> elderFamilyDtoList;

    /** 入住配置 */
    private CheckInConfigDTO checkInConfigDto;

    /** 签约合同 */
    private CheckInContractDTO checkInContractDto;
}
