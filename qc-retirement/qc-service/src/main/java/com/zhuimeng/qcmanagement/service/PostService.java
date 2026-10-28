package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.PostDTO;

public interface PostService {

    /** 岗位分页列表
    * */
    Result page(Integer pageNum, Integer pageSize, PostDTO postDTO);

    Result list(PostDTO postDTO);

    Result optionselect();

    Result detail(Long postId);

    Result add(PostDTO postDTO);

    Result update(PostDTO postDTO);

    Result updateIsEnable(PostDTO postDTO);

    Result delete(Long id);
}
