package com.sogou.upd.passport.session.util;

import org.perf4j.aop.Profiled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-2
 * Time: 下午8:16
 */
public class KvUtil {

    private static Logger logger = LoggerFactory.getLogger(KvUtil.class);

    private final static String KV_PERF4J_LOGGER = "kvTimingLogger";

    private RedisTemplate kvTemplate;

    private String kvPrefix;

    @Profiled(el = true, logger = KV_PERF4J_LOGGER, tag = "kv_set")
    public void set(String key, String value) throws Exception{
        String storeKey = kvPrefix+key;
        try {
            ValueOperations<String, String> valueOperations = kvTemplate.opsForValue();
            valueOperations.set(storeKey,value);
        } catch (Exception e) {
            logger.error("[Cache] set cache fail, key:" + storeKey + " value:" + value, e);
            try {
                delete(key);
            } catch (Exception ex) {
                logger.error("[Cache] set and delete cache fail, key:" + storeKey + " value:" + value, e);
                throw e;
            }
        }
    }

    @Profiled(el = true, logger = KV_PERF4J_LOGGER, tag = "kv_get")
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


    @Profiled(el = true, logger = KV_PERF4J_LOGGER, tag = "kv_delete", timeThreshold = 100, normalAndSlowSuffixesEnabled = true)
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

    public void setKvTemplate(RedisTemplate kvTemplate) {
        this.kvTemplate = kvTemplate;
    }

    public String getKvPrefix() {
        return kvPrefix;
    }

    public void setKvPrefix(String kvPrefix) {
        this.kvPrefix = kvPrefix;
    }
}

