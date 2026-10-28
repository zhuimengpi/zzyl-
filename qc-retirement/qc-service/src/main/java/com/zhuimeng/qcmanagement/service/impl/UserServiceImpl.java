package com.zhuimeng.qcmanagement.service.impl;

import com.zhuimeng.Result.Result;
import com.zhuimeng.Thread.UserHolder;
import com.zhuimeng.dto.UserDTO;
import com.zhuimeng.entity.User;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.UserMapper;
import com.zhuimeng.qcmanagement.service.UserService;
import com.zhuimeng.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    //用户列表key
    private static final String key = RedisKeyProperties.USER;

    /** 获取个人信息
     * */
    @Override
    public Result getCurrentUser() {
        //获取用户ID
        String userId = UserHolder.getId();

        //根据用户ID查询用户信息
        User user = userMapper.selectUserById(userId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        // 将逗号分隔的角色字符串转为列表，匹配前端 roleNames 数组
        if (user.getRoleName() != null && !user.getRoleName().isEmpty()) {
            userVO.setRoleNames(java.util.Arrays.asList(user.getRoleName().split(",")));
        }

        return Result.ok(userVO);
    }

    /** 修改个人信息
     *  姓名、手机号、性别、头像
     * */
    @Override
    public Result updateUser(UserDTO userDTO) {
        //获取用户ID
        String userId = UserHolder.getId();

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        // 从session覆盖用户ID，防止前端传入的id篡改
        user.setId(Long.valueOf(userId));

        //修改用户信息
        userMapper.updateUserById(user);

        return Result.ok();
    }

    /** 修改密码
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result modifyPasswords(String oldPassword, String newPassword) {
        // 1. 获取当前用户ID
        String userId = UserHolder.getId();

        // 2. 校验旧密码
        String oldHash = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        User user = userMapper.getPass(userId);
        if (!oldHash.equals(user.getPassword())) {
            return Result.fail("原密码错误");
        }

        // 3. 新密码校验
        // 密码长度：8-20位
        if (newPassword.length() < 8 || newPassword.length() > 20) {
            return Result.fail("密码长度显示8-20位, 请重新输入");
        }
        // 必须包含数字
        if (!newPassword.matches(".*[0-9].*")) {
            return Result.fail("必须包含数字, 请重新输入");
        }
        // 必须包含小写字母
        if (!newPassword.matches(".*[a-z].*")) {
            return Result.fail("必须包含小写字母,请重新输入");
        }
        // 必须包含大写字母
        if (!newPassword.matches(".*[A-Z].*")) {
            return Result.fail("必须包含大写字母,请重新输入");
        }

        // 4. 更新密码
        String newHash = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        user.setPassword(newPassword);
        userMapper.updatePassword(user);

        // 5. 删除Redis缓存的Token
        redisTemplate.delete(RedisKeyProperties.EMPLOYEE_LOGIN_TOKEN + userId);

        return Result.ok();
    }

    /** 校验密码
     * */
    @Override
    public Result checkPassword(String password) {
        String userId = UserHolder.getId();
        String hash = DigestUtils.md5DigestAsHex(password.getBytes());
        User user = userMapper.getPass(userId);

        if(user.getPassword().equals(hash)){
            return Result.ok();
        }
        return Result.fail("密码错误");
    }

    private static final Object userListLock = new Object();

    @Override
    public Result page(Integer pageNum, Integer pageSize, UserDTO userDTO) {
        // 1.获取全量用户列表（优先Redis）
        List<User> allList = getAllFromCache();

        // 2.内存过滤
        List<User> filteredList = allList.stream()
                .filter(item -> {
                    if (userDTO.getDeptNo() == null || userDTO.getDeptNo().isEmpty()
                            || "100001000000000".equals(userDTO.getDeptNo())) {
                        return true;
                    }
                    return userDTO.getDeptNo().equals(item.getDeptNo());
                })
                .filter(item -> {
                    if (userDTO.getRealName() == null || userDTO.getRealName().isEmpty()) {
                        return true;
                    }
                    return item.getRealName() != null
                            && item.getRealName().contains(userDTO.getRealName());
                })
                .filter(item -> {
                    if (userDTO.getEmail() == null || userDTO.getEmail().isEmpty()) {
                        return true;
                    }
                    return item.getEmail() != null
                            && item.getEmail().contains(userDTO.getEmail());
                })
                .filter(item -> {
                    if (userDTO.getDataState() == null || userDTO.getDataState().isEmpty()) {
                        return true;
                    }
                    return item.getDataState() != null
                            && userDTO.getDataState().equals(item.getDataState());
                })
                .filter(item -> {
                    if (userDTO.getPostNo() == null || userDTO.getPostNo().isEmpty()) {
                        return true;
                    }
                    return userDTO.getPostNo().equals(item.getPostNo());
                })
                .collect(java.util.stream.Collectors.toList());

        // 3.内存分页
        int total = filteredList.size();
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<User> pageRecords = (fromIndex < total)
                ? filteredList.subList(fromIndex, toIndex)
                : new java.util.ArrayList<>();

        // 4.填充角色标签
        fillRoleLabels(pageRecords);

        // 5.封装结果
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("records", pageRecords);
        data.put("pages", pages);
        data.put("total", total);

        return Result.ok(data);
    }

    /** 从Redis获取全量用户列表，缓存未命中则查DB
     * */
    private List<User> getAllFromCache() {
        List<User> list = (List<User>) redisTemplate.opsForValue().get(key);
        if (list != null && !list.isEmpty()) {
            return list;
        }
        synchronized (userListLock) {
            list = (List<User>) redisTemplate.opsForValue().get(key);
            if (list != null && !list.isEmpty()) {
                return list;
            }
            list = userMapper.listAll();
            redisTemplate.opsForValue().set(key, list, 1, java.util.concurrent.TimeUnit.HOURS);
        }
        return list;
    }

    /** 用户列表（下拉选择等）
     * */
    @Override
    public Result list(UserDTO userDTO) {
        List<User> allList = getAllFromCache();
        List<User> filteredList = allList.stream()
                .filter(item -> {
                    if (userDTO.getDeptNo() == null || userDTO.getDeptNo().isEmpty()
                            || "100001000000000".equals(userDTO.getDeptNo())) {
                        return true;
                    }
                    return userDTO.getDeptNo().equals(item.getDeptNo());
                })
                .collect(Collectors.toList());

        fillRoleLabels(filteredList);
        return Result.ok(filteredList);
    }

    /** 添加用户
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result add(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setDataState(userDTO.getDataState() != null ? userDTO.getDataState() : "0");
        user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            return Result.fail("用户姓名或邮箱已存在");
        }

        // 保存角色关联
        if (userDTO.getRoleVoIds() != null && !userDTO.getRoleVoIds().isEmpty()) {
            userMapper.insertUserRoles(user.getId(), userDTO.getRoleVoIds());
        }

        transactionAfter();
        return Result.ok("新增成功");
    }

    /** 修改用户（管理员编辑）
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result update(UserDTO userDTO) {
        User existing = findUserById(userDTO.getId());
        if (existing == null) {
            return Result.fail("用户不存在");
        }

        // 更新字段
        if (userDTO.getRealName() != null) existing.setRealName(userDTO.getRealName());
        if (userDTO.getMobile() != null) existing.setMobile(userDTO.getMobile());
        if (userDTO.getSex() != null) existing.setSex(userDTO.getSex());
        if (userDTO.getEmail() != null) existing.setEmail(userDTO.getEmail());
        if (userDTO.getDeptNo() != null) existing.setDeptNo(userDTO.getDeptNo());
        if (userDTO.getPostNo() != null) existing.setPostNo(userDTO.getPostNo());
        if (userDTO.getDataState() != null) existing.setDataState(userDTO.getDataState());

        existing.setOldUpdateTime(existing.getUpdateTime());
        userMapper.updateById(existing);

        // 更新角色关联
        if (userDTO.getRoleVoIds() != null) {
            userMapper.deleteUserRoles(userDTO.getId());
            if (!userDTO.getRoleVoIds().isEmpty()) {
                userMapper.insertUserRoles(userDTO.getId(), userDTO.getRoleVoIds());
            }
        }

        transactionAfter();
        return Result.ok("修改成功");
    }

    /** 删除用户（软删除，支持批量）
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result delete(String userIds) {
        List<Long> ids = Arrays.stream(userIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            return Result.fail("用户ID不能为空");
        }

        userMapper.softDeleteByIds(ids);
        transactionAfter();
        return Result.ok("删除成功");
    }

    /** 重置密码（默认 123456）
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result resetPassword(Long userId) {
        User user = findUserById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        userMapper.updatePassword(user);
        return Result.ok("重置成功");
    }

    /** 启用/禁用用户
     * */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result updateIsEnable(Long id, String dataState) {
        User user = findUserById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setDataState(dataState);
        user.setOldUpdateTime(user.getUpdateTime());
        userMapper.updateById(user);

        transactionAfter();
        return Result.ok("1".equals(dataState) ? "禁用成功" : "启用成功");
    }

    /** 用户信息分页（C端）
     * */
    @Override
    public Result cUserPage(Integer pageNum, Integer pageSize, String name, String idCard) {
        List<User> allList = getAllFromCache();

        // 仅返回 C 端用户
        List<User> filteredList = allList.stream()
                .filter(item -> item.getUserType() != null && item.getUserType() == 1)
                .filter(item -> {
                    if (name == null || name.isEmpty()) return true;
                    return item.getRealName() != null && item.getRealName().contains(name);
                })
                .collect(Collectors.toList());

        int total = filteredList.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<User> pageRecords = (fromIndex < total)
                ? filteredList.subList(fromIndex, toIndex)
                : new ArrayList<>();

        Map<String, Object> data = new HashMap<>();
        data.put("records", pageRecords);
        data.put("total", total);
        return Result.ok(data);
    }

    // ==================== 私有辅助方法 ====================

    /** 根据ID查找用户
     * */
    private User findUserById(Long id) {
        List<User> allList = (List<User>) redisTemplate.opsForValue().get(key);
        if (allList != null && !allList.isEmpty()) {
            return allList.stream()
                    .filter(u -> u.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }
        return userMapper.selectUserById(String.valueOf(id));
    }

    /** 填充角色标签列表（由 roleName 拆分）
     * */
    private void fillRoleLabels(List<User> users) {
        if (users == null || users.isEmpty()) return;
        for (User user : users) {
            if (user.getRoleName() != null && !user.getRoleName().isEmpty()) {
                user.setRoleLabels(Arrays.asList(user.getRoleName().split(",")));
            }
        }
    }

    /** 事务提交后清除缓存
     * */
    private void transactionAfter() {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        redisTemplate.delete(key);
                    }
                }
        );
    }
}
