package com.zhuimeng.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** redis key 前缀
* */
public class RedisKeyProperties {
    /** 系统用户登录
    * */
    public static final String EMPLOYEE_LOGIN_TOKEN = "login:employee:";

    /** 客户登录
    * */
    public static final String FAMILY_LOGIN_TOKEN = "login:family:";

    /** 热门房型（多用户通用）
    * */
    public static final String ROOM_TYPE = "roomType:";

    /** 服务项目列表（多用户通用）
    * */
    public static final String NURSING_PROJECT = "nursingProject:";


    /** 楼层列表（多用户通用）
     * */
    public static final String FLOOR_LIST = "floor:list:";

    /** 房型列表（多用户通用）
    * */
    public static final String FLOOR_ROOM_TYPE_LIST = "floor:roomType:list:";

    /** 房间列表（多用户通用）
    * */
    public static final String FLOOR_ROOM_LIST = "floor:room:list:";

    /** 床位列表（多用户通用）
     * */
    public static final String FLOOR_ROOM_BED_LIST = "floor:room:bed:list:";


    /** 合同列表（多用户使用）
     * */
    public static final String CONTRACT_LIST = "contract:list:";

    /** 订单（多用户使用）
    * */
    public static final String ORDER_LIST = "order:list:";

    /** 老人余额（多用户使用）
    * */
    public static final String FAMILY_BALANCE = "family:balance:";


    /** 护理等级（多用户使用）
    * */
    public static final String NURSING_LEVEL = "nursing:level:";

    /** 护理项目（多用户使用）
    * */
    public static final String NURSING_PROJECTS = "nursing:projects:";

    /** 护理计划（多用户使用）
    * */
    public static final String NURSING_PLAN = "nursing:plan:";

    /** 护理任务（多用户使用）
    * */
    public static final String NURSING_TASK = "nursing:task:";


    /** 部门（多用户使用）
    * */
    public static final String DEPT = "dept:";

    /** 岗位（多用户使用）
     */
    public static final String POST = "post:";

    /** 用户（多用户使用）
    * */
    public static final String USER = "user:";

    /** 角色（多用户使用）
    * */
    public static final String ROLE = "role:";

    /** 资源菜单（多用户使用）
    * */
    public static final String RESOURCE = "resource:";


    /** 家庭老人列表（单用户使用）
     */
    public static final String FAMILY_ELDER = "family:elder:";

    /** 预约取消次数（单用户使用）
     * */
    public static final String COUNT_RESERVATION = "reservation:count:";

    /** 预约队列（消息队列）
    * */
    public static final String RESERVATION_LIST = "reservation:list";

    /** 入住申请列表
     * */
    public static final String CHECK_IN_LIST = "checkIn:list:";

    /** 退住申请列表
     * */
    public static final String RETREAT_LIST = "retreat:list:";

    /** 订单支付计时（延时队列）
    * */
    public static final String ORDER_TIME = "order:time:";

    /** 分布式锁
    * */
    public static final String LOCK = "lock:";
}
