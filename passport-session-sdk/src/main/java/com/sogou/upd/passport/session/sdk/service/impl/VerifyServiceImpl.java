package com.sogou.upd.passport.session.sdk.service.impl;

import com.sogou.upd.passport.session.sdk.service.EhcacheService;
import com.sogou.upd.passport.session.sdk.service.RemoteSessionService;
import com.sogou.upd.passport.session.sdk.service.VerifyService;
import com.sogou.upd.passport.session.sdk.util.CoderUtil;
import com.sogou.upd.passport.session.sdk.util.SessionSDKUtil;
import com.sogou.upd.passport.session.sdk.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: hujunfei
 * Date: 13-11-29
 * Time: 下午12:30
 * To change this template use File | Settings | File Templates.
 */
public class VerifyServiceImpl implements VerifyService {
    private static Logger logger = LoggerFactory.getLogger(VerifyServiceImpl.class);

    private static String ERR_MSG_SYSTEM = "{\"data\": {},\"status\": \"10001\",\"statusText\": \"系统级错误\"}";
    private static String ERR_MSG_PARAMS = "{\"data\": {},\"status\": \"10002\",\"statusText\": \"参数错误,请输入必填的参数或参数验证失败\"}";
    private static String ERR_MSG_SIG = "{\"data\": {},\"status\": \"50001\",\"statusText\": \"sid错误或已过期\"}";

    EhcacheService ehcacheService;
    private RemoteSessionService remoteSessionService;

    private int clientId;
    private String serverSecretKey;

    public VerifyServiceImpl() {
        ehcacheService = new EhcacheServiceImpl();
        remoteSessionService = new RemoteSessionServiceImpl();
    }

    public VerifyServiceImpl(int clientId, String serverSecret) {
        ehcacheService = new EhcacheServiceImpl();
        remoteSessionService = new RemoteSessionServiceImpl();
        setClientId(clientId);
        setServerSecretKey(serverSecret);
    }

    public VerifyServiceImpl(int clientId, String serverSecret, int cacheMaxElements, int cacheExpire) {
        ehcacheService = new EhcacheServiceImpl();
        ehcacheService.setMaxElements(cacheMaxElements);
        ehcacheService.setCacheExpire(cacheExpire);

        remoteSessionService = new RemoteSessionServiceImpl();
        setClientId(clientId);
        setServerSecretKey(serverSecret);
    }

    public VerifyServiceImpl(int clientId, String serverSecret, int cacheMaxElements, int cacheExpire, int instanceSize) {
        ehcacheService = new EhcacheServiceImpl();
        ehcacheService.setMaxElements(cacheMaxElements);
        ehcacheService.setCacheExpire(cacheExpire);
        ehcacheService.setCacheInstanceSize(instanceSize);

        remoteSessionService = new RemoteSessionServiceImpl();
        setClientId(clientId);
        setServerSecretKey(serverSecret);
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void setServerSecretKey(String serverSecretKey) {
        this.serverSecretKey = serverSecretKey;
    }

/*    public void setEhcacheService(EhcacheService ehcacheService) {
        this.ehcacheService = ehcacheService;
    }

    public void setRemoteSessionService(RemoteSessionService remoteSessionService) {
        this.remoteSessionService = remoteSessionService;
    }*/

    /**
     * 根据sid验证session
     *
     * @param sid
     * @param userIp
     * @param isWeak
     * @return 成功返回passportId，失败返回null
     */
    @Override
    public String verifySession(String sid, String userIp, boolean isWeak) {
        try {
            if (!SessionSDKUtil.checkSid(sid)) {
                return ERR_MSG_SIG;
            }

            String value = null;
            if (isWeak) {
                // 弱查询
                value = ehcacheService.get(sid);
                if (StringUtil.isEmpty(value)) {
                    value = verifyRemoteSession(sid, userIp);
                }
            } else {
                // 强查询，从http server查询
                value = verifyRemoteSession(sid, userIp);
            }
            return value;
        } catch (IllegalArgumentException iae) {
            logger.error("param erro(参数错误或不符合格式要求): " + iae.getMessage());
            return ERR_MSG_PARAMS;
        }
    }

    private String verifyRemoteSession(String sid, String userIp) {
        long stamp = System.currentTimeMillis();
        String code = CoderUtil.generatorCode(sid, clientId, serverSecretKey, stamp);

        if (StringUtil.isBlank(code)) {
            return ERR_MSG_SYSTEM;
        }

        String value = remoteSessionService.getSession(sid, userIp, clientId, stamp, code);

        if (StringUtil.isEmpty(value)) {
            return ERR_MSG_SYSTEM;
        }
        ehcacheService.set(sid, value);
        return value;
    }

}
