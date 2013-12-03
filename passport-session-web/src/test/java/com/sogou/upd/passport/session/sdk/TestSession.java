package com.sogou.upd.passport.session.sdk;

import com.sogou.upd.passport.BaseTest;
import com.sogou.upd.passport.session.sdk.client.SessionServerClient;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created with IntelliJ IDEA.
 * User: hujunfei
 * Date: 13-12-3
 * Time: 上午10:19
 * To change this template use File | Settings | File Templates.
 */
public class TestSession extends BaseTest {
    @Autowired
    private SessionServerClient sessionServerClient;

    private static String SID = "AVKcIZJsBHcpbaA-xstFVMo";
    private static String SID1 = "AVKcIZJsBHcpbaA-xstFVMp";
    private static String IP = "127.0.0.1";

    @Test
    public void testSession() {
        while (true) {
            String res1 = sessionServerClient.weakVerifySid(SID, IP);
            String res2 = sessionServerClient.weakVerifySid(SID1, IP);
            String res3 = sessionServerClient.coerciveVerifySid(SID, IP);
            String res4 = sessionServerClient.coerciveVerifySid(SID1, IP);

            /*System.out.println(sessionServerClient.weakVerifySid(SID, IP));
            System.out.println(sessionServerClient.weakVerifySid(SID1, IP));

            System.out.println(sessionServerClient.coerciveVerifySid(SID, IP));
            System.out.println(sessionServerClient.coerciveVerifySid(SID1, IP));
            */
            System.out.println(res1+"\n"+res2+"\n"+res3+"\n"+res4);
            System.out.flush();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                break;
            }
        }

    }

}
