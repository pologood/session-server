package com.sogou.upd.passport;

import com.sogou.upd.passport.session.util.SessionServerUtil;
import sun.net.www.http.HttpClient;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-9
 * Time: 下午5:16
 */
public class ProfileThread implements Runnable {
    public long count=0;


    @Override
    public void run() {

    }


    private void setSession(){
        long threadId= Thread.currentThread().getId();
        count++;
        String passportId=count+threadId+"@sogou_test.com";
        String sgid= SessionServerUtil.createSessionSid(passportId);
//        HttpClientUtil.
    }

}
