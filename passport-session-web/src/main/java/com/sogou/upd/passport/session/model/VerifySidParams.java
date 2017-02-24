package com.sogou.upd.passport.session.model;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-11-28
 * Time: 上午10:57
 */
public class VerifySidParams extends BaseSidParams {

    /**
     * 用户调用IP
     */
    @NotEmpty(message = "userIp不允许为空")
    private String user_ip;

    /**
     * 是否是 web 登陆
     */
    private boolean isWeb;

    public String getUser_ip() {
        return user_ip;
    }

    public void setUser_ip(String user_ip) {
        this.user_ip = user_ip;
    }

    public boolean isWeb() {
        return isWeb;
    }

    public void setWeb(boolean web) {
        isWeb = web;
    }
}
