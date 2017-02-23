package com.sogou.upd.passport.session.util;

import com.google.common.collect.Maps;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.perf4j.aop.Profiled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-2
 * Time: 下午8:16
 */
public class KvUtil {

    private static Logger logger = LoggerFactory.getLogger(KvUtil.class);

    private final static String KV_PERF4J_LOGGER = "kvTimingLogger";

    public static final Logger KVTimingLogger= LoggerFactory.getLogger(KV_PERF4J_LOGGER);

    private RedisTemplate<String, String> kvTemplate;

    private String kvPrefix;

//    kv set  操作慢请求日志
    @Profiled(el = true, logger = KV_PERF4J_LOGGER, tag = "kv_set", timeThreshold = 50, normalAndSlowSuffixesEnabled = true)
    public void set(String key, String value,long timeOut) {
        String storeKey = kvPrefix+key;
        try {
            ValueOperations<String, String> valueOperations = kvTemplate.opsForValue();
            valueOperations.set(storeKey,value,timeOut, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("[Cache] set cache fail, key:" + storeKey + " value:" + value, e);
            try {
                delete(key);
            } catch (Exception ex) {
                logger.error("[Cache] set and delete cache fail, key:" + storeKey + " value:" + value, e);
            }
        }
    }

    @Profiled(el = true, logger = KV_PERF4J_LOGGER, tag = "kv_get", timeThreshold = 50, normalAndSlowSuffixesEnabled = true)
    public String get(String key) {
        String storeKey = kvPrefix+key;
        try {
            ValueOperations<String, String> valueOperations = kvTemplate.opsForValue();
            String value=valueOperations.get(storeKey);
            return value;
        } catch (Exception e) {
            logger.error("[KvCache] get cache fail, key:" + storeKey, e);
        }
        return null;
    }


    @Profiled(el = true, logger = KV_PERF4J_LOGGER, tag = "kv_delete", timeThreshold = 50, normalAndSlowSuffixesEnabled = true)
    public void delete(String key) {
        try {
            String storeKey = kvPrefix+key;
            kvTemplate.delete(storeKey);
        } catch (Exception e) {

        }
    }

    public RedisTemplate getKvTemplate() {
        return kvTemplate;
    }

    public void setKvTemplate(RedisTemplate<String, String> kvTemplate) {
        this.kvTemplate = kvTemplate;
    }

    public String getKvPrefix() {
        return kvPrefix;
    }

    public void setKvPrefix(String kvPrefix) {
        this.kvPrefix = kvPrefix;
    }

    @Profiled(el = true, logger = KV_PERF4J_LOGGER, tag = "kv_expire", timeThreshold = 50, normalAndSlowSuffixesEnabled = true)
    public void expire(String cacheKey, long timeout) {
        try {
            kvTemplate.expire(cacheKey, timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("[Cache] set cache expire fail, key:" + cacheKey + "timeout:" + timeout, e);
        }
    }

    public void hset(String key, String field, String value) {
        try {
            HashOperations<String, String, String> hashOperations = kvTemplate.opsForHash();
            hashOperations.put(key, field, value);
        } catch (Exception e) {
            logger.error("[KvCache] hset cache fail, key:" + key + " field:" + field + " value:" + value, e);
        }
    }

    public void hmset(String key, Map<String, String> hash) {
        try {
            HashOperations<String, String, String> hashOperations = kvTemplate.opsForHash();
            hashOperations.putAll(key, hash);
        } catch (Exception e) {
            logger.error("[KvCache] hmset cache fail, key:" + key + " hash:" + hash, e);
        }
    }

    public Map<String, String> hgetAll(String key) {
        Map<String, String> resultMap = Maps.newHashMap();
        try {
            HashOperations<String, String, String> hashOperations = kvTemplate.opsForHash();
            return hashOperations.entries(key);
        } catch (Exception e) {
            logger.error("[KvCache] hgetAll cache fail, key:" + key, e);
        }
        return resultMap;
    }


    public void hdel(String key, String... field) {
        if(ArrayUtils.isEmpty(field)) {
            return ;
        }
        try {
            HashOperations<String, String, String> hashOperations = kvTemplate.opsForHash();
            hashOperations.delete(key, (Object[]) field);
        } catch (Exception e) {
            logger.error("[KvCache] hdel cache fail, key:" + key + " field:" + StringUtils.join(field, ' '));
        }
    }
}

