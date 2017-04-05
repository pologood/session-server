package com.sogou.upd.passport.session.model;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 请求新 session 参数
 */
public class NewSessionParams extends BaseApiParams {

    @NotBlank(message = "prefix不能为空")
    private String prefix;

    @NotBlank(message = "passportId不能为空")
    private String passportId;

    /**
     * 微信 openId
     */
    private String weixinOpenId;

    /**
     * 是否是 wap 登陆
     */
    private boolean wap;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPassportId() {
        return passportId;
    }

    public void setPassportId(String passportId) {
        this.passportId = passportId;
    }

    public boolean isWap() {
        return wap;
    }

    public void setWap(boolean wap) {
        this.wap = wap;
    }

    public String getWeixinOpenId() {
        return weixinOpenId;
    }

    public void setWeixinOpenId(String weixinOpenId) {
        this.weixinOpenId = weixinOpenId;
    }

}
