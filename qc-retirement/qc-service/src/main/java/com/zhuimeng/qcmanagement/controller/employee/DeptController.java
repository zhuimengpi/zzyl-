package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.DeptDTO;
import com.zhuimeng.qcmanagement.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 部门管理
* */

@RestController
@RequestMapping("/sysUser/dept")
public class DeptController {
    @Autowired
    private DeptService deptService;

    /** 部门列表
    * */
    @PostMapping("/list")
    public Result list(@RequestBody DeptDTO deptDTO) {
        return deptService.list(deptDTO);
    }

    /** 部门下拉树
    * */
    @PostMapping("/tree")
    public Result tree(@RequestBody String level) {
        return deptService.tree(level);
    }

    /** 新增部门
    * */
    @PutMapping
    public Result add(@RequestBody DeptDTO deptDTO) {
        return deptService.add(deptDTO);
    }

    /** 修改部门
    * */
    @PatchMapping
    public Result update(@RequestBody DeptDTO deptDTO) {
        return deptService.update(deptDTO);
    }

    /** 启用/禁用部门
    * */
    @PatchMapping("/is_enable")
    public Result updateIsEnable(@RequestParam Long id, @RequestParam Integer status) {
        return deptService.updateIsEnable(id, status);
    }

    /** 删除部门
    * */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return deptService.delete(id);
    }
}
