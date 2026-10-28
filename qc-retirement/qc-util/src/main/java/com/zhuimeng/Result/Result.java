package com.zhuimeng.Result;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 统一返回结果类
 * <p>
 * 前端约定：
 * <ul>
 *   <li>{@code code = 200} 表示成功</li>
 *   <li>{@code code = 500} 表示服务端错误</li>
 *   <li>{@code code = 400} 表示参数校验失败</li>
 *   <li>{@code code = 401} 表示未认证/Token过期</li>
 *   <li>{@code code = 403} 表示无权限</li>
 *   <li>{@code code = 404} 表示资源不存在</li>
 * </ul>
 *
 * <pre>{@code
 * // 典型用法：
 * Result.ok(data)                     // 成功返回数据
 * Result.ok()                         // 成功无数据
 * Result.fail("错误信息")              // 失败
 * Result.page(list, total)            // 分页数据
 * }</pre>
 *
 * @param <T> 响应数据类型
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 标准状态码 ====================

    /** 成功 */
    public static final int CODE_SUCCESS = 200;
    /** 参数错误 */
    public static final int CODE_BAD_REQUEST = 400;
    /** 未认证 */
    public static final int CODE_UNAUTHORIZED = 401;
    /** 无权限 */
    public static final int CODE_FORBIDDEN = 403;
    /** 资源不存在 */
    public static final int CODE_NOT_FOUND = 404;
    /** 请求方法不支持 */
    public static final int CODE_METHOD_NOT_ALLOWED = 405;
    /** 业务逻辑错误 */
    public static final int CODE_BUSINESS_ERROR = 409;
    /** 参数校验失败 */
    public static final int CODE_VALIDATION_ERROR = 422;
    /** 服务端错误 */
    public static final int CODE_SERVER_ERROR = 500;

    // ==================== 字段 ====================

    /** 状态码 */
    private int code;

    /** 提示信息 */
    private String msg;

    /** 响应数据 */
    private T data;

    // ==================== 构造器 ====================

    public Result() {
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ==================== 成功响应 ====================

    /**
     * 成功（无数据）
     */
    public static <T> Result<T> ok() {
        return new Result<>(CODE_SUCCESS, "操作成功", null);
    }

    /**
     * 成功（带数据）
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(CODE_SUCCESS, "操作成功", data);
    }

    /**
     * 成功（自定义消息 + 数据）
     */
    public static <T> Result<T> ok(String msg, T data) {
        return new Result<>(CODE_SUCCESS, msg, data);
    }

    /**
     * 成功（仅自定义消息）
     */
    public static <T> Result<T> okMsg(String msg) {
        return new Result<>(CODE_SUCCESS, msg, null);
    }

    // ==================== 失败响应 ====================

    /**
     * 失败（自定义状态码 + 消息）
     */
    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 失败（自定义消息，默认 code=500）
     */
    public static <T> Result<T> fail(String msg) {
        return new Result<>(CODE_SERVER_ERROR, msg, null);
    }

    /**
     * 失败（自定义状态码 + 消息 + 数据）
     */
    public static <T> Result<T> fail(int code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    // ==================== 快捷错误响应 ====================

    /**
     * 参数校验失败
     */
    public static <T> Result<T> badRequest(String msg) {
        return new Result<>(CODE_BAD_REQUEST, msg, null);
    }

    /**
     * 未登录/Token过期
     */
    public static <T> Result<T> unauthorized(String msg) {
        return new Result<>(CODE_UNAUTHORIZED, msg == null ? "请先登录" : msg, null);
    }

    /**
     * 无权限
     */
    public static <T> Result<T> forbidden(String msg) {
        return new Result<>(CODE_FORBIDDEN, msg == null ? "权限不足" : msg, null);
    }

    /**
     * 资源不存在
     */
    public static <T> Result<T> notFound(String msg) {
        return new Result<>(CODE_NOT_FOUND, msg == null ? "资源不存在" : msg, null);
    }

    /**
     * 服务器内部错误
     */
    public static <T> Result<T> serverError(String msg) {
        return new Result<>(CODE_SERVER_ERROR, msg == null ? "服务器内部错误" : msg, null);
    }

    // ==================== 分页响应 ====================

    /**
     * 分页成功返回
     *
     * @param list  当前页数据列表
     * @param total 总记录数
     */
    public static <T> Result<PageData<T>> page(List<T> list, long total) {
        return ok(new PageData<>(list, total));
    }

    /**
     * 分页成功返回（含分页参数）
     *
     * @param list     当前页数据列表
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     */
    public static <T> Result<PageData<T>> page(List<T> list, long total, long pageNum, long pageSize) {
        return ok(new PageData<>(list, total, pageNum, pageSize));
    }

    /**
     * 分页成功返回（空数据）
     */
    public static <T> Result<PageData<T>> pageEmpty() {
        return ok(new PageData<>(Collections.emptyList(), 0));
    }

    // ==================== 布尔响应 ====================

    /**
     * 根据布尔值返回成功或失败
     */
    public static <T> Result<T> of(boolean success) {
        return success ? ok() : fail("操作失败");
    }

    /**
     * 根据布尔值返回，自定义失败消息
     */
    public static <T> Result<T> of(boolean success, String failMsg) {
        return success ? ok() : fail(failMsg);
    }

    // ==================== 链式调用 ====================

    public Result<T> code(int code) {
        this.code = code;
        return this;
    }

    public Result<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    public Result<T> data(T data) {
        this.data = data;
        return this;
    }

    // ==================== 判断方法 ====================

    public boolean isSuccess() {
        return this.code == CODE_SUCCESS;
    }

    public boolean isFail() {
        return !isSuccess();
    }

    // ==================== Getter / Setter ====================

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // ==================== toString ====================

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    // ==================== 内部分页数据类 ====================

    /**
     * 分页数据结构，与前端 PageData 约定一致。
     * <p>
     * JSON 序列化后格式：
     * <pre>{@code
     * {
     *   "code": 200,
     *   "msg": "操作成功",
     *   "data": {
     *     "list": [...],
     *     "total": 100,
     *     "pageNum": 1,
     *     "pageSize": 10
     *   }
     * }
     * }</pre>
     */
    public static class PageData<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 当前页数据 */
        private List<T> list;

        /** 总记录数 */
        private long total;

        /** 当前页码 */
        private long pageNum;

        /** 每页条数 */
        private long pageSize;

        public PageData() {
        }

        public PageData(List<T> list, long total) {
            this.list = list;
            this.total = total;
        }

        public PageData(List<T> list, long total, long pageNum, long pageSize) {
            this.list = list;
            this.total = total;
            this.pageNum = pageNum;
            this.pageSize = pageSize;
        }

        /** 计算总页数 */
        public long getPages() {
            if (pageSize <= 0) {
                return 0;
            }
            return (total + pageSize - 1) / pageSize;
        }

        // ==================== Getter / Setter ====================

        public List<T> getList() {
            return list;
        }

        public void setList(List<T> list) {
            this.list = list;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public long getPageNum() {
            return pageNum;
        }

        public void setPageNum(long pageNum) {
            this.pageNum = pageNum;
        }

        public long getPageSize() {
            return pageSize;
        }

        public void setPageSize(long pageSize) {
            this.pageSize = pageSize;
        }

        @Override
        public String toString() {
            return "PageData{" +
                    "list=" + list +
                    ", total=" + total +
                    ", pageNum=" + pageNum +
                    ", pageSize=" + pageSize +
                    '}';
        }
    }
}
