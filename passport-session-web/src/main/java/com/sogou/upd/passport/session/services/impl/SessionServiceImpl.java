package com.sogou.upd.passport.session.services.impl;

import com.alibaba.fastjson.JSONObject;
import com.sogou.upd.passport.session.services.SessionService;
import com.sogou.upd.passport.session.util.CommonConstant;
import com.sogou.upd.passport.session.util.redis.RedisClientTemplate;
import org.apache.commons.lang3.StringUtils;
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


    @Override
    public JSONObject getSession(String sid) {
        String key= CommonConstant.PREFIX_SESSION+sid;

        String value= redisClientTemplate.get(key);

        if(StringUtils.isNotBlank(value)){
             return JSONObject.parseObject(value);
        }
        //TODO 这里需要再从KV取一次
        return null;
    }

    @Override
    public void deleteSession(String sid) {
        String key= CommonConstant.PREFIX_SESSION+sid;
        redisClientTemplate.del(key);
    }

    @Override
    public void setSession(String sid, String userInfo) {
        String key= CommonConstant.PREFIX_SESSION+sid;
        redisClientTemplate.set(key,userInfo);
        redisClientTemplate.expire(key,CommonConstant.SESSION_EXPIRSE);
        //TODO 这里需要设置一次kv
    }
}
