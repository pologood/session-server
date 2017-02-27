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
        String cacheKey = "sessionid_12-23932186";
        Map<String, String> map = newSgidRedisClientTemplate.hgetAll(cacheKey);
        for (Map.Entry<String, String> set : map.entrySet()) {
            System.err.println(set);
        }
    }
}
