package com.sogou.upd.passport.session.util;

/**
 * Sogou passport Session-server java SDK
 * User: ligang201716@sogou-inc.com
 * Date: 13-11-29
 * Time: 下午8:50
 */
public interface SessionServerClient {

    /**
     * 初始化SDK（local cache默认最高10W条记录，缓存30分钟）
     * @param clientId passport分配的应用clientid
     * @param serverSecret passport分配的应用服务端秘钥
     */
    public void SessionServerClient(int clientId,String serverSecret);


    /**
     * 初始化SDK
     * @param clientId passport分配的应用clientId
     * @param serverSecret passport分配的应用服务端秘钥
     * @param cacheMaxElement 本地cache的最大元素个数（默认为10W）
     * @param cacheExpirse 本地cache的有效期（默认为30分钟）
     */
    public void SessionServerClient(int clientId,String serverSecret,int cacheMaxElement,int cacheExpirse);

    /**
     * 弱验证接口（非敏感操作推荐使用）
     * 功能：简单校验sid，获取用户信息，信息可能有一小段时间的延迟（使用本地cache）
     * 适用场景：用户做非核心操作，阅读翻页，用户行为log记录
     * 实现：优先使用本地cache进行校验，本地cache miss再到session server校验
     *
     * @param sid 要检验的SID
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
    public String weakVerifySid(String sid,String userIP);


    /**
     * 强验证接口
     * 功能：强校验SID的接口（不适用本地cache）
     * 适用场景：用户做核心操作时使用，修改个人信息，涉及资金接口
     * 实现：远程调用session server验证，验证结果更新本地cache
     *
     * @param sid 要检验的SID
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
    public String coerciveVerifySid(String sid,String userIP);


}
