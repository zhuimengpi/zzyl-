package com.zhuimeng.qcmanagement.service.impl;

import com.zhuimeng.Result.Result;
import com.zhuimeng.entity.NursingProject;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.ProjectMapper;
import com.zhuimeng.qcmanagement.service.ProjectService;
import com.zhuimeng.util.Lock.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** 服务项目列表
     * */
    @Override
    public Result projectPage(Integer pageNum, Integer pageSize, String keyWord) {
        String key = RedisKeyProperties.NURSING_PROJECT;

        // 1. 查询Redis缓存
        List<NursingProject> allList = (List<NursingProject>) redisTemplate.opsForValue().get(key);

        if (allList == null || allList.isEmpty()) {
            // 创建锁
            RedisLock redisLock = new RedisLock(redisTemplate, key);
            // -获取锁（防止缓存击穿）
            if(!redisLock.tryGetLock()){
                // -获取锁失败，直接返回  （逻辑不可能触发）
                return Result.fail("请刷新重试");
            }

            // 2. 缓存未命中 → 查全部启用项目（不分页，不加关键词过滤）
            allList = projectMapper.projectPage();

            // 3. 写入Redis，1天过期
            redisTemplate.opsForValue().set(key, allList, 1, TimeUnit.DAYS);

            // -释放锁
            redisLock.releaseLock();
        }

        // 4. 内存过滤 + 分页
        return paginate(allList, pageNum, pageSize, keyWord);
    }

    /** 服务项目详情  （在列表查询之后，几乎一定存在Redis缓存）
     * */
    @Override
    public Result projectDetail(Long id) {
        String key = RedisKeyProperties.NURSING_PROJECT;
        // 1. 从Redis取完整列表（与projectPage共享缓存）
        List<NursingProject> allList = (List<NursingProject>) redisTemplate.opsForValue().get(key);

        if (allList == null || allList.isEmpty()) {
            // 2. 缓存未命中
            return Result.fail("缓存未命中");
        }

        // 3. 从列表中按id查找
        NursingProject nursingProject = allList.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);

        // 4. 判断数据是否存在
        if (nursingProject == null) {
            return Result.fail("服务项目不存在");
        }
        return Result.ok(nursingProject);
    }

    /** 内存分页（含关键词过滤）
     * */
    private Result paginate(List<NursingProject> source, int pageNum, int pageSize, String keyWord) {
        // 名称模糊过滤
        List<NursingProject> filtered = source;
        if (keyWord != null && !keyWord.isBlank()) {
            filtered = source.stream()
                    .filter(p -> p.getName() != null && p.getName().contains(keyWord))
                    .collect(Collectors.toList());
        }

        // 手动分页
        int total = filtered.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<NursingProject> pageList = (fromIndex < total)
                ? filtered.subList(fromIndex, toIndex)
                : Collections.emptyList();

        return Result.page(pageList, total, pageNum, pageSize);
    }
}
