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
    @NotBlank(message = "user_info不能为空")
    private String user_info;


    public String getUser_info() {
        return user_info;
    }

    public void setUser_info(String user_info) {
        this.user_info = user_info;
    }
}
