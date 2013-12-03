package com.sogou.upd.passport.session.sdk.service;

/**
 * Created with IntelliJ IDEA.
 * User: hujunfei
 * Date: 13-11-29
 * Time: 上午11:40
 * To change this template use File | Settings | File Templates.
 */
public interface VerifyService {

    /**
     * 根据sid验证session
     *
     * @param sid
     * @param userIp
     * @param isWeak
     * @return 成功返回passportId，失败返回null
     */
    public String verifySession(String sid, String userIp, boolean isWeak);


    public void setClientId(int clientId);

    public void setServerSecretKey(String serverSecretKey);

}
