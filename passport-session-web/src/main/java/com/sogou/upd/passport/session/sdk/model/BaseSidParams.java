package com.sogou.upd.passport.session.sdk.model;

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
    @NotBlank(message = "sgid不允许为空")
    private String sgid;

    public String getSgid() {
        return sgid;
    }

    public void setSgid(String sgid) {
        this.sgid = sgid;
    }
}
