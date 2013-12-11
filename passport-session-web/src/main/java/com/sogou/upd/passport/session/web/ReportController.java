package com.sogou.upd.passport.session.web;

import com.alibaba.fastjson.JSONObject;
import com.sogou.upd.passport.session.model.VerifySidParams;
import org.perf4j.aop.Profiled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-11
 * Time: 下午3:21
 */
@Controller
public class ReportController {

    private static Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Profiled(el = true, logger = "webTimingLogger", tag = "POST:/report_shooting", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    @RequestMapping(value = "/report_shooting", method = RequestMethod.POST,produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String verifySid(HttpServletRequest request){
        logger.info(JSONObject.toJSON(request.getParameterMap()).toString());
        return "{\"statuc\":\"0\"}";
    }
}
