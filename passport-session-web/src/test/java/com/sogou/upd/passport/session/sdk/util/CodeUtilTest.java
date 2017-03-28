package com.sogou.upd.passport.session.sdk.util;

import com.alibaba.fastjson.JSONObject;
import com.sogou.upd.passport.session.services.SessionService;
import com.sogou.upd.passport.session.util.CommonConstant;
import com.sogou.upd.passport.session.util.SessionCommonUtil;
import com.sogou.upd.passport.session.util.SessionServerUtil;

import com.sogou.upd.passport.session.util.redis.RedisClientTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-2
 * Time: 下午3:32
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations ={"classpath:spring-config-test.xml"})
public class CodeUtilTest {
    
    @Autowired
    private SessionService sessionService;

    @Autowired
    @Qualifier("newSgidRedisClientTemplate")
    private RedisClientTemplate newSgidRedisClientTemplate;

    @Test
    public void testSetSession() throws Exception {
        // test set session for old version
        JSONObject userInfo = new JSONObject();
        userInfo.put(CommonConstant.REDIS_PASSPORTID, "codetest1@sogou.com");
        sessionService.setSession("AViauYScqsCxJNumo2V9svjg", userInfo.toJSONString(), true);
    }

    @Test
    public void testNewSession() throws Exception {
        // clear dirty data
        clearDirtyData();

        // set the new session for new version
        JSONObject userinfo = new JSONObject();
        userinfo.put(CommonConstant.REDIS_PASSPORTID, "codetest1@sogou.com");

        // set the session by new version
        sessionService.setSession("12-23932186-AViauYScqsCxJNumo2V9svjg", userinfo.toJSONString(), true);
        sessionService.setSession("12-23932186-AViauYScqsCxJNumo2V9svjg2", userinfo.toJSONString(), false);
        JSONObject resultObj = sessionService.getSession("12-23932186-AViauYScqsCxJNumo2V9svjg");
        assert (resultObj != null);
        System.out.println(resultObj.toJSONString());

        Map<String, String> valueMap = newSgidRedisClientTemplate.hgetAll(CommonConstant.PREFIX_SESSION + "12-23932186");
        printMapKeyAndValue(valueMap);
    }

    @Test
    public void testGetNewSession() throws Exception {
        // clear the dirty data
        clearDirtyData();
        Map<String, String> valueMap = new HashMap<String, String>();

        Map<String, String> values = new HashMap<String, String>();
        JSONObject userinfo = new JSONObject();
        userinfo.put(CommonConstant.REDIS_PASSPORTID, "codetest1@sogou.com");
        userinfo.put(CommonConstant.REDIS_SGID_ISWAP, "true");
        userinfo.put(CommonConstant.REDIS_SGID_EXPIRE, 1491806305);
        values.put("AViauYScqsCxJNumo2V9svjg", userinfo.toJSONString());
        values.put("AViauYScqsCxJNumo2V9svjg2", userinfo.toJSONString());
        userinfo.put(CommonConstant.REDIS_SGID_ISWAP, "false");
        values.put("AViauYScqsCxJNumo2V9svjg3", userinfo.toJSONString());

        newSgidRedisClientTemplate.hmset(CommonConstant.PREFIX_SESSION + "12-23932186", values);

        valueMap = newSgidRedisClientTemplate.hgetAll(CommonConstant.PREFIX_SESSION + "12-23932186");
        assert (valueMap.size() != 0); // assert the data has been set success
        System.out.println("The map before code get session");
        printMapKeyAndValue(valueMap);

        // call the session get method
        JSONObject result = sessionService.getSession("12-23932186-AViauYScqsCxJNumo2V9svjg");
        assert (result != null);
        System.out.println(result.toJSONString());

        // verify the format has been reset
        valueMap = newSgidRedisClientTemplate.hgetAll(CommonConstant.PREFIX_SESSION + "12-23932186");
        assert (valueMap.size() != 0); // assert the data has been set success
        System.out.println("The map after code get session");
        printMapKeyAndValue(valueMap);

        // call the session get method again
        result = sessionService.getSession("12-23932186-AViauYScqsCxJNumo2V9svjg");
        assert (result != null);
        System.out.println(result.toJSONString());
    }

    private void printMapKeyAndValue(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println("Key -> Values:" + entry.getKey() + " -> " + entry.getValue());
        }
    }

    private void clearDirtyData() {
        // clear the data
        newSgidRedisClientTemplate.del(CommonConstant.PREFIX_SESSION + "12-23932186");
        Map<String, String> valueMap = newSgidRedisClientTemplate.hgetAll(CommonConstant.PREFIX_SESSION + "12-23932186");
        assert (valueMap.size() == 0); // assert the data has been cleaned
    }
    
    @Test
    public void testCheckCode() throws Exception {

        String sid= SessionServerUtil.createSessionSid("upd_test@sogou.com");
        int clientId = 1120;
        String serverSecret = "4xoG%9>2Z67iL5]OdtBq$l#>DfW@TY";
        long ct=System.currentTimeMillis();
        
        
        StringBuilder codeBuilder=new StringBuilder();
        
        codeBuilder.append(sid);
        codeBuilder.append(clientId);
        codeBuilder.append(serverSecret);
        codeBuilder.append(ct);
        String code= SessionCommonUtil.calculateMD5Hex(codeBuilder.toString());
        System.out.println(codeBuilder.toString());
        System.out.println("sid:"+sid+",ct:"+ct+",code:"+code);
        System.out.println(sessionService.checkCode(sid, clientId, code, ct));
    }
}
