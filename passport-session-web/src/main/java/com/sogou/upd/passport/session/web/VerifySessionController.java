package com.sogou.upd.passport.session.web;

import com.alibaba.fastjson.JSONObject;
import com.sogou.upd.passport.session.model.VerifySidParams;
import com.sogou.upd.passport.session.services.SessionService;
import com.sogou.upd.passport.session.util.ControllerHelper;
import com.sogou.upd.passport.session.util.SessionServerUtil;

import org.apache.commons.lang3.StringUtils;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 负责校验sig及检测session的有效期
 * User: ligang201716@sogou-inc.com
 * Date: 13-11-27
 * Time: 下午2:07
 */
@Controller
public class VerifySessionController extends BaseController {

    @Autowired
    private SessionService sessionService;

    private static Logger logger = LoggerFactory.getLogger(VerifySessionController.class);

    @RequestMapping(value = "/verify_sid", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String verifySid(HttpServletRequest request, VerifySidParams verifySidParams) {

        StopWatch stopWatch = new Slf4JStopWatch(WebTimingLogger);
        request.setAttribute("stopWatch", stopWatch);

        String sgid = verifySidParams.getSgid();
        int clientId = verifySidParams.getClient_id();
        String code = verifySidParams.getCode();
        long ct = verifySidParams.getCt();
        boolean isWeb = verifySidParams.isWeb();

        JSONObject result = new JSONObject();
        // 参数校验
        String validateResult = ControllerHelper.validateParams(verifySidParams);
        if (StringUtils.isNotBlank(validateResult)) {
            result.put("status", "10002");
            result.put("statusText", validateResult);
            return handleResult(result, request);
        }

        if (!SessionServerUtil.checkSgid(sgid)) {
            result.put("status", "50002");
            result.put("statusText", "sid自校验错误");
            return handleResult(result, request);
        }

        if (!sessionService.checkCode(sgid, clientId, code, ct)) {
            result.put("status", "10003");
            result.put("statusText", "code签名错误");
            return handleResult(result, request);
        }

        JSONObject userInfo = sessionService.getSession(sgid, isWeb);
        if (userInfo == null) {
            result.put("status", "50001");
            result.put("statusText", "sid不存在或已过期");
            logger.warn("sid miss sgid:" + sgid + " , client_id:" + clientId);
            return handleResult(result, request);
        }

        result.put("status", "0");
        result.put("data", userInfo);

        return handleResult(result, request);
    }
}
