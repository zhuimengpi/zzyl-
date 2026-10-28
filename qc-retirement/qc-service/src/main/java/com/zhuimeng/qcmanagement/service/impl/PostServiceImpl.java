package com.zhuimeng.qcmanagement.service.impl;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.PostDTO;
import com.zhuimeng.entity.Dept;
import com.zhuimeng.entity.DeptVo;
import com.zhuimeng.entity.Post;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.PostMapper;
import com.zhuimeng.qcmanagement.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    //岗位key
    private static final String POST_KEY = RedisKeyProperties.POST;

    private static final Object postListLock = new Object();    //岗位列表锁

    /** 岗位分页列表（Redis缓存全量，内存过滤+分页）
     * */
    @Override
    public Result page(Integer pageNum, Integer pageSize, PostDTO postDTO) {
        // 1.获取全量岗位列表（优先Redis）
        List<Post> allList = getAllFromCache();

        // 2.内存过滤
        List<Post> filteredList = allList.stream()
                .filter(item -> {
                    // deptNo 过滤：为空或根节点则不过滤
                    if (postDTO.getDeptNo() == null || postDTO.getDeptNo().isEmpty()
                            || "100001000000000".equals(postDTO.getDeptNo())) {
                        return true;
                    }
                    return postDTO.getDeptNo().equals(item.getDeptNo());
                })
                .filter(item -> {
                    // 岗位名称模糊搜索
                    if (postDTO.getPostName() == null || postDTO.getPostName().isEmpty()) {
                        return true;
                    }
                    return item.getPostName() != null
                            && item.getPostName().contains(postDTO.getPostName());
                })
                .filter(item -> {
                    // 数据状态过滤
                    if (postDTO.getDataState() == null || postDTO.getDataState().isEmpty()) {
                        return true;
                    }
                    return postDTO.getDataState().equals(item.getDataState());
                })
                .collect(Collectors.toList());

        // 3.内存分页
        int total = filteredList.size();
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Post> pageRecords = (fromIndex < total)
                ? filteredList.subList(fromIndex, toIndex)
                : new ArrayList<>();

        // 4.填充部门名称
        fillDeptVo(pageRecords);

        // 5.封装结果
        Map<String, Object> data = new HashMap<>();
        data.put("records", pageRecords);
        data.put("pages", pages);
        data.put("total", total);

        return Result.ok(data);
    }

    /** 岗位列表（不分页，用于下拉选择）
     * */
    @Override
    public Result list(PostDTO postDTO) {
        // 1.获取全量岗位列表
        List<Post> allList = getAllFromCache();

        // 2.按部门过滤
        List<Post> filteredList = allList.stream()
                .filter(item -> {
                    if (postDTO.getDeptNo() == null || postDTO.getDeptNo().isEmpty()
                            || "100001000000000".equals(postDTO.getDeptNo())) {
                        return true;
                    }
                    return postDTO.getDeptNo().equals(item.getDeptNo());
                })
                .collect(Collectors.toList());

        // 3.填充部门名称
        fillDeptVo(filteredList);

        return Result.ok(filteredList);
    }

    /** 岗位下拉选项（仅返回启用状态的岗位）
     * */
    @Override
    public Result optionselect() {
        List<Post> allList = getAllFromCache();
        List<Post> enabledList = allList.stream()
                .filter(item -> "0".equals(item.getDataState()))
                .collect(Collectors.toList());
        // 填充部门名称
        fillDeptVo(enabledList);
        return Result.ok(enabledList);
    }

    /** 岗位详情
     * */
    @Override
    public Result detail(Long postId) {
        // 1.从缓存中查找
        Post post = findPostById(postId);
        if (post != null) {
            // 填充部门名称
            fillDeptVo(post);
            return Result.ok(post);
        }
        return Result.fail("岗位不存在");
    }

    /** 新增岗位
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result add(PostDTO postDTO) {
        // 1.构造实体
        Post post = new Post();
        post.setDeptNo(postDTO.getDeptNo());
        post.setPostName(postDTO.getPostName());
        post.setDataState(postDTO.getDataState() != null ? postDTO.getDataState() : "0");
        post.setRemark(postDTO.getRemark());
        post.setPostNo(generatePostNo(postDTO.getDeptNo()));

        // 2.插入
        try {
            postMapper.insert(post);
        } catch (DuplicateKeyException e) {
            return Result.fail("岗位名称已存在");
        }

        // 3.事务提交后清除缓存
        transactionAfter();

        return Result.ok("新增成功");
    }

    /** 修改岗位
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result update(PostDTO postDTO) {
        // 1.查询原数据
        Post existing = findPostById(postDTO.getId());
        if (existing == null) {
            return Result.fail("岗位不存在");
        }

        // 2.更新字段
        existing.setDeptNo(postDTO.getDeptNo());
        existing.setPostName(postDTO.getPostName());
        existing.setDataState(postDTO.getDataState());
        existing.setRemark(postDTO.getRemark());
        existing.setOldUpdateTime(existing.getUpdateTime());

        // 3.执行更新
        postMapper.updateById(existing);

        // 4.清除缓存
        transactionAfter();

        return Result.ok("修改成功");
    }

    /** 启用/禁用岗位
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result updateIsEnable(PostDTO postDTO) {
        // 1.查询原数据
        Post existing = findPostById(postDTO.getId());
        if (existing == null) {
            return Result.fail("岗位不存在");
        }

        // 2.更新状态
        existing.setDataState(String.valueOf(postDTO.getDataState()));
        existing.setOldUpdateTime(existing.getUpdateTime());
        postMapper.updateById(existing);

        // 3.清除缓存
        transactionAfter();

        return Result.ok(postDTO.getDataState().equals("1") ? "启用成功" : "禁用成功");
    }

    /** 删除岗位
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result delete(Long id) {
        // 1.查询原数据（前端传的可能是 id 或 postNo，两者都尝试）
        Post existing = findPostByIdOrPostNo(id);
        if (existing == null) {
            return Result.fail("岗位不存在");
        }

        // 2.启用状态下不可删除
        if ("0".equals(existing.getDataState())) {
            return Result.fail("启用状态下不可删除");
        }

        // 3.删除（使用真实主键）
        postMapper.deleteById(existing.getId());

        // 4.清除缓存
        transactionAfter();

        return Result.ok("删除成功");
    }

    // ==================== 私有方法 ====================

    /** 从Redis获取全量岗位列表，缓存未命中则查DB
     * */
    private List<Post> getAllFromCache() {
        // 1.查询Redis
        List<Post> list = (List<Post>) redisTemplate.opsForValue().get(POST_KEY);

        // 2.判断Redis是否存储
        if (list != null && !list.isEmpty()) {
            return list;
        }

        // 3.本地锁 + 双重检查
        synchronized (postListLock) {
            list = (List<Post>) redisTemplate.opsForValue().get(POST_KEY);
            if (list != null && !list.isEmpty()) {
                return list;
            }

            // 4.查询数据库
            list = postMapper.listAll();

            // 5.存入Redis
            redisTemplate.opsForValue().set(POST_KEY, list, 1, TimeUnit.HOURS);
        }

        return list;
    }

    /** 根据ID查找岗位（优先缓存，未命中查DB）
     * */
    private Post findPostById(Long id) {
        // 1.从缓存中查找
        List<Post> allList = (List<Post>) redisTemplate.opsForValue().get(POST_KEY);
        if (allList != null && !allList.isEmpty()) {
            return allList.stream()
                    .filter(item -> item.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        // 2.缓存未命中，查DB
        return postMapper.selectById(id);
    }

    /** 根据ID或岗位编码查找岗位（前端删除时传的可能是 postNo 而非主键 id）
     * */
    private Post findPostByIdOrPostNo(Long id) {
        // 1.先按主键 id 查
        Post post = findPostById(id);
        if (post != null) {
            return post;
        }

        // 2.未命中，转为 String 按 postNo 模糊匹配
        String postNo = String.valueOf(id);
        List<Post> allList = (List<Post>) redisTemplate.opsForValue().get(POST_KEY);
        if (allList != null && !allList.isEmpty()) {
            return allList.stream()
                    .filter(item -> postNo.equals(item.getPostNo()))
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }

    /** 生成岗位编码（部门编号 + 2位序号）
     * */
    private String generatePostNo(String deptNo) {
        if (deptNo == null || deptNo.isEmpty()) {
            deptNo = "100001000000000";
        }

        // 找到该部门下最大的岗位编码
        List<Post> allList = getAllFromCache();
        String prefix = deptNo;
        int maxSeq = allList.stream()
                .filter(p -> p.getPostNo() != null && p.getPostNo().startsWith(prefix))
                .map(p -> {
                    String suffix = p.getPostNo().substring(prefix.length());
                    try {
                        return Integer.parseInt(suffix);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max(Comparator.naturalOrder())
                .orElse(0);

        return String.format("%s%02d", deptNo, maxSeq + 1);
    }

    /** 事务提交后删除缓存
     * */
    private void transactionAfter() {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        redisTemplate.delete(POST_KEY);
                    }
                }
        );
    }

    /** 批量填充部门名称（从部门缓存中按 deptNo 查找）
     * */
    private void fillDeptVo(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        // 从部门缓存中获取 deptNo → deptName 映射
        List<Dept> deptList = (List<Dept>) redisTemplate.opsForValue().get(RedisKeyProperties.DEPT);
        if (deptList == null || deptList.isEmpty()) {
            return;
        }
        Map<String, String> deptNameMap = deptList.stream()
                .collect(Collectors.toMap(Dept::getDeptNo, Dept::getDeptName, (a, b) -> a));

        for (Post post : posts) {
            String deptName = deptNameMap.get(post.getDeptNo());
            if (deptName != null) {
                post.setDeptVo(new DeptVo(deptName));
            }
        }
    }

    /** 单个填充部门名称
     * */
    private void fillDeptVo(Post post) {
        if (post == null) {
            return;
        }
        List<Post> list = new ArrayList<>();
        list.add(post);
        fillDeptVo(list);
    }
}
