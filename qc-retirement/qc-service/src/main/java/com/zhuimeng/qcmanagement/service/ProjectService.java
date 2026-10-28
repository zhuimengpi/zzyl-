package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;

public interface ProjectService {
    Result projectPage(Integer pageNum, Integer pageSize, String keyWord);

    Result projectDetail(Long id);
}
