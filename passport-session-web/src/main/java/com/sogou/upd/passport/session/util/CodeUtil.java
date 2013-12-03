package com.sogou.upd.passport.session.util;

import com.sogou.upd.passport.session.model.BaseSidParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-2
 * Time: 下午2:36
 */
public class CodeUtil {

    private static Logger logger = LoggerFactory.getLogger(CodeUtil.class);

    public static int PASSPORT_CLIENT_ID=1120;

    //TODO 由于这次时间较为紧急，先将数据写死到代码中，之后将其改为从数据库读取
    private static ConcurrentHashMap<Integer,String> APP_SECRET_MAP=new ConcurrentHashMap<Integer, String>();

    static{
        //bobo
        APP_SECRET_MAP.put(1001,"c3425ddc98da66f51628ee6a59eb08cb784d610c");
        //搜狗游戏
        APP_SECRET_MAP.put(1100,"yRWHIkB$2.9Esk>7mBNIFEcr:8\\[Cv");
        //搜狗通行证
        APP_SECRET_MAP.put(1120,"4xoG%9>2Z67iL5]OdtBq$l#>DfW@TY");
        //应用市场
        APP_SECRET_MAP.put(1110,"FqMV=*S:y^s0$FlwyW>xZ8#A4bQ2Hr");
        //搜狗地图
        APP_SECRET_MAP.put(1024,"Z>DsObdUXTV,?)uYu7+V4T#14J2F");
        //搜狗邮箱
        APP_SECRET_MAP.put(1014,"6n$gFQf<=Az_3MZb#W?4&LCm~)Qhm{");
        //浏览器输入法桌面
        APP_SECRET_MAP.put(1044,"=#dW$h%q)6xZB#m#lu'x]]wP=\\FUO7");
        //浏览器
        APP_SECRET_MAP.put(1065,"tg6Fd%/Ik5l3#6(,UB0%p5[+a&1(Cd");
        //搜狗百科
        APP_SECRET_MAP.put(1121,"JaE]Masjc2#0D0XKYc8%uN3c9P5HV1");
        //搜狗阅读
        APP_SECRET_MAP.put(1115,"RBCqf6a448Wj5a8#KF&POL75*5GBQ5");


    }

    /**
     * 计算code是否正确
     * @param baseSidParams
     * @return
     */
    public static boolean checkCode(BaseSidParams baseSidParams){
        int appid=baseSidParams.getClient_id();
        if(!APP_SECRET_MAP.containsKey(appid)){
           return false;
        }
        String secret=APP_SECRET_MAP.get(appid);
        StringBuilder codeBuilder=new StringBuilder(baseSidParams.getSgid());
        codeBuilder.append(appid);
        codeBuilder.append(secret);
        codeBuilder.append(baseSidParams.getCt());
        String code=SessionCommonUtil.calculateMD5Hex(codeBuilder.toString());
        boolean result=code.equals(baseSidParams.getCode());
        if(logger.isDebugEnabled()){
            logger.debug("codeBuilder:"+codeBuilder.toString()+",code:"+code+",result:"+result);
        }
        if(!result){
            logger.warn("codeBuilder:"+codeBuilder.toString()+",params_code"+baseSidParams.getCode()+",code:"+code+",result:"+result);
        }
        return result;
    }


}
