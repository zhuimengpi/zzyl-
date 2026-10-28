package com.zhuimeng.qcmanagement.controller.employee;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.BedDTO;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/** 房间床位管理
* */

@RestController
@RequestMapping("/sysUser/bed")
public class BedController {
    @Autowired
    private BedService bedService;

    /** 房间下床位
    * */
    @GetMapping("/read/room/{id}")
    public Result readRoom(@PathVariable Long id){
        return bedService.readRoom(id);
    }

    /** 床位详情
     * */
    @GetMapping("/read/{id}")
    public Result read(@PathVariable Long id){
        return bedService.read(id);
    }

    /** 添加床位
    * */
    @PostMapping("/create")
    public Result create(@RequestBody BedDTO bedDTO){
        return bedService.create(bedDTO);
    }

    /** 编辑床位
     * */
    @PutMapping("/update")
    public Result update(@RequestBody BedDTO bedDTO){
        return bedService.update(bedDTO);
    }

    /** 删除床位
     * */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id){
        return bedService.delete(id);
    }
}
