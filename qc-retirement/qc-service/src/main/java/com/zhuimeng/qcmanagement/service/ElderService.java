package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;

import java.util.List;
import java.util.Map;

public interface ElderService {
    Result selectList(String name, String idCard);

    Result selectListByPage(Integer pageNum, Integer pageSize, String name, String idCard);

    Result pageQuery(Integer pageNum, Integer pageSize, String name, String idCard);

    Result selectByIdCard(String idCardNo);

    Result checkInInfo(Long id);

    Result setNursing(List<Map<String, Object>> nursingList);
}
