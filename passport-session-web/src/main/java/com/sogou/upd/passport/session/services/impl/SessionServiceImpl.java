package com.sogou.upd.passport.session.services.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.alibaba.fastjson.JSONObject;
import com.sogou.upd.passport.session.dao.SessionDao;
import com.sogou.upd.passport.session.model.BaseSidParams;
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
    public JSONObject getSession(String sid) {
        //先从redis中获取
        String key = CommonConstant.PREFIX_SESSION + sid;
        String value = redisClientTemplate.get(key);
        
        //再从kv中获取
        if (StringUtils.isBlank(value)) {
            value = kvUtil.get(key);
            //kv中存在则存入redis中
            
            if (StringUtils.isBlank(value)) {
                value = "null";
            }
            redisClientTemplate.set(key, value);
            redisClientTemplate.expire(key, CommonConstant.SESSION_EXPIRSE);
        }
        
        if (StringUtils.isBlank(value) || "null".equals(value)) {
            return null;
        }
        
        //对有效的且剩余生命不足有效期一半的sgid进行续期
        Long leftTime = redisClientTemplate.ttl(key);
        if (leftTime != null && leftTime <= 0.5 * CommonConstant.SESSION_EXPIRSE) {
            redisClientTemplate.expire(key, CommonConstant.SESSION_EXPIRSE);
            try {
                kvUtil.set(key, value, CommonConstant.SESSION_EXPIRSE);
            } catch (Exception e) {
                logger.error("set kv fail", e);
            }
        }
        
        try {
            return JSONObject.parseObject(value);
        } catch (Exception e) {
            logger.error("value parse json error, value:" + value);
        }
        
        return null;
    }
    
    @Override
    public void deleteSession(String sid) {
        String key = CommonConstant.PREFIX_SESSION + sid;
        redisClientTemplate.del(key);
        kvUtil.delete(key);
    }
    
    @Override
    public void setSession(String sid, String userInfo) {
        String key = CommonConstant.PREFIX_SESSION + sid;
        try {
            kvUtil.set(key, userInfo, CommonConstant.SESSION_EXPIRSE);
        } catch (Exception e) {
            logger.error("set kv fail", e);
        }
        
        /**
         * 由于key已经放出去了，所以及时kv设置失败，依然会去设置redis，因为kv只是备份
         */
        redisClientTemplate.set(key, userInfo);
        redisClientTemplate.expire(key, CommonConstant.SESSION_EXPIRSE);
    }
    
    private String loadAppServerSecret(int clientId) {
        String serverSecret = null;
        if (appLocalCache != null) {
            try {
                serverSecret = appLocalCache.get(clientId);
            } catch (Exception e) {
                logger.warn("[App] queryAppConfigByClientId fail,clientId:" + clientId, e);
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
        }
        
        return serverSecret;
    }
    
    @Override
    public boolean checkCode(String sgid, int clientId, String code, long ct) {
        String serverSecret = loadAppServerSecret(clientId);
        
        StringBuilder codeBuilder = new StringBuilder(sgid);
        codeBuilder.append(clientId);
        codeBuilder.append(serverSecret);
        codeBuilder.append(ct);
        String actualCode = SessionCommonUtil.calculateMD5Hex(codeBuilder.toString());
        boolean result = actualCode.equals(code);
        if (logger.isDebugEnabled()) {
            logger.debug("codeBuilder:" + codeBuilder.toString() + ",code:" + actualCode + ",result:" + result);
        }
        if (!result) {
            logger.warn(
                "codeBuilder:" + codeBuilder.toString() + ",params_code:" + code + ",code:" + actualCode + ",result:"
                + result);
        }
        return result;
    }
    
    private String buildAppConfigCacheKey(int client_id) {
        return "SP.CLIENTID:APPCONFIG_" + client_id;
    }
}
