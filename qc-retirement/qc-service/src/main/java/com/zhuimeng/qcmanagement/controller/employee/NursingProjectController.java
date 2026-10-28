package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.NursingProjectDTO;
import com.zhuimeng.qcmanagement.service.NursingProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 护理项目
* */

@RestController
@RequestMapping("/sysUser/nursing_project")
public class NursingProjectController {
    @Autowired
    private NursingProjectService nursingProjectService;

    /** 获取所有项目
     * */
    @GetMapping("/all")
    public Result listAll() {
        return nursingProjectService.listAll();
    }

    /** 项目分页查询
     * */
    @GetMapping
    public Result page(
            @RequestParam(required = true) Integer pageNum,
            @RequestParam(required = true) Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status
    ) {
        return nursingProjectService.listByPage(pageNum, pageSize, name, status);
    }

    /** 项目详情
     * */
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        return nursingProjectService.detail(id);
    }

    /** 添加项目
     * */
    @PostMapping
    public Result insert(@RequestBody NursingProjectDTO nursingProjectDTO) {
        return nursingProjectService.insert(nursingProjectDTO);
    }

    /** 编辑项目
     * */
    @PutMapping
    public Result update(@RequestBody NursingProjectDTO nursingProjectDTO) {
        return nursingProjectService.update(nursingProjectDTO);
    }

    /** 删除项目
     * */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return nursingProjectService.delete(id);
    }

    /** 项目启用/禁用
     * */
    @PutMapping("/{id}/status/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return nursingProjectService.updateStatus(id, status);
    }
}
