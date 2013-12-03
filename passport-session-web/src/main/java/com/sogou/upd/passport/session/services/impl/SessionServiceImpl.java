package com.sogou.upd.passport.session.services.impl;

import com.alibaba.fastjson.JSONObject;
import com.sogou.upd.passport.session.services.SessionService;
import com.sogou.upd.passport.session.sdk.util.CommonConstant;
import com.sogou.upd.passport.session.sdk.util.KvUtil;
import com.sogou.upd.passport.session.sdk.util.redis.RedisClientTemplate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private static Logger logger = LoggerFactory.getLogger(SessionServiceImpl.class);


    @Override
    public JSONObject getSession(String sid) {
        //先从redis中获取
        String key= CommonConstant.PREFIX_SESSION+sid;
        String value= redisClientTemplate.get(key);

        //再从kv中获取
        if(StringUtils.isBlank(value)){
            value= kvUtil.get(key);
            if(StringUtils.isNotBlank(value)){
                redisClientTemplate.expire(key,CommonConstant.SESSION_EXPIRSE);
            }
        }

        if(StringUtils.isNotBlank(value)){
           return JSONObject.parseObject(value);
        }

        return null;
    }

    @Override
    public void deleteSession(String sid) {
        String key= CommonConstant.PREFIX_SESSION+sid;
        redisClientTemplate.del(key);
        kvUtil.delete(key);
    }

    @Override
    public void setSession(String sid, String userInfo) {
        String key= CommonConstant.PREFIX_SESSION+sid;
        try {
            kvUtil.set(key,userInfo);
        } catch (Exception e) {
            logger.error("set kv fail",e);
        }

        /**
         * 由于key已经放出去了，所以及时kv设置失败，依然会去设置redis，因为kv只是备份
         */
        redisClientTemplate.set(key,userInfo);
        redisClientTemplate.expire(key,CommonConstant.SESSION_EXPIRSE);
    }
}
