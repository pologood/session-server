package com.sogou.upd.passport.session.util.redis;

import com.google.common.base.Strings;

import org.perf4j.aop.Profiled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类 User: mayan Date: 13-3-27 Time: 上午11:19 To change this template use File | Settings | File Templates.
 */
public class RedisUtils {
    
    private static Logger logger = LoggerFactory.getLogger(RedisUtils.class);
    private static final Logger redisMissLogger = LoggerFactory.getLogger("redisMissLogger");
    
    private RedisTemplate redisTemplate;
    
    private static final String ALL_REQUEST_TIMER = "REDIES_ALL_REQUEST";
    
    /*
     * 设置缓存内容
     */
    @Profiled(el = true, logger = "rediesTimingLogger", tag = "redies_set", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public void set(String key, String value) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(key, value);
        } catch (Exception e) {
            logger.error("[Cache] set cache fail, key:" + key + " value:" + value, e);
            try {
                delete(key);
            } catch (Exception ex) {
                logger.error("[Cache] set and delete cache fail, key:" + key + " value:" + value, e);
                throw new RuntimeException(e);
            }
        }
    }
    
    @Profiled(el = true, logger = "rediesTimingLogger", tag = "redies_hIncrByTimes", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public void hIncrByTimes(String cacheKey, String key, long time) {
        try {
            BoundHashOperations<String, String, String> boundHashOperations = redisTemplate.boundHashOps(cacheKey);
            boundHashOperations.increment(key, time);
        } catch (Exception e) {
            logger.error("[Cache] hIncr num cache fail, key:" + cacheKey + "value:" + key, e);
        }
    }
    
    /*
     * 设置缓存内容及有效期，单位为秒
     * TODO:是否抛出异常及如何处理
     */
    @Profiled(el = true, logger = "rediesTimingLogger", tag = "redies_setEx", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public void setWithinSeconds(String key, String value, long timeout) throws Exception {
        set(key, value, timeout, TimeUnit.SECONDS);
    }
    
    /*
     * 设置缓存内容
     */
    @Profiled(el = true, logger = "rediesTimingLogger", tag = "redies_increment", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public long increment(String key) throws Exception {
        long countNum = 0;
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            countNum = valueOperations.increment(key, 1);
        } catch (Exception e) {
            logger.error("[Cache] increment fail, key:" + key, e);
            throw e;
        }
        return countNum;
    }
    
    /*
     * 设置缓存内容
     * 冲突不覆盖
     */
    @Profiled(el = true, logger = "rediesTimingLogger", tag = "redies_setNx", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public boolean setNx(String cacheKey, Object obj) {
        try {
            BoundValueOperations boundValueOperation = redisTemplate.boundValueOps(cacheKey);
            return boundValueOperation.setIfAbsent(obj);
        } catch (Exception e) {
            logger.error("[Cache] set if absent cache fail, key:" + cacheKey + " value:" + obj, e);
            return false;
        }
    }
    
    /*
     * 根据key取缓存内容
     */
    @Profiled(el = true, logger = "rediesTimingLogger", tag = "redies_get", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public String get(String key) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            String res = valueOperations.get(key);
            if (Strings.isNullOrEmpty(res)) {
                redisMissLogger.info("get cache miss, key:" + key);
            }
            return res;
        } catch (Exception e) {
            logger.error("[Cache] get cache fail, key:" + key, e);
        }
        return null;
    }
    
    /*
     * 判断key是否存在
     */
    @Profiled(el = true, logger = "rediesTimingLogger", tag = "redies_checkKeyIsExist", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public boolean checkKeyIsExist(String key) {
        try {
            boolean res = redisTemplate.hasKey(key);
            if (!res) {
                redisMissLogger.info("checkKeyIsExist cache miss, key:" + key);
            }
            return res;
        } catch (Exception e) {
            logger.error("[Cache] check key is exist in cache fail, key:" + key, e);
            return false;
        }
    }
    
    @Profiled(el = true, logger = "rediesTimingLogger", tag = "redies_expire", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public void expire(String cacheKey, long timeout) {
        try {
            redisTemplate.expire(cacheKey, timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("[Cache] set cache expire fail, key:" + cacheKey + "timeout:" + timeout, e);
        }
    }
    
    @Profiled(el = true, logger = "rediesTimingLogger", tag = "redies_delete", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public void delete(String cacheKey) {
        redisTemplate.delete(cacheKey);
    }
    
    @Profiled(el = true, logger = "rediesTimingLogger", tag = "redies_multiDelete", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public void multiDelete(Collection cacheKeyList) {
        redisTemplate.delete(cacheKeyList);
    }
    
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /*
     * 设置缓存内容
     */
    public void set(String key, String value, long timeout, TimeUnit timeUnit) throws Exception {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            valueOperations.set(key, value, timeout, timeUnit);
        } catch (Exception e) {
            logger.error("[Cache] set cache fail, key:" + key + " value:" + value, e);
            
        }
    }
    
    /**
     * 获取key 的剩余时间
     */
    public long getExpireTime(String key) throws Exception {
        long expireSeconds = -1;
        try {
            expireSeconds = redisTemplate.getExpire(key);
        } catch (Exception e) {
            logger.error("[Cache] getExpireTime cache fail, key:" + key, e);
        }
        return expireSeconds;
    }
}
