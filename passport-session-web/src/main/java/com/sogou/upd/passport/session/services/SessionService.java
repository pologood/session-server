package com.sogou.upd.passport.session.services;

import com.alibaba.fastjson.JSONObject;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-11-29
 * Time: 下午5:00
 */
public interface SessionService {

    /**
     * 设置session信息
     * @param sgid
     * @param userInfo
     */
    public void setSession(String sgid, String userInfo, boolean isWap);

    /**
     * 生成新 session
     * @param prefix 前缀，【账号分表索引】-【账号自增 id】
     * @param passportId 账号
     * @param userInfo 用户信息
     * @param isWap 是否是移动端
     */
    public void newSession(String prefix, String passportId, String userInfo, boolean isWap);

    /**
     * 获取session信息
     * @param sgid
     * @return
     */
    public JSONObject getSession(String sgid);

    /**
     * 删除session信息
     * @param sgid
     */
    public void deleteSession(String sgid);

    /**
     * 读取 app 的 server secret
     * @param clientId
     * @return
     */
    public String queryAppConfigByClientId(int clientId);

    /**
     * 检查 code
     * @param sgid
     * @param clientId
     * @param code
     * @param ct
     * @return
     */
    public boolean checkCode(String sgid, int clientId, String code, long ct);
}
