package com.sogou.upd.passport.session.sdk.util;

import com.sogou.upd.passport.session.services.SessionService;
import com.sogou.upd.passport.session.util.SessionCommonUtil;
import com.sogou.upd.passport.session.util.SessionServerUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
