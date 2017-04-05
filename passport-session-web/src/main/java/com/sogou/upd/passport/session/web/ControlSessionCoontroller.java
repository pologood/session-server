package com.sogou.upd.passport.session.web;

import com.google.common.collect.Maps;

import com.alibaba.fastjson.JSONObject;
import com.sogou.upd.passport.session.model.DeleteSessionParams;
import com.sogou.upd.passport.session.model.NewSessionParams;
import com.sogou.upd.passport.session.model.SetSessionParams;
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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * 负责设置session相关的处理
 * User: ligang201716@sogou-inc.com
 * Date: 13-11-27
 * Time: 下午2:06
 */
@Controller
public class ControlSessionCoontroller extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(ControlSessionCoontroller.class);

    @Autowired
    private SessionService sessionService;

    @RequestMapping(value = "/set_session", params = {"client_id=1120"}, method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String setSession(HttpServletRequest request, SetSessionParams setSessionParams) {
        StopWatch stopWatch = new Slf4JStopWatch(WebTimingLogger);
        request.setAttribute(STOPWATCH, stopWatch);
        request.setAttribute(SLOW_THRESHOLD, 20);

        String sgid = setSessionParams.getSgid();
        int clientId = setSessionParams.getClient_id();
        String code = setSessionParams.getCode();
        long ct = setSessionParams.getCt();
        boolean isWap = setSessionParams.isWap();

        JSONObject result = new JSONObject();
        // 参数校验
        String validateResult = ControllerHelper.validateParams(setSessionParams);
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

        sessionService.setSession(sgid, setSessionParams.getUser_info(), isWap);

        result.put("status", "0");

        return handleResult(result, request);
    }

    @RequestMapping(value = "/new_session", params = {"client_id=1120"}, method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String newSession(HttpServletRequest request, NewSessionParams newSessionParams) {
        StopWatch stopWatch = new Slf4JStopWatch(WebTimingLogger);
        request.setAttribute(STOPWATCH, stopWatch);
        request.setAttribute(SLOW_THRESHOLD, 20);

        JSONObject result = new JSONObject();
        // 参数校验
        String validateResult = ControllerHelper.validateParams(newSessionParams);
        if (StringUtils.isNotBlank(validateResult)) {
            result.put("status", "10002");
            result.put("statusText", validateResult);
            return handleResult(result, request);
        }

        int clientId = newSessionParams.getClient_id();
        String code = newSessionParams.getCode();
        long ct = newSessionParams.getCt();

        String prefix = newSessionParams.getPrefix();
        String passportId = newSessionParams.getPassportId();
        String weixinOpenId = newSessionParams.getWeixinOpenId();
        boolean isWap = newSessionParams.isWap();

        if (!sessionService.checkCode(passportId, clientId, code, ct)) {
            result.put("status", "10003");
            result.put("statusText", "code签名错误");
            return handleResult(result, request);
        }

        String sgid = sessionService.newSession(prefix, passportId, weixinOpenId, isWap);

        result.put("status", "0");
        Map<String, String> dataMap = Maps.newHashMap();
        dataMap.put("sgid", sgid);
        result.put("data", dataMap);

        return handleResult(result, request);
    }

    @RequestMapping(value = "/del_session", params = {"client_id=1120"}, method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String deleteSession(HttpServletRequest request, DeleteSessionParams deleteSessionParams) {
        StopWatch stopWatch = new Slf4JStopWatch(WebTimingLogger);
        request.setAttribute("stopWatch", stopWatch);

        String sgid = deleteSessionParams.getSgid();
        int clientId = deleteSessionParams.getClient_id();
        String code = deleteSessionParams.getCode();
        long ct = deleteSessionParams.getCt();

        JSONObject result = new JSONObject();
        // 参数校验
        String validateResult = ControllerHelper.validateParams(deleteSessionParams);
        if (StringUtils.isNotBlank(validateResult)) {
            result.put("status", "10002");
            result.put("statusText", validateResult);
            return handleResult(result, request);
        }

        if (!sessionService.checkCode(sgid, clientId, code, ct)) {
            result.put("status", "10003");
            result.put("statusText", "code签名错误");
            return handleResult(result, request);
        }

        sessionService.deleteSession(sgid);

        result.put("status", "0");

        return handleResult(result, request);
    }

}
