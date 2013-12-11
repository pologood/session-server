package com.sogou.upd.passport.session.sdk.client;

import com.sogou.upd.passport.session.sdk.service.EhcacheService;
import com.sogou.upd.passport.session.sdk.service.VerifyService;
import com.sogou.upd.passport.session.sdk.service.impl.EhcacheServiceImpl;
import com.sogou.upd.passport.session.sdk.service.impl.VerifyServiceImpl;

/**
 * Created with IntelliJ IDEA.
 * User: hujunfei
 * Date: 13-12-2
 * Time: 上午10:25
 * To change this template use File | Settings | File Templates.
 */
public class SessionServerClient {

/*    private int clientId;
    private String serverSecret;
    private int cacheMaxElements;
    private int cacheExpire;*/

    private VerifyService verifyService;

    /**
     * 初始化SDK（local cache默认最高10W条记录，缓存30分钟）
     * @param clientId passport分配的应用clientid
     * @param serverSecret passport分配的应用服务端秘钥
     */
    public SessionServerClient(int clientId,String serverSecret) {
        verifyService = new VerifyServiceImpl(clientId, serverSecret);
    }


    /**
     * 初始化SDK
     * @param clientId passport分配的应用clientId
     * @param serverSecret passport分配的应用服务端秘钥
     * @param cacheMaxElements 本地cache的最大元素个数（默认为10W）
     * @param cacheExpire 本地cache的有效期（默认为30分钟）
     */
    public SessionServerClient(int clientId,String serverSecret,int cacheMaxElements,int cacheExpire) {
        verifyService = new VerifyServiceImpl(clientId, serverSecret, cacheMaxElements, cacheExpire);
    }

    /**
     * 测试instanceSize
     * @param clientId
     * @param serverSecret
     * @param cacheMaxElements
     * @param cacheExpire
     */
    public SessionServerClient(int clientId,String serverSecret,int cacheMaxElements,int cacheExpire, int instanceSize) {
        verifyService = new VerifyServiceImpl(clientId, serverSecret, cacheMaxElements, cacheExpire, instanceSize);
    }


    /**
     * 弱验证接口（非敏感操作推荐使用）
     * 功能：简单校验sid，获取用户信息，信息可能有一小段时间的延迟（使用本地cache）
     * 适用场景：用户做非核心操作，阅读翻页，用户行为log记录
     * 实现：优先使用本地cache进行校验，本地cache miss再到session server校验
     *
     * @param sgid 要检验的SID
     * @param userIP 用户IP
     * @return 返回结果（json）
     *
     *  成功：{
     *           "data": {
     *           "passport_id":"upd_test@sogou.com"
     *           },
     *           "status": "0",
     *           "statusText": ""
     *           }
     *
     *  失败：{
     *       "data": {},
     *       "status": "10002",
     *       "statusText": "参数错误,请输入必填的参数或参数验证失败"
     *       }
     */
    public String weakVerifySid(String sgid,String userIP) {
        return verifyService.verifySession(sgid, userIP, true);
    }


    /**
     * 强验证接口
     * 功能：强校验SID的接口（不适用本地cache）
     * 适用场景：用户做核心操作时使用，修改个人信息，涉及资金接口
     * 实现：远程调用session server验证，验证结果更新本地cache
     *
     * @param sgid 要检验的SID
     * @param userIP 用户IP
     * @return 返回结果（json）
     *
     *  成功：{
     *           "data": {
     *           "passport_id":"upd_test@sogou.com"
     *           },
     *           "status": "0",
     *           "statusText": ""
     *           }
     *
     *  失败：{
     *       "data": {},
     *       "status": "10002",
     *       "statusText": "参数错误,请输入必填的参数或参数验证失败"
     *       }
     */
    public String coerciveVerifySid(String sgid,String userIP) {
        return verifyService.verifySession(sgid, userIP, false);
    }

    public static void main(String args[]) {
        EhcacheService service = new EhcacheServiceImpl();
        service.set("key1", "value1");
        System.out.println(service.get("key1"));
        service.set("key1", "value2");
        System.out.println(service.get("key1"));
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            //
        }
    }
}
