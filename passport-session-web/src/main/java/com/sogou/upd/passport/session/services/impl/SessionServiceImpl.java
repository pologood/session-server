package com.sogou.upd.passport.session.services.impl;

import com.alibaba.fastjson.JSONObject;
import com.sogou.upd.passport.session.services.SessionService;
import com.sogou.upd.passport.session.util.CommonConstant;
import com.sogou.upd.passport.session.util.KvUtil;
import com.sogou.upd.passport.session.util.redis.RedisClientTemplate;
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
            //kv中存在则存入redis中

            if(StringUtils.isBlank(value)){
                value="null";
            }
            redisClientTemplate.set(key,value);
            redisClientTemplate.expire(key,CommonConstant.SESSION_EXPIRSE);
        }

        if(StringUtils.isBlank(value)||"null".equals(value)){
            return null;
        }

        //对有效的且剩余生命不足有效期一半的sgid进行续期
        Long leftTime=redisClientTemplate.ttl(key);
        if(leftTime!=null && leftTime<=0.5*CommonConstant.SESSION_EXPIRSE){
            redisClientTemplate.expire(key,CommonConstant.SESSION_EXPIRSE);
            try {
                kvUtil.set(key,value,CommonConstant.SESSION_EXPIRSE);
            } catch (Exception e) {
                logger.error("set kv fail",e);
            }
        }

        try{
            return JSONObject.parseObject(value);
        }catch (Exception e){
            logger.error("value parse json error, value:"+value);
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
            kvUtil.set(key,userInfo,CommonConstant.SESSION_EXPIRSE);
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
