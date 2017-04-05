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
     * 用户基本信息
     */
    @NotBlank(message = "user_info不能为空")
    private String user_info;

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
    public String getUser_info() {
        return user_info;
    }

    public void setUser_info(String user_info) {
        this.user_info = user_info;
    }

    public boolean isWap() {
        return wap;
    }

    public void setWap(boolean wap) {
        this.wap = wap;
    }
}
