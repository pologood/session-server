package com.sogou.upd.passport.session.services.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.alibaba.fastjson.JSONObject;
import com.sogou.upd.passport.session.dao.SessionDao;
import com.sogou.upd.passport.session.services.SessionService;
import com.sogou.upd.passport.session.util.CommonConstant;
import com.sogou.upd.passport.session.util.KvUtil;
import com.sogou.upd.passport.session.util.SessionCommonUtil;
import com.sogou.upd.passport.session.util.redis.RedisClientTemplate;
import com.sogou.upd.passport.session.util.redis.RedisUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-11-29
 * Time: 下午5:11
 */
@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private RedisClientTemplate redisClientTemplate;
    @Autowired
    private KvUtil kvUtil;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private SessionDao sessionDao;
    @Autowired
    private RedisClientTemplate newSgidRedisClientTemplate;

    private static Logger logger = LoggerFactory.getLogger(SessionServiceImpl.class);

    private static final long ONE_MONTH = 30 * 24 * 60 * 60; // 时间 1个月 ,单位s

    private static LoadingCache<Integer, String> appLocalCache = null;

    public SessionServiceImpl() {
        appLocalCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<Integer, String>() {
                @Override
                public String load(Integer clientId) throws Exception {
                    return queryAppConfigByClientId(clientId);
                }
            });
    }

    @Override
    public JSONObject getSession(String sgid) {
        // 判断新旧 sgid
        int lastIndex = sgid.lastIndexOf('-');
        if (lastIndex > 0) { // 新 sgid
            String prefix = sgid.substring(0, lastIndex);
            String realSgid = sgid.substring(lastIndex + 1);
            return getNewSgidSession(prefix, realSgid);
        } else {    // 旧 sgid
            return getOldSgidSession(sgid);
        }
    }

    /**
     * 获取新 sgid <br>
     * 因为使用 hash 存储，所以只能手动维护过期时间。为保证 hash 下不会保存大量无效 field，故会将失败的 field 删除，并对不足一半有效期的进行续期
     */
    private JSONObject getNewSgidSession(String prefix, String sgid) {
        //先从redis中获取
        String cacheKey = CommonConstant.PREFIX_SESSION + prefix;

        long currentTimeMillis = System.currentTimeMillis();

        // 待更新 fields
        Map<String, String> updateFieldsMap = Maps.newHashMap();
        // 待删除 fields
        List<String> delFieldsList = Lists.newArrayList();

        JSONObject jsonResult = null;

        Map<String, String> valueMap = newSgidRedisClientTemplate.hgetAll(cacheKey);
        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            // 存储的 sgid （field）
            String cachedSgid = entry.getKey();
            // 存储的 passport id，有效期 等信息 （value）
            String userInfo = entry.getValue();
            JSONObject userInfoJson = JSONObject.parseObject(userInfo);

            // 有效期
            String expire = (String) userInfoJson.get(CommonConstant.REDIS_KEY_EXPIRE);
            // 剩余时间
            long leftTime = Long.parseLong(expire) - currentTimeMillis;
            if (leftTime <= 0) { // 超过有效期
                // 加入待删除列表
                delFieldsList.add(cachedSgid);
                continue;
            } else if (leftTime <= CommonConstant.SESSION_EXPIRSE_HALF) { // 不足一半有效期
                // 计算新过期时间
                long expireTime = (System.currentTimeMillis() / 1000) + CommonConstant.SESSION_EXPIRSE;
                userInfoJson.put(CommonConstant.REDIS_KEY_EXPIRE, expireTime);
                updateFieldsMap.put(cachedSgid, userInfoJson.toJSONString());
            }

            if (StringUtils.equals(cachedSgid, sgid)) { // 当前 sgid
                jsonResult = userInfoJson;
            }
        }

        if (jsonResult == null) { // 未取到
            return null;
        }

        if (delFieldsList.size() > 0) { // 删除过期 sgid
            newSgidRedisClientTemplate.hdel(cacheKey, delFieldsList.toArray(new String[delFieldsList.size()]));

        }
        if (updateFieldsMap.size() > 0) { // 待更新的 field
            newSgidRedisClientTemplate.hmset(cacheKey, updateFieldsMap);
        }

        // 对有效的且剩余生命不足有效期一半的 key 进行续期
        // ttl 返回，key 不存在 -2，未设置过期时间 -1，正常设置返回剩余时间
        Long leftTime = redisClientTemplate.ttl(cacheKey);
        if ((leftTime != null) && (leftTime <= CommonConstant.SESSION_EXPIRSE_HALF)) {
            newSgidRedisClientTemplate.expire(cacheKey, CommonConstant.SESSION_EXPIRSE);
        }

        // 返回结果中去掉过期时间，防止业务线误存此值进行自有逻辑判断
        // 业务线自己判断会依赖本地时间，并且此过期时间会由于续期而产生变化
        jsonResult.remove(CommonConstant.REDIS_KEY_EXPIRE);

        return jsonResult;
    }

    /**
     * 获取旧 sgid
     */
    private JSONObject getOldSgidSession(String sgid) {
        //先从redis中获取
        String key = CommonConstant.PREFIX_SESSION + sgid;
        String value = redisClientTemplate.get(key);

        // 再从kv中获取
        if (StringUtils.isBlank(value)) {
            value = kvUtil.get(key);
            //kv中存在则存入redis中

            if (StringUtils.isBlank(value)) {
                return null;
            }
            redisClientTemplate.set(key, value);
        }

        // 对有效的且剩余生命不足有效期一半的 sgid 进行续期
        // ttl 返回，key 不存在 -2，未设置过期时间 -1，正常设置返回剩余时间
        Long leftTime = redisClientTemplate.ttl(key);
        if ((leftTime != null) && (leftTime <= CommonConstant.SESSION_EXPIRSE_HALF)) {
            redisClientTemplate.expire(key, CommonConstant.SESSION_EXPIRSE);
            kvUtil.set(key, value, CommonConstant.SESSION_EXPIRSE);
        }

        try {
            return JSONObject.parseObject(value);
        } catch (Exception e) {
            logger.error("value parse json error, value:" + value);
        }

        return null;
    }

    @Override
    public void setSession(String sgid, String userInfo) {
        // 判断新旧 sgid
        int lastIndex = sgid.lastIndexOf('-');
        if (lastIndex <= 0) {
            // 非法 sgid
            logger.error("invalid sgid:" + sgid);
            return;
        }

        // sgid 前缀 [分表索引]-[account 自增 id]
        String prefix = sgid.substring(0, lastIndex);
        // 真实 sgid
        String realSgid = sgid.substring(lastIndex + 1);

        String cacheKey = CommonConstant.PREFIX_SESSION + prefix;

        // 维护 sgid 的过期时间
        JSONObject userInfoJson = JSONObject.parseObject(userInfo);
        long expire = (System.currentTimeMillis() / 1000) + CommonConstant.SESSION_EXPIRSE;
        userInfoJson.put("expire", expire);

        // 设置 field 和 key 的失效时间
        newSgidRedisClientTemplate.hset(cacheKey, realSgid, userInfoJson.toJSONString());
        newSgidRedisClientTemplate.expire(cacheKey, CommonConstant.SESSION_EXPIRSE);
    }

    @Override
    public void deleteSession(String sgid) {
        // 判断新旧 sgid
        int lastIndex = sgid.lastIndexOf('-');
        if (lastIndex > 0) { // 新 sgid
            String prefix = sgid.substring(0, lastIndex);
            String realSgid = sgid.substring(lastIndex + 1);

            newSgidRedisClientTemplate.hdel(prefix, realSgid);
        } else {    // 旧 sgid
            String key = CommonConstant.PREFIX_SESSION + sgid;
            redisClientTemplate.del(key);
            kvUtil.delete(key);
        }
    }

    private String loadAppServerSecret(int clientId) {
        String serverSecret = null;
        if (appLocalCache != null) {
            try {
                serverSecret = appLocalCache.get(clientId);
            } catch (Exception e) {
                logger.warn("loadAppServerSecret fail,clientId:" + clientId, e);
                return null;
            }
        } else {
            logger.error("appLocalCache initial,failed");
        }
        if (StringUtils.isBlank(serverSecret)) {
            serverSecret = queryAppConfigByClientId(clientId);
        }

        return serverSecret;
    }

    @Override
    public String queryAppConfigByClientId(int clientId) {
        String cacheKey = buildAppConfigCacheKey(clientId);
        String serverSecret = redisUtils.get(cacheKey);
        if (StringUtils.isBlank(serverSecret)) {
            serverSecret = sessionDao.queryAppConfigByClientId(clientId);
            if (StringUtils.isNotBlank(serverSecret)) {
                redisUtils.setWithinSeconds(cacheKey, serverSecret, ONE_MONTH);
            }
        }

        return serverSecret;
    }

    @Override
    public boolean checkCode(String sgid, int clientId, String code, long ct) {
        String serverSecret = loadAppServerSecret(clientId);

        String actualCode = sgid + clientId + serverSecret + ct;
        String actualCodeMD5Hex = SessionCommonUtil.calculateMD5Hex(actualCode);
        boolean result = actualCodeMD5Hex.equals(code);
        if (logger.isDebugEnabled()) {
            logger.debug("actualCode:" + actualCode + ",code:" + actualCodeMD5Hex + ",result:" + result);
        }
        if (!result) {
            logger.warn("actualCode:" + actualCode + ",params_code:" + code + ",code:" + actualCode + ",result:" + result);
        }
        return result;
    }

    private String buildAppConfigCacheKey(int client_id) {
        return "SP.CLIENTID:APPCONFIG_SECRET_" + client_id;
    }
}
