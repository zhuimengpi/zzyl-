package com.zhuimeng.qcmanagement.controller.family;

import com.zhuimeng.Result.Result;
import com.zhuimeng.qcmanagement.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 服务项目
* */

@RestController
@RequestMapping("/customer/orders")
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    /** 服务项目列表
    * */
    @GetMapping("/project/page")
    public Result projectPage(Integer pageNum, Integer pageSize, String name) {
        return projectService.projectPage(pageNum, pageSize, name);
    }

    /** 服务项目详情
    * */
    @GetMapping("/project/{id}")
    public Result projectDetail(@PathVariable Long id) {
        return projectService.projectDetail(id);
    }
}
