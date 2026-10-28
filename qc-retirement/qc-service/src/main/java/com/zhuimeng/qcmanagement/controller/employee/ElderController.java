package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.qcmanagement.service.ElderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 老人管理
* */

@RestController
@RequestMapping("/sysUser/elder")
public class ElderController {
    @Autowired
    private ElderService elderService;

    /** 获取所有老人信息列表
     * 查询条件：老人姓名、老人身份证号
     */
    @GetMapping("/selectList")
    public Result selectList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String idCard
    ) {
        return elderService.selectList(name, idCard);
    }

    /** 分页查询老人信息
     * 查询条件：老人姓名、老人身份证号
     */
    @GetMapping("/selectListByPage")
    public Result selectListByPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String idCard
    ) {
        return elderService.selectListByPage(pageNum, pageSize, name, idCard);
    }

    /** 分页查询老人信息（V2版本）
     * 查询条件：老人姓名、老人身份证号
     */
    @GetMapping("/pageQuery")
    public Result pageQuery(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String idCard
    ) {
        return elderService.pageQuery(pageNum, pageSize, name, idCard);
    }

    /** 校验身份证号是否已登记，防止重复录入
     */
    @GetMapping("/selectByIdCard")
    public Result selectByIdCard(@RequestParam("idCard") String idCardNo) {
        return elderService.selectByIdCard(idCardNo);
    }

    /** 查询指定老人入住信息
     */
    @GetMapping("/checkInInfo/{id}")
    public Result checkInInfo(@PathVariable Long id) {
        return elderService.checkInInfo(id);
    }

    /** 为老人分配护理员
     * 请求体格式：[{"elderId": 1, "nursingIds": [101, 102]}, ...]
     */
    @PutMapping("/setNursing")
    public Result setNursing(@RequestBody List<Map<String, Object>> nursingList) {
        return elderService.setNursing(nursingList);
    }
}
