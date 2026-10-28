package com.zhuimeng.qcmanagement.service.impl;

import com.zhuimeng.Result.Result;
import com.zhuimeng.Thread.UserHolder;
import com.zhuimeng.dto.UserDTO;
import com.zhuimeng.entity.User;
import com.zhuimeng.entity.SysResource;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.MenuMapper;
import com.zhuimeng.qcmanagement.mapper.UserMapper;
import com.zhuimeng.qcmanagement.service.UserLoginService;
import com.zhuimeng.util.JwtUtil;
import com.zhuimeng.vo.MenuVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserLoginServiceImpl implements UserLoginService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private MenuMapper menuMapper;

    /** 账号登录
     * */
    @Override
    public Result login(String username, String password) {
        //1. 查询用户是否注册
        String hash = DigestUtils.md5DigestAsHex(password.getBytes());
        User user = userMapper.selectUserByUsername(username,hash);
        if (user == null) {
            // 用户不存在
            return Result.fail("用户登录失败");
        }

        //2. 查询用户是否被禁用
        if (user.getDataState().equals("1")) {
            // 用户被禁用
            return Result.fail("用户登录失败");
        }

        //4. 用户是否被删除
        if(user.getIsDelete() == 1){
            // 用户被删除
            return Result.fail("用户登录失败");
        }

        //5. 查询用户密码是否正确
        if(!user.getPassword().equals(hash)) {
            // 密码错误
            return Result.fail("用户登录失败");
        }

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        // 显式设置 id（User.id 为 Long，UserDTO.id 为 String，BeanUtils 无法自动转换）
        userDTO.setId(user.getId());
        String userId = String.valueOf(userDTO.getId());

        //6. 生成token
        String jwt = jwtUtil.createJwt(userId, user.getUsername());

        //7. redis存储UserDTO，1h
        String key = RedisKeyProperties.EMPLOYEE_LOGIN_TOKEN + userId;
        Map<String, Object> userMap = redisTemplate.opsForHash().entries(key);
        userMap.put("id", userId);
        userMap.put("token", jwt);
        redisTemplate.opsForHash().putAll(key, userMap);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        //8. 返回token
        return Result.ok(jwt);
    }

    /** 获取路由菜单
     * */
    @Override
    public Result getMenus() {
        // 1. 获取当前登录用户
        String userId = UserHolder.getId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        // 2. 查询用户有权限的菜单资源
        List<SysResource> resourceList = menuMapper.selectMenusByUserId(Long.valueOf(userId));
        if (resourceList == null || resourceList.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }

        // 3. 构建菜单树
        List<MenuVO> menuTree = buildMenuTree(resourceList);

        return Result.ok(menuTree);
    }

    /** 将平铺的资源列表构建为树形菜单
     * */
    private List<MenuVO> buildMenuTree(List<SysResource> resourceList) {
        // 根节点 parent_resource_no
        final String ROOT_PARENT = "100000000000000";

        // 按 resourceNo 建立节点映射
        Map<String, MenuVO> nodeMap = new HashMap<>();
        for (SysResource res : resourceList) {
            nodeMap.put(res.getResourceNo(), toMenuVO(res));
        }

        List<MenuVO> tree = new ArrayList<>();
        for (SysResource res : resourceList) {
            String parentNo = res.getParentResourceNo();
            MenuVO node = nodeMap.get(res.getResourceNo());

            if (ROOT_PARENT.equals(parentNo)) {
                // 顶级节点
                tree.add(node);
            } else {
                // 挂到父节点下
                MenuVO parent = nodeMap.get(parentNo);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(node);
                }
            }
        }

        return tree;
    }

    /** 获取按钮权限列表
     * */
    @Override
    public Result getMyButtons() {
        // 1. 获取当前登录用户
        String userId = UserHolder.getId();
        if (userId == null) {
            return Result.unauthorized("请先登录");
        }

        // 2. 查询用户有权限的按钮资源(类型 r)
        List<SysResource> buttonList = menuMapper.selectButtonsByUserId(Long.valueOf(userId));
        if (buttonList == null || buttonList.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }

        return Result.ok(buttonList);
    }

    /** 将 SysResource 转为 MenuVO
     *  DB类型: c=目录 m=菜单 → 前端类型: M=目录 C=菜单
     * */
    private MenuVO toMenuVO(SysResource resource) {
        MenuVO vo = new MenuVO();
        // 类型映射: c→M(目录), m→C(菜单)
        String type = resource.getResourceType();
        if ("c".equalsIgnoreCase(type)) {
            vo.setType("M");
        } else if ("m".equalsIgnoreCase(type)) {
            vo.setType("C");
        } else {
            vo.setType(type != null ? type.toUpperCase() : null);
        }
        vo.setPath(resource.getRequestPath());
        vo.setName(resource.getResourceName());
        vo.setIcon(resource.getIcon());
        vo.setSortNo(resource.getSortNo());
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}
