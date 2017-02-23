package com.sogou.upd.passport.session.util.redis;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.perf4j.aop.Profiled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类 User: mayan Date: 13-3-27 Time: 上午11:19 To change this template use File | Settings | File Templates.
 */
public class NewSgidRedisUtils {

    private static Logger logger = LoggerFactory.getLogger(NewSgidRedisUtils.class);
    private static final Logger newSgidredisMissLogger = LoggerFactory.getLogger("newSgidredisMissLogger");

    private RedisTemplate<String, String> redisTemplate;

    private static final String ALL_REQUEST_TIMER = "REDIES_ALL_REQUEST";

    /*
     * 设置缓存内容
     */
    @Profiled(el = true, logger = "newSgidRediesTimingLogger", tag = "new_sgid_redies_set", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
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
            }
        }
    }

    @Profiled(el = true, logger = "newSgidRediesTimingLogger", tag = "new_sgid_redies_hIncrByTimes", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
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
    @Profiled(el = true, logger = "newSgidRediesTimingLogger", tag = "new_sgid_redies_setEx", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public void setWithinSeconds(String key, String value, long timeout) throws Exception {
        set(key, value, timeout, TimeUnit.SECONDS);
    }

    /*
     * 设置缓存内容
     */
    @Profiled(el = true, logger = "newSgidRediesTimingLogger", tag = "new_sgid_redies_increment", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
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
    @Profiled(el = true, logger = "newSgidRediesTimingLogger", tag = "new_sgid_redies_setNx", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
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
    @Profiled(el = true, logger = "newSgidRediesTimingLogger", tag = "new_sgid_redies_get", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public String get(String key) {
        try {
            ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
            String res = valueOperations.get(key);
            if (Strings.isNullOrEmpty(res)) {
                newSgidredisMissLogger.info("get cache miss, key:" + key);
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
    @Profiled(el = true, logger = "newSgidRediesTimingLogger", tag = "new_sgid_redies_checkKeyIsExist", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public boolean checkKeyIsExist(String key) {
        try {
            boolean res = redisTemplate.hasKey(key);
            if (!res) {
                newSgidredisMissLogger.info("checkKeyIsExist cache miss, key:" + key);
            }
            return res;
        } catch (Exception e) {
            logger.error("[Cache] check key is exist in cache fail, key:" + key, e);
            return false;
        }
    }

    @Profiled(el = true, logger = "newSgidRediesTimingLogger", tag = "new_sgid_redies_expire", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public void expire(String cacheKey, long timeout) {
        try {
            redisTemplate.expire(cacheKey, timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("[Cache] set cache expire fail, key:" + cacheKey + "timeout:" + timeout, e);
        }
    }

    @Profiled(el = true, logger = "newSgidRediesTimingLogger", tag = "new_sgid_redies_delete", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public void delete(String cacheKey) {
        try {
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            logger.error("[Cache] delete cache fail, key:" + cacheKey, e);
        }
    }

    @Profiled(el = true, logger = "newSgidRediesTimingLogger", tag = "new_sgid_redies_multiDelete", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    public void multiDelete(Collection<String> cacheKeyList) {
        try {
            redisTemplate.delete(cacheKeyList);
        } catch (Exception e) {
            logger.error("[Cache] multi delete cache fail, key:" + StringUtils.join(cacheKeyList, ' '), e);
        }
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

    public void hset(String key, String field, String value) {
        try {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            hashOperations.put(key, field, value);
        } catch (Exception e) {
            logger.error("[Cache] hset cache fail, key:" + key + " field:" + field + " value:" + value, e);
        }
    }

    public void hmset(String key, Map<String, String> hash) {
        try {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            hashOperations.putAll(key, hash);
        } catch (Exception e) {
            logger.error("[Cache] hmset cache fail, key:" + key + " hash:" + hash, e);
        }
    }

    public Map<String, String> hgetAll(String key) {
        Map<String, String> resultMap = Maps.newHashMap();
        try {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            return hashOperations.entries(key);
        } catch (Exception e) {
            logger.error("[Cache] hgetAll cache fail, key:" + key, e);
        }
        return resultMap;
    }

    public void hdel(String key, String... field) {
        if(ArrayUtils.isEmpty(field)) {
            return ;
        }
        try {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            hashOperations.delete(key, (Object[]) field);
        } catch (Exception e) {
            logger.error("[Cache] hdel cache fail, key:" + key + " field:" + StringUtils.join(field, ' '));
        }
    }
}
