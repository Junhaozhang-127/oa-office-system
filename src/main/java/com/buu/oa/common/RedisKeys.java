package com.buu.oa.common;

/**
 * Redis Key常量
 * 统一管理所有Redis Key，避免硬编码字符串散落
 */
public final class RedisKeys {

    private RedisKeys() {}

    /** 延迟提醒ZSet Key，score=提醒时间戳，value=任务标识 */
    public static final String REMINDER_ZSET = "oa:reminder:zset";

    /** 未读消息计数缓存前缀，格式: oa:unread:count:{userId} */
    public static final String UNREAD_COUNT_PREFIX = "oa:unread:count:";
}
