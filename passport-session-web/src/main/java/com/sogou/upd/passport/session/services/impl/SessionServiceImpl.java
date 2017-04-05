package com.sogou.upd.passport.session.services.impl;

import com.google.common.base.Strings;
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
import com.sogou.upd.passport.session.util.SessionServerUtil;
import com.sogou.upd.passport.session.util.redis.RedisClientTemplate;
import com.sogou.upd.passport.session.util.redis.RedisUtils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("redisClientTemplate")
    private RedisClientTemplate redisClientTemplate;
    @Autowired
    private KvUtil kvUtil;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private SessionDao sessionDao;
    @Autowired
    @Qualifier("newSgidRedisClientTemplate")
    private RedisClientTemplate newSgidRedisClientTemplate;

    private static Logger logger = LoggerFactory.getLogger(SessionServiceImpl.class);

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
    private JSONObject getNewSgidSession(final String prefix, final String sgid) {
        final JSONObject resultUserInfoJson = new JSONObject();

        // 遍历查找 sgid
        iterateNewSgidSession(prefix, null, new IterateNewSgidCallback() {
            @Override
            public void callback(String passportId, String cachedSgid, JSONObject cachedInfoJson, long leftTime) {
                if (StringUtils.equals(cachedSgid, sgid)) { // 当前 sgid
                    boolean isWap = BooleanUtils.isTrue(cachedInfoJson.getBoolean(CommonConstant.REDIS_SGID_ISWAP));
                    // we need to re-calculate the expire date for WAP client
                    if (isWap && (leftTime <= CommonConstant.SESSION_EXPIRSE_HALF)) { // wap 登录，不足一半有效期的续期
                        // 重新计算有效期
                        long expireTime = (System.currentTimeMillis() / 1000) + CommonConstant.SESSION_EXPIRSE;
                        cachedInfoJson.put(CommonConstant.REDIS_SGID_EXPIRE, expireTime);

                        // 更新失效时间
                        String cacheKey = CommonConstant.PREFIX_SESSION + prefix;
                        newSgidRedisClientTemplate.hset(cacheKey, cachedSgid, cachedInfoJson.toJSONString());
                        newSgidRedisClientTemplate.expire(cacheKey, CommonConstant.SESSION_EXPIRSE);
                    }
                }

                // 返回结果-账号
                resultUserInfoJson.put(CommonConstant.REDIS_PASSPORTID, passportId);
                // 返回结果-阅读返回微信 openId
                if (cachedInfoJson.containsKey(CommonConstant.REDIS_SGID_WEIXIN_OPENID)) {
                    String weixinOpenid = cachedInfoJson.getString(CommonConstant.REDIS_SGID_WEIXIN_OPENID);
                    resultUserInfoJson.put(CommonConstant.REDIS_SGID_WEIXIN_OPENID, weixinOpenid);
                }
            }
        });

        return resultUserInfoJson;
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
    public void setSession(String sgid, String userInfo, boolean isWap) {
        // 判断新旧 sgid

        int lastIndex = sgid.lastIndexOf('-');
        // 生成新 sgid
        int sessionExpirse = isWap ? CommonConstant.SESSION_EXPIRSE : CommonConstant.SESSION_EXPIRSE_TWO_WEEKS;
        // sgid 前缀 [分表索引]-[account 自增 id]
        String prefix = sgid.substring(0, lastIndex);
        // 真实 sgid
        String realSgid = sgid.substring(lastIndex + 1);

        String cacheKey = CommonConstant.PREFIX_SESSION + prefix;
        // 待删除 fields
        List<String> delFieldsList = Lists.newArrayList();
        // 当前时间
        long currentTimeMillis = System.currentTimeMillis();

        // clear the session
        // clear the expired session
        Map<String, String> valueMap = newSgidRedisClientTemplate.hgetAll(cacheKey);
        if (valueMap != null && valueMap.size() > 0) {
            for (Map.Entry<String, String> entry : valueMap.entrySet()) {
                // 存储的 sgid （field）
                String cachedSgid = entry.getKey();
                /**
                 * If this the property for passport_id, do nothing
                 */
                if (CommonConstant.REDIS_PASSPORTID.equals(cachedSgid)) {
                    continue;
                }

                // 存储的 passport id，有效期 等信息 （value）
                String sgidInfo = entry.getValue();
                JSONObject sgidInfoJson = JSONObject.parseObject(sgidInfo);

                // 有效期
                int expire = (Integer) sgidInfoJson.get(CommonConstant.REDIS_SGID_EXPIRE);
                // 剩余时间
                long leftTime = expire - (currentTimeMillis / 1000);
                if (leftTime <= 0) { // 超过有效期
                    // 加入待删除列表
                    delFieldsList.add(cachedSgid);
                    logger.warn("sid delete expired sgid in set method cachekey:{} userinfo:{} del_sgid:{} expire:{}", prefix, userInfo, cachedSgid, expire);
                }
            }
        }
        if (delFieldsList.size() > 0) { // 删除过期 sgid
            newSgidRedisClientTemplate.hdel(cacheKey, delFieldsList.toArray(new String[delFieldsList.size()]));
        }

        // 维护 sgid 的过期时间
        // the new session format of redis
        /**
         * 1. Save entity for every sgid
         * 2. If the sgid is not from WAP, we ignore "isWap" property
         *
         * sgid1={"expire":1491806305,}
         * sgid2={"expire":1491806306, "isWap":true}
         * passport_id=codetest1@sogou.com
         */
        JSONObject userInfoJson = JSONObject.parseObject(userInfo);
        JSONObject sgidInfoJson = new JSONObject();
        long expire = (System.currentTimeMillis() / 1000) + sessionExpirse;
        sgidInfoJson.put("expire", expire);
        if (isWap) { // save into redis when the request from WAP
            sgidInfoJson.put("isWap", isWap);
        }

        // 设置 field 和 key 的失效时间
        newSgidRedisClientTemplate.hset(cacheKey, realSgid, sgidInfoJson.toJSONString());
        String passportId = (String) userInfoJson.get(CommonConstant.REDIS_PASSPORTID);
        if (!Strings.isNullOrEmpty(passportId)) { // if the passport is not NULL or empty string
            newSgidRedisClientTemplate.hset(cacheKey, CommonConstant.REDIS_PASSPORTID, passportId);
        }
        newSgidRedisClientTemplate.expire(cacheKey, CommonConstant.SESSION_EXPIRSE);
        logger.warn("sid set sgid:" + sgid + " userinfo:" + userInfo);
    }

    @Override
    public String newSession(String prefix, String passportId, String weixinOpenId, boolean isWap) {
        String cacheKey = CommonConstant.PREFIX_SESSION + prefix;

        // 新 sgid
        String newSgid = iterateNewSgidSession(prefix, isWap);
        if (StringUtils.isBlank(newSgid)) {
            newSgid = SessionServerUtil.createSessionSid(passportId);
        }

        // 维护 sgid 的过期时间
        // the new session format of redis
        /**
         * 1. Save entity for every sgid
         * 2. If the sgid is not from WAP, we ignore "isWap" property
         *
         * sgid1={"expire":1491806305,}
         * sgid2={"expire":1491806306, "isWap":true}
         * passport_id=codetest1@sogou.com
         */
        JSONObject sgidInfoJson = new JSONObject();
        // 过期时间
        int sessionExpire = isWap ? CommonConstant.SESSION_EXPIRSE : CommonConstant.SESSION_EXPIRSE_TWO_WEEKS;
        long expire = (System.currentTimeMillis() / 1000) + sessionExpire;

        sgidInfoJson.put(CommonConstant.REDIS_SGID_EXPIRE, expire);
        if (isWap) { // save into redis when the request from WAP
            sgidInfoJson.put(CommonConstant.REDIS_SGID_ISWAP, isWap);
        }
        // 阅读需要获取微信 openId 来进行消息推送
        if (StringUtils.isNotBlank(weixinOpenId)) {
            sgidInfoJson.put(CommonConstant.REDIS_SGID_WEIXIN_OPENID, weixinOpenId);
        }

        // 设置 field 和 key 的失效时间
        newSgidRedisClientTemplate.hset(cacheKey, newSgid, sgidInfoJson.toJSONString());
        newSgidRedisClientTemplate.hset(cacheKey, CommonConstant.REDIS_PASSPORTID, passportId);
        newSgidRedisClientTemplate.expire(cacheKey, CommonConstant.SESSION_EXPIRSE);

        String resultSgid = prefix + "-" + newSgid;
        logger.warn("sid set sgid:{} passportId:{} userinfo:{}", resultSgid, passportId, sgidInfoJson);

        return resultSgid;
    }

    @Override
    public void deleteSession(String sgid) {
        // 判断新旧 sgid
        int lastIndex = sgid.lastIndexOf('-');
        if (lastIndex > 0) { // 新 sgid
            String prefix = sgid.substring(0, lastIndex);
            String realSgid = sgid.substring(lastIndex + 1);
            // get the userinfo and print
            JSONObject userInfo = getNewSgidSession(prefix, realSgid);
            String key = CommonConstant.PREFIX_SESSION + prefix;
            newSgidRedisClientTemplate.hdel(key, realSgid);
            if (userInfo != null) {
                logger.warn("sid delete sgid:{} userInfo:{}", sgid, userInfo.toJSONString());
            } else {
                logger.warn("sid delete sgid:{}", sgid);
            }
        } else {    // 旧 sgid
            String key = CommonConstant.PREFIX_SESSION + sgid;
            redisClientTemplate.del(key);
            kvUtil.delete(key);
            logger.warn("sid delete sgid:" + sgid);
        }
    }

    /**
     * 遍历 sgid 的回调接口 <br>
     * 遍历 sgid 过程中涉及更新有效期、删除过期 sgid、去除多余信息
     */
    private interface IterateNewSgidCallback {

        void callback(String passportId, String cachedSgid, JSONObject cachedInfoJson, long leftTime);
    }

    private String iterateNewSgidSession(String prefix, Boolean isWap) {
        return iterateNewSgidSession(prefix, isWap, null);
    }

    /**
     * 遍历账号下所有新 sgid <br>
     * 对所有 sgid 进行检查，删除过期的 sgid，更新格式，打印日志
     */
    private String iterateNewSgidSession(String prefix, Boolean isWap, IterateNewSgidCallback iterateSgidCallback) {
        //先从redis中获取
        String cacheKey = CommonConstant.PREFIX_SESSION + prefix;

        long currentTimeMillis = System.currentTimeMillis();

        // 存储的 sgid 数量
        int cachedSgidCount = 0;
        // 最早过期时间
        long earliestExpire = (currentTimeMillis / 1000) + CommonConstant.SESSION_EXPIRSE;
        // 最早过期时间对应用的 sgid，默认为新生成的
        String earliestSgid = "";

        Map<String, String> valueMap = newSgidRedisClientTemplate.hgetAll(cacheKey);
        if (MapUtils.isNotEmpty(valueMap)) {
            // 需要移除 json 中的 passportId
            boolean needMovePassportId = false;
            // 待更新 fields
            Map<String, String> updateFieldsMap = Maps.newHashMap();
            // 待删除 fields
            List<String> delFieldsList = Lists.newArrayList();

            String passportId = valueMap.get(CommonConstant.REDIS_PASSPORTID);
            for (Map.Entry<String, String> entry : valueMap.entrySet()) {
                // 存储的 sgid （field）
                String cachedSgid = entry.getKey();

                /**
                 * If this the property for passport_id, do nothing
                 */
                if (CommonConstant.REDIS_PASSPORTID.equals(cachedSgid)) {
                    continue;
                }

                String cachedInfo = entry.getValue();
                JSONObject cachedInfoJson = JSONObject.parseObject(cachedInfo);

                // 有效期
                int expire = (Integer) cachedInfoJson.get(CommonConstant.REDIS_SGID_EXPIRE);
                // 剩余时间
                long leftTime = expire - (currentTimeMillis / 1000);
                if (leftTime <= 0) { // 超过有效期
                    // 加入待删除列表
                    delFieldsList.add(cachedSgid);
                    logger.warn("sid delete expired sgid in get method. sgid{}: expire:{} passportId:{}", cachedSgid, expire, passportId);
                    continue; // 已经过期，不再做后续操作
                }

                // 记录 passportId
                if (cachedInfoJson.containsKey(CommonConstant.REDIS_PASSPORTID)) {
                    passportId = cachedInfoJson.getString(CommonConstant.REDIS_PASSPORTID);
                    needMovePassportId = true;
                    // remove the passport_id from sgid property and update the redis
                    cachedInfoJson.remove(CommonConstant.REDIS_PASSPORTID);
                    updateFieldsMap.put(cachedSgid, cachedInfoJson.toJSONString());
                }

                // 移除 isWap 标记
                boolean isCachedSgidWap = BooleanUtils.isTrue(cachedInfoJson.getBoolean(CommonConstant.REDIS_SGID_ISWAP));
                if (!isCachedSgidWap && cachedInfoJson.containsKey(CommonConstant.REDIS_SGID_ISWAP)) { // remove the original isWap=false
                    cachedInfoJson.remove(CommonConstant.REDIS_SGID_ISWAP);
                    updateFieldsMap.put(cachedSgid, cachedInfoJson.toJSONString());
                }

                // cachedSgid 是否是 wap
                if (isWap != null && (isWap == isCachedSgidWap)) { // 寻找相同类型最早过期 sgid
                    // 计数 +1
                    cachedSgidCount++;
                    if (expire <= earliestExpire) { // 记录最早过期时间和对应的 sgid
                        earliestExpire = expire;
                        earliestSgid = cachedSgid;
                    }
                }

                // 循环回调
                if (iterateSgidCallback != null) {
                    iterateSgidCallback.callback(passportId, cachedSgid, cachedInfoJson, leftTime);
                }
            }

            // need to move the passport_id property from sgid to user cache
            if (needMovePassportId) {
                updateFieldsMap.put(CommonConstant.REDIS_PASSPORTID, passportId);
            }

            if (delFieldsList.size() > 0) { // 删除过期 sgid
                newSgidRedisClientTemplate.hdel(cacheKey, delFieldsList.toArray(new String[delFieldsList.size()]));
            }

            if (updateFieldsMap.size() > 0) { // 待更新的 field
                newSgidRedisClientTemplate.hmset(cacheKey, updateFieldsMap);
            }

            // log the fields info when there is more than 5 sgid field for one customer
            if (newSgidRedisClientTemplate.hlen(cacheKey) > 5) {
                Map<String, String> updatedValueMap = newSgidRedisClientTemplate.hgetAll(cacheKey);
                logger.warn("sid get sgid more than 5 fields. cachekey:{} size:{} fields:{}", prefix, updatedValueMap.size(), updatedValueMap.toString());
            }

            String newSgid = null;
            if (cachedSgidCount >= 10) { // 存在 10 个及以上的 sgid
                // 此种策略虽然会造成登录 10 次以后，之后再登录都会返回相同的 sgid
                // 但若正常登录行为，由于有 cookie 和 sgid 保持登录状态的机制，不会造成重复登录
                newSgid = earliestSgid;
            }

            return newSgid;
        }

        return null;
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
                redisUtils.setWithinSeconds(cacheKey, serverSecret, CommonConstant.ONE_MONTH);
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
        if (!result) {
            logger.warn("actualCode:{}, params_code:{}, code:{}, result:{}", actualCode, code, actualCodeMD5Hex, result);
        }
        return result;
    }

    private String buildAppConfigCacheKey(int client_id) {
        return "SP.CLIENTID:APPCONFIG_SECRET_" + client_id;
    }
}
