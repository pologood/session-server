package com.sogou.upd.passport.session.web;

import com.alibaba.fastjson.JSONObject;
import com.sogou.upd.passport.session.model.VerifySidParams;
import com.sogou.upd.passport.session.services.SessionService;
import com.sogou.upd.passport.session.util.CodeUtil;
import com.sogou.upd.passport.session.util.ControllerHelper;
import com.sogou.upd.passport.session.util.SessionServerUtil;
import org.apache.commons.lang3.StringUtils;
import org.perf4j.aop.Profiled;
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
public class VerifySessionController {

    @Autowired
    private SessionService sessionService;

    @Profiled(el = true, logger = "webTimingLogger", tag = "POST:/verify_sid", timeThreshold = 10, normalAndSlowSuffixesEnabled = true)
    @RequestMapping(value = "/verify_sid", method = RequestMethod.POST,produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String verifySid(HttpServletRequest request,VerifySidParams verifySidParams){
        JSONObject result=new JSONObject();
        // 参数校验
        String validateResult = ControllerHelper.validateParams(verifySidParams);
        if(StringUtils.isNotBlank(validateResult)){
            result.put("status","1002");
            result.put("statusText",validateResult);
            return result.toJSONString();
        }

        if(!SessionServerUtil.checkSid(verifySidParams.getSid())){
            result.put("status","50001");
            result.put("statusText","sid不存在或已过期");
            return result.toJSONString();
        }

        if(!CodeUtil.checkCode(verifySidParams)){
            result.put("status","10003");
            result.put("statusText","code签名错误");
            return result.toJSONString();
        }

        JSONObject userInfo= sessionService.getSession(verifySidParams.getSid());
        if(userInfo==null){
            result.put("status","50001");
            result.put("statusText","sid不存在或已过期");
            return result.toJSONString();
        }

        result.put("status","200");
        result.put("data",userInfo);

        return result.toJSONString();
    }
}
