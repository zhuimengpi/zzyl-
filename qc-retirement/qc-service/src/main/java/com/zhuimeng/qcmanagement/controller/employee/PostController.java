package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.PostDTO;
import com.zhuimeng.qcmanagement.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 岗位管理
* */

@RestController
@RequestMapping("/sysUser/post")
public class PostController {
    @Autowired
    private PostService postService;

    /** 岗位分页列表
    * */
    @PostMapping("/page/{pageNum}/{pageSize}")
    public Result page(
            @PathVariable Integer pageNum,
            @PathVariable Integer pageSize,
            @RequestBody PostDTO postDTO
    ) {
        return postService.page(pageNum, pageSize, postDTO);
    }

    /** 岗位列表
    * */
    @PostMapping("/list")
    public Result list(@RequestBody PostDTO postDTO) {
        return postService.list(postDTO);
    }

    /** 岗位下拉选项
    * */
    @GetMapping("/optionselect")
    public Result optionselect() {
        return postService.optionselect();
    }

    /** 岗位详情
    * */
    @GetMapping("/system/post/{postId}")
    public Result detail(@PathVariable Long postId) {
        return postService.detail(postId);
    }

    /** 新增岗位
    * */
    @PutMapping
    public Result add(@RequestBody PostDTO postDTO) {
        return postService.add(postDTO);
    }

    /** 修改岗位
    * */
    @PatchMapping
    public Result update(@RequestBody PostDTO postDTO) {
        return postService.update(postDTO);
    }

    /** 启用/禁用岗位
    * */
    @PatchMapping("/is_enable")
    public Result updateIsEnable(@RequestBody PostDTO postDTO) {
        return postService.updateIsEnable(postDTO);
    }

    /** 删除岗位
    * */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return postService.delete(id);
    }
}
