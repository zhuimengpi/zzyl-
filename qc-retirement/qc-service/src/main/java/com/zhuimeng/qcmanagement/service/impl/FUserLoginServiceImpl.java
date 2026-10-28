package com.zhuimeng.qcmanagement.service.impl;

import com.zhuimeng.Result.Result;
import com.zhuimeng.Thread.UserHolder;
import com.zhuimeng.dto.FUserDTO;
import com.zhuimeng.entity.Member;
import com.zhuimeng.entity.User;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.FUserMapper;
import com.zhuimeng.qcmanagement.service.FUserLoginService;
import com.zhuimeng.util.JwtUtil;
import com.zhuimeng.util.WechatUtil;
import com.zhuimeng.vo.FUserLoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class FUserLoginServiceImpl implements FUserLoginService {
    @Autowired
    private FUserMapper fUserMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private WechatUtil wechatUtil;

    /** 客户登录
     * */
    @Transactional(rollbackFor = {Exception.class})        //所有异常都进行回滚
    @Override
    public Result login(FUserDTO fUserDTO) {
        Long userId = null;
        String userName = fUserDTO.getNickName();
        Member user = new Member();

        // 通过微信API解密获取 openId 和手机号
        String openId = wechatUtil.getOpenId(fUserDTO.getCode());
        String phone = fUserDTO.getPhoneCode();

        // 1. 判断用户是否登陆过
        userId = fUserMapper.selectIdByUserName(fUserDTO.getNickName());
        if(userId == null){
            //用户未登录
            // 1.1 将客户存入数据库
            user.setName(userName);
            user.setOpenId(openId);
            user.setPhone(phone != null ? phone : "");
            fUserMapper.insert(user);

            // 1.2 查询客户ID
            userId = fUserMapper.selectIdByUserName(userName);
            System.out.println(userId);
        }

        // 2. 生成TOKEN
        String jwt = jwtUtil.createJwt(String.valueOf(userId),userName);

        // 3. 存入Redis
        String key = RedisKeyProperties.FAMILY_LOGIN_TOKEN + userId;
        Map<String, Object> userMap = redisTemplate.opsForHash().entries(key);
        userMap.put("id", userId.toString());
        userMap.put("token", jwt);
        redisTemplate.opsForHash().putAll(key, userMap);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        // 4. 转换为FUserLoginVO
        FUserLoginVO fUserLoginVO = new FUserLoginVO();
        BeanUtils.copyProperties(user, fUserLoginVO);
        fUserLoginVO.setNickName(userName);
        fUserLoginVO.setToken(jwt);

        // 5. 返回
        return Result.ok(fUserLoginVO);
    }

    /** 退出登录
     * */
    @Override
    public Result logout() {
        // 1. 用户Id
        String userId = UserHolder.getId();

        // 2. 删除Redis
        String key = RedisKeyProperties.FAMILY_LOGIN_TOKEN + userId;
        Boolean delete = redisTemplate.delete(key);

        // 3. 返回
        if (delete) {
            return Result.ok("退出登录成功");
        } else {
            return Result.fail("用户未登录,无法退出");
        }
    }

    /** 校验登录状态
     * */
    @Override
    public Result checkLogin(String token) {
        // 1. 判断token是否为空
        if (token == null || token.isEmpty()) {
            return Result.fail(401, "未登录");
        }

        // 2. 解析token
        Map<String, Object> map = JwtUtil.parseJwt(token);
        if (map == null || map.isEmpty()) {
            return Result.fail(401, "token无效");
        }
        String userId = map.get("id").toString();

        // 3. 查询Redis中的登录信息
        String key = RedisKeyProperties.FAMILY_LOGIN_TOKEN + userId;
        Map userMap = redisTemplate.opsForHash().entries(key);
        if (userMap.isEmpty()) {
            return Result.fail(401, "登录已过期");
        }

        // 4. 校验token是否与Redis中一致
        String redisToken = userMap.get("token").toString();
        if (!token.equals(redisToken)) {
            return Result.fail(401, "token不一致");
        }

        // 5. 校验通过
        return Result.ok();
    }
}
