package com.sogou.upd.passport.session;

import com.sogou.upd.passport.session.sdk.client.SessionServerClient;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 14-6-20
 * Time: 下午7:52
 */
public class VerifyTest {


    public static void main(String[] args) throws InterruptedException {

        List<SessionServerClient> sessionServerClients=new ArrayList<SessionServerClient>();

        for(int i=0;i<100;i++){
            SessionServerClient sessionServerClient=new SessionServerClient(2011,"vYaI0Cf=ui$EOyB\\NK_r%et*v~jH(t");
            sessionServerClient.coerciveVerifySid("SVKgJSxwas9zJLANVvPwD8Y","192.168.2.1");
            sessionServerClients.add(sessionServerClient);
            Thread.sleep(6000l);
        }
        Thread.sleep(6000000l);

    }
}
