package com.sogou.upd.passport.session.sdk.service.impl;

import com.sogou.upd.passport.session.sdk.model.RequestModel;
import com.sogou.upd.passport.session.sdk.param.HttpMethodEnum;
import com.sogou.upd.passport.session.sdk.service.EhcacheService;
import com.sogou.upd.passport.session.sdk.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-10
 * Time: 下午7:28
 */
public class ShootingMonitor extends TimerTask {

    private static String REPORT_SHOOTING_URL = "http://session.account.sogou/report_shooting";

    private static Logger logger = LoggerFactory.getLogger(ShootingMonitor.class);

    private EhcacheService ehcacheService;

    private long hitsLastMin;

    private long missesLastMin = 0;

    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ShootingMonitor(EhcacheService ehcacheService) {
        this.ehcacheService = ehcacheService;
        hitsLastMin = 0;
        missesLastMin = 0;
    }

    @Override
    public void run() {
        if (ehcacheService == null) {
            logger.error("session server SDK ehcacheService is null");
        }
        long hits = ehcacheService.getCacheHits();
        long misses = ehcacheService.getCacheMisses();
        long hitsMin = hits - hitsLastMin;
        hitsLastMin=hits;
        long misMin = misses - missesLastMin;
        missesLastMin= misses;
        long countMin = hitsMin + misMin;
        double shooting = 1;
        if (countMin > 0) {
            shooting = hitsMin / (hitsMin + misMin);
        }
        logger.info("[session-server SDK] local cache hits:" + hitsMin + ",misses=" + misMin + ",shooting=" + shooting);

        this.sendShootingLog(hitsMin,misMin,shooting);
    }

    private void sendShootingLog(long hitsMin, long misMin, double shooting) {
        RequestModel requestModel = new RequestModel(REPORT_SHOOTING_URL);
        requestModel.setHttpMethodEnum(HttpMethodEnum.POST);
        InetAddress addr;
        String ip = "";
        String address = "";
        try {
            addr = InetAddress.getLocalHost();
            ip = addr.getHostAddress();//获得本机IP
            address = addr.getHostName();//获得本机名称
        } catch (UnknownHostException e) {
            logger.warn("[session-server SDK] getLocalHost error");
        }
        requestModel.addParam("ip", ip);
        requestModel.addParam("address", address);
        requestModel.addParam("hitsMin", hitsMin);
        requestModel.addParam("misMin", misMin);
        requestModel.addParam("shooting", shooting);
        String time = dateformat.format(new Date());
        requestModel.addParam("time", time);

        String result= HttpClientUtil.executeStr(requestModel);

        logger.info("upload shooting result:"+result);
    }
}
