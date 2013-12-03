package com.sogou.upd.passport.session.model;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-11-28
 * Time: 上午10:51
 */
public class BaseApiParams {
    @Min(0)
    protected int client_id; //应用id
    @NotBlank(message = "code不允许为空")
    protected String code; // MD5（str+client_id+ct+server_secret+ct)方法签名
    @Min(0)
    protected long ct; //单位为毫秒

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }

    public long getCt() {
        return ct;
    }

    public void setCt(long ct) {
        this.ct = ct;
    }
}
