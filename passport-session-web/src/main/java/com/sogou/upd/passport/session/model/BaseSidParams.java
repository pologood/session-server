package com.sogou.upd.passport.session.model;

import org.hibernate.validator.constraints.NotBlank;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-2
 * Time: 下午3:04
 */
public class BaseSidParams extends BaseApiParams {
    /**
     * session id
     */
    @NotBlank(message = "uid不允许为空")
    private String sid;

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
}
