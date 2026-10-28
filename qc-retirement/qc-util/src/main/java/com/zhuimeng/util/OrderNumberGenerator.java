package com.zhuimeng.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单号生成器，格式：ORD-YYYYMMDD-XXX
 * 示例：ORD-20260704-001
 */
public class OrderNumberGenerator {
    // 序列号计数器（全局递增，若需每日重置可配合日期缓存）
    private static final AtomicInteger counter = new AtomicInteger(1);
    // 日期格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 生成基于当前日期的订单号，序列号全局递增（从1开始）
     * @return 格式化的订单号，如 ORD-20260704-001
     */
    public static String generate() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        int seq = counter.getAndIncrement();
        return String.format("ORD-%s-%03d", datePart, seq);
    }

    /**
     * 重置序列号（例如每日零点调用，或手动重置）
     */
    public static void resetCounter() {
        counter.set(1);
    }

    /**
     * 指定日期生成（便于测试）
     * @param date 日期
     * @param seq  序列号
     * @return 格式化的订单号
     */
    public static String generateWithDate(LocalDate date, int seq) {
        String datePart = date.format(DATE_FORMATTER);
        return String.format("ORD-%s-%03d", datePart, seq);
    }
}
