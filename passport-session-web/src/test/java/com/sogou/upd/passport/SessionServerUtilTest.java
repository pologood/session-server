package com.sogou.upd.passport;

import com.sogou.upd.passport.session.util.SessionServerUtil;
import org.junit.Test;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-25
 * Time: 下午4:01
 */
public class SessionServerUtilTest {

    @Test
    public void testCode(){
        String code="AVOWj3ARP3PFtf8VLqy5Twg";
        System.out.println(SessionServerUtil.checkSgid(code));
    }
}
