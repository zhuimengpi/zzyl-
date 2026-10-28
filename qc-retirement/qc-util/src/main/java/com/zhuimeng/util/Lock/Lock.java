package com.zhuimeng.util.Lock;

/** 分布式锁接口
* */

public interface Lock {
    /** 获取锁（尝试）
    * */
    boolean tryGetLock();

    /** 释放锁
    * */
    void releaseLock();
}
