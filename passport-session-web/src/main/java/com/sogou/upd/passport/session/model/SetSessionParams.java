package com.sogou.upd.passport.session.model;

import org.hibernate.validator.constraints.NotBlank;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-1
 * Time: 下午10:49
 */
public class SetSessionParams extends BaseSidParams{

    /**
     * 用户基本信息
     */
    @NotBlank(message = "userInfo")
    private String userInfo;


    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }
}
