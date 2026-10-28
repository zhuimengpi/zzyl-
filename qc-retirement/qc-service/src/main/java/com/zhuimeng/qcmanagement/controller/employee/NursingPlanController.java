package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.NursingPlanDTO;
import com.zhuimeng.qcmanagement.service.NursingPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 护理计划
* */

@RestController
@RequestMapping("/sysUser/nursing/plan")
public class NursingPlanController {
    @Autowired
    private NursingPlanService nursingPlanService;

    /** 获取所有计划
    * */
    @GetMapping
    public Result list() {
        return nursingPlanService.list();
    }

    /** 计划分页查询
    * */
    @GetMapping("/search")
    public Result page(
            @RequestParam(required = true) Integer pageSize,
            @RequestParam(required = true) Integer pageNum,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status
    ) {
        return nursingPlanService.page(pageSize, pageNum, name, status);
    }

    /** 计划详情
    * */
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        return nursingPlanService.detail(id);
    }

    /** 添加计划
    * */
    @PostMapping
    public Result add(@RequestBody NursingPlanDTO nursingPlanDTO) {
        return nursingPlanService.add(nursingPlanDTO);
    }

    /** 编辑计划
    * */
    @PutMapping("/{id}")
    public Result edit(
            @PathVariable Long id,
            @RequestBody NursingPlanDTO nursingPlanDTO
    ) {
        return nursingPlanService.edit(id, nursingPlanDTO);
    }

    /** 删除计划
    * */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return nursingPlanService.delete(id);
    }

    /** 计划启用/禁用
    * */
    @PutMapping("/{id}/status/{status}")
    public Result status(
            @PathVariable Long id,
            @PathVariable Integer status
    ) {
        return nursingPlanService.status(id, status);
    }
}
