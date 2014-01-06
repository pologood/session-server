package com.sogou.upd.passport.session.web;

import com.alibaba.fastjson.JSONObject;
import org.perf4j.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-18
 * Time: 下午8:26
 */
public class BaseController  {

    public static final String STOPWATCH= "stopWatch";

    public static final String SLOW_THRESHOLD="slowThreshold";

    public static final Logger WebTimingLogger= LoggerFactory.getLogger("webTimingLogger");

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    private final static int SLOW_TIME = 10;

    String handleResult(JSONObject result,HttpServletRequest request){
        try {
            Object stopWatchObject = request.getAttribute(STOPWATCH);
            if (stopWatchObject != null) {
                StopWatch stopWatch = (StopWatch) stopWatchObject;

                StringBuilder tagBuilder = new StringBuilder(request.getRequestURI());

                String status= result.get("status").toString();

                tagBuilder.append(".");
                tagBuilder.append(status);

                //获取自定义慢请求阀值
                Object slowThreshold = request.getAttribute(SLOW_THRESHOLD);
                int slowTime=SLOW_TIME;
                if(slowThreshold!=null){
                    slowTime=(Integer)slowThreshold;
                }

                //检测是否慢请求
                if (stopWatch.getElapsedTime() >= slowTime) {
                    tagBuilder.append(".slow");
                }

                stopWatch.stop(tagBuilder.toString());
            }
        } catch (Exception e) {
            logger.error("CostTimeInteceptor.afterCompletion error url=" + request.getRequestURL(), e);
        }
        return result.toString();
    }
}
