package com.sogou.upd.passport.session.sdk.util;

import com.sogou.upd.passport.session.sdk.model.SetSessionParams;
import org.junit.Test;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-2
 * Time: 下午3:32
 */
public class CodeUtilTest {

    @Test
    public void testCheckCode() throws Exception {

        String sid=SessionServerUtil.createSessionSid("dasda@sogou-inc.com");

        StringBuilder codeBuilder=new StringBuilder(sid);
        long ct=System.currentTimeMillis();
        codeBuilder.append("1120");
        codeBuilder.append("4xoG%9>2Z67iL5]OdtBq$l#>DfW@TY");
        codeBuilder.append(ct);
        String code= SessionCommonUtil.calculateMD5Hex(codeBuilder.toString());
        System.out.println(codeBuilder.toString());
        System.out.println("sid:"+sid+",ct:"+ct+",code:"+code);
        SetSessionParams setSessionParams=new SetSessionParams();
        setSessionParams.setUserInfo("{\"passport_id\":\"upd_test@sogou.com\"}");
        setSessionParams.setClient_id(1120);
        setSessionParams.setCode(code);
        setSessionParams.setCt(ct);
        setSessionParams.setSid(sid);
        System.out.println(CodeUtil.checkCode(setSessionParams));
    }
}
