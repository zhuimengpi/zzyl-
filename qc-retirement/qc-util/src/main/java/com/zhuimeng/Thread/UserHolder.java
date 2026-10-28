package com.zhuimeng.Thread;

import com.zhuimeng.dto.UserDTO;

/**
 * 用户上下文持有工具，基于 ThreadLocal 实现线程级用户信息隔离。
 */
public class UserHolder {

    private static final ThreadLocal<UserDTO> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前线程的用户信息
     */
    public static void set(UserDTO user) {
        USER_THREAD_LOCAL.set(user);
    }

    /**
     * 获取当前线程的用户信息
     *
     * @return 当前用户，可能为 null
     */
    public static UserDTO get() {
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 移除当前线程的用户信息（防止内存泄漏，请求结束时必须调用）
     */
    public static void remove() {
        USER_THREAD_LOCAL.remove();
    }

    // ==================== 快捷取值方法 ====================

    /**
     * 获取当前用户ID（字符串形式）
     */
    public static String getId() {
        UserDTO user = get();
        return user == null || user.getId() == null ? null : String.valueOf(user.getId());
    }

    /** 获取当前用户对象
    * */
    public static UserDTO getUserDTO() {
        return get();
    }
}
