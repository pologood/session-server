package com.sogou.upd.passport;

import com.sogou.upd.passport.session.util.redis.RedisClientTemplate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: shipengzhi
 * Date: 13-4-23
 * Time: 上午1:19
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-config-test.xml")
public class RedisTest {
    @Autowired
    @Qualifier("newSgidRedisClientTemplate")
    private RedisClientTemplate newSgidRedisClientTemplate;

    @Test
    public void testNewSgidRedisUtils() {
//        String cacheKey = "sessionid_18-25469668";
        // coderqing AVjkU0DV0icD16nOAgRDxv68
        String cacheKey = "sessionid_12-23932186";
        // qq
//        String cacheKey = "sessionid_21-22335201";
        // 手机
//        String cacheKey = "sessionid_30-26997935";
        Map<String, String> map = newSgidRedisClientTemplate.hgetAll(cacheKey);
        for (Map.Entry<String, String> set : map.entrySet()) {
            System.err.println(set);
        }
    }

    @Test
    public void testNewSgidTtl() {
        String cacheKey = "sessionid_12-23932186";
        long ttl = newSgidRedisClientTemplate.ttl(cacheKey);
        System.err.println(ttl);
    }

    @Test
    public void testChangeNewSgidExpire() {
        String cacheKey = "sessionid_12-23932186";
        String sgid = "AViavkvCVXR4lKD0mUfSl7FM";
        long expire = System.currentTimeMillis() / 1000 + 10000;
        String userInfo = "{\"expire\":" + expire + ",\"isWap\":" + true + ",\"passport_id\":\"coderqing@sogou.com\"}";
//        String userInfo = "{\"expire\":1487902730,\"passport_id\":\"coderqing@sogou.com\"}";
        newSgidRedisClientTemplate.hset(cacheKey, sgid, userInfo);
    }

    @Test
    public void testDelNewSgidExpire() {
        String cacheKey = "sessionid_12-23932186";
        newSgidRedisClientTemplate.del(cacheKey);
    }
}
