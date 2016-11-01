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
     * @param sid
     * @param userInfo
     */
    public void setSession(String sid,String userInfo);

    /**
     * 获取session信息
     * @param sid
     * @return
     */
    public JSONObject getSession(String sid);

    /**
     * 删除session信息
     * @param sid
     */
    public void deleteSession(String sid);
    
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
