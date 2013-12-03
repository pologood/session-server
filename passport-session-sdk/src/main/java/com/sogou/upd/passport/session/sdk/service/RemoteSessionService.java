package com.sogou.upd.passport.session.sdk.service;

/**
 * Created with IntelliJ IDEA.
 * User: hujunfei
 * Date: 13-11-29
 * Time: 下午3:19
 * To change this template use File | Settings | File Templates.
 */
public interface RemoteSessionService {

    public String getSession(String sid, String userIp, int clientId, long stamp, String code);
}
