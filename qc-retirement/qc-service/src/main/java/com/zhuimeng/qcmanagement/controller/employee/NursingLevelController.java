package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.NursingLevelDTO;
import com.zhuimeng.qcmanagement.service.NursingLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 护理等级
* */

@RestController
@RequestMapping("/sysUser/nursingLevel")
public class NursingLevelController {
    @Autowired
    private NursingLevelService nursingLevelService;

    /** 获取所有等级
    * */
    @GetMapping("/listAll")
    public Result listAll() {
        return nursingLevelService.listAll();
    }

    /** 等级分页查询
    * */
    @GetMapping("/listByPage")
    public Result listByPage(
            @RequestParam(required = true) Integer pageNum,
            @RequestParam(required = true) Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status
    ) {
        return nursingLevelService.listByPage(pageNum, pageSize, name, status);
    }

    /** 等级详情
    * */
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        return nursingLevelService.detail(id);
    }

    /** 添加等级
    * */
    @PostMapping("/insert")
    public Result insert(@RequestBody NursingLevelDTO nursingLevelDTO) {
        return nursingLevelService.insert(nursingLevelDTO);
    }

    /** 编辑等级
    * */
    @PutMapping("/update")
    public Result update(@RequestBody NursingLevelDTO nursingLevelDTO) {
        return nursingLevelService.update(nursingLevelDTO);
    }

    /** 删除等级
    * */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return nursingLevelService.delete(id);
    }

    /** 等级启用/禁用
    * */
    @PutMapping("/{id}/status/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        return nursingLevelService.updateStatus(id, status);
    }
}
