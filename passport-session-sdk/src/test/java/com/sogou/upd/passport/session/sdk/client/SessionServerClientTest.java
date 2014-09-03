package com.sogou.upd.passport.session.sdk.client;

import org.junit.BeforeClass;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 14-3-31
 * Time: 上午11:40
 */
public class SessionServerClientTest {

    static SessionServerClient sessionServerClient;

    @BeforeClass
    public static void SessionServerClientTest(){
        sessionServerClient=new SessionServerClient(2020,"JaoV><rv/l fJi3i(vgK5m(hK$M%UF");
    }


    @org.junit.Test
    public void testWeakVerifySid() throws Exception {
      System.out.println(sessionServerClient.weakVerifySid("AVQF6sO3bjpZ7uaGerWDq7M","192.168.2.1"))  ;
    }

    @org.junit.Test
    public void testCoerciveVerifySid() throws Exception {
       System.out.println(sessionServerClient.coerciveVerifySid("AVQF6sO3bjpZ7uaGerWDq7M","192.168.2.1"))  ;
    }

//    public static void main(String[] srgs){
//        sessionServerClient=new SessionServerClient(1100,"yRWHIkB$2.9Esk>7mBNIFEcr:8\\[Cv");
//        System.out.println(sessionServerClient.weakVerifySid("AVKgJSxwas9zJLANVvPwD8Y","192.168.2.1"))  ;
//    }
}
