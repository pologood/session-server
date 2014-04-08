package com.sogou.upd.passport.session.sdk.service.impl;

import com.sogou.upd.passport.session.sdk.model.RequestModel;
import com.sogou.upd.passport.session.sdk.param.HttpMethodEnum;
import com.sogou.upd.passport.session.sdk.service.RemoteSessionService;
import com.sogou.upd.passport.session.sdk.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: hujunfei
 * Date: 13-11-29
 * Time: 下午3:21
 * To change this template use File | Settings | File Templates.
 */
public class RemoteSessionServiceImpl implements RemoteSessionService {
    private static Logger logger = LoggerFactory.getLogger(RemoteSessionServiceImpl.class);

    private static String URL = "http://session.account.sogou.com.z.sogou-op.org/verify_sid";
//    private static String URL = "http://10.11.202.168:8090/verify_sid";

//    private HttpClientUtil httpClientUtil;

    @Override
    public String getSession(String sid, String userIp, int clientId, long stamp, String code) {
        RequestModel requestModel = new RequestModel(URL);
        requestModel.addParam("sgid", sid);
        requestModel.addParam("user_ip", userIp);
        requestModel.addParam("client_id", clientId);
        requestModel.addParam("ct", stamp);
        requestModel.addParam("code", code);

        requestModel.setHttpMethodEnum(HttpMethodEnum.POST);
        try {
            return HttpClientUtil.executeStr(requestModel);
        } catch (RuntimeException re) {
            logger.error("http error" ,re);
            return null;
        }
    }


    private String getRestfulUrl(String sid) {
        return URL + sid;
    }
}
