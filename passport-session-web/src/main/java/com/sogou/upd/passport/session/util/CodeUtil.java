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
        APP_SECRET_MAP.put(1001,"c3425ddc98da66f51628ee6a59eb08cb784d610c");//bobo
        APP_SECRET_MAP.put(1100,"yRWHIkB$2.9Esk>7mBNIFEcr:8\\[Cv");//搜狗游戏
        APP_SECRET_MAP.put(1120,"4xoG%9>2Z67iL5]OdtBq$l#>DfW@TY");//搜狗通行证
        APP_SECRET_MAP.put(1110,"FqMV=*S:y^s0$FlwyW>xZ8#A4bQ2Hr");//应用市场
        APP_SECRET_MAP.put(1024,"Z>DsObdUXTV,?)uYu7+V4T#14J2F");//搜狗地图
        APP_SECRET_MAP.put(1044,"=#dW$h%q)6xZB#m#lu'x]]wP=\\FUO7");//浏览器输入法桌面
        APP_SECRET_MAP.put(1065,"tg6Fd%/Ik5l3#6(,UB0%p5[+a&1(Cd");//浏览器
        APP_SECRET_MAP.put(1121,"JaE]Masjc2#0D0XKYc8%uN3c9P5HV1");//搜狗百科
        APP_SECRET_MAP.put(1086,"Ds9h4/7a*d?(qcodS&oeGw=<s=Q&#$");//搜狗开放平台
        APP_SECRET_MAP.put(2000,"\\NTA'1]UY!SJ6P8ad|zNI$9AMkO4W&");//笑话
        APP_SECRET_MAP.put(2001,"XFUKkwkoSFJ>$&M=3Z[1w2Fw!_~8qZ");//LOST
        APP_SECRET_MAP.put(2002,"hc,H:O%eq`748f,zXs]}m%7g%kI>H0");//壁纸
        APP_SECRET_MAP.put(1112,"Q*FO8%4SG4@N$RcgX2UsQ)g5BK39TX");//搜狗问答
        APP_SECRET_MAP.put(2003,"VV(x^d5~`i#SnO]D,5'&5UtTBv-lp[");//输入法
        APP_SECRET_MAP.put(1099,"Ge,+)2%LT(oOYoO?i<B^0`E7rQsgUg");//手机助手
        APP_SECRET_MAP.put(1119,"c79I[mL5*I2SWST5IQ2otZsm23Nlew");//站长平台
        APP_SECRET_MAP.put(2004,"NLKo43`^Ek`2'JlV%M=0b]GRL&y4~k");//opsms
        APP_SECRET_MAP.put(2005,"P|Me0f(0~[wD}\"/&2G8)x\\%Dh!53/^");//搜狗指数
        APP_SECRET_MAP.put(1115,"RBCqf6a448Wj5a8#KF&POL75*5GBQ5");//搜狗阅读
        APP_SECRET_MAP.put(2006,"9i1J4(P;TWTw=9)KGs7xA^LMEI+|2v");//搜狗新闻搜索
        APP_SECRET_MAP.put(2007,"udj0D>~Ez`:%Zbj`wFfh8mW`lB[{(]");//随便看看
        APP_SECRET_MAP.put(2008,"O/R*!&`.=K!n/~FMH`))7JbD8:.kT)");//搜狗游戏盒子
        APP_SECRET_MAP.put(2009,"Hpi%#ZT<u@hR.6F)HtfvUKf5ERYR1b");//qq导航
        APP_SECRET_MAP.put(2010,"<6Cxph3Un\'BaWsL7g\'U.VLd|s?tZC)");//qq输入法
        APP_SECRET_MAP.put(2011,"vYaI0Cf=ui$EOyB\\NK_r%et*v~jH(t");//TIMO
        APP_SECRET_MAP.put(2012,"=Rlo\"PjYrM_epd3PTEG{`Ww$mR2@og");//彩票
        APP_SECRET_MAP.put(2013,"ezr8DRjSn%*[mqa>,$^m6_+r~qSwN3");//安全应急响应中心
        APP_SECRET_MAP.put(2014,"^]cG4z\\zkl0}`p|6O2;ivHK~KdC|!u");//搜狗导航
        APP_SECRET_MAP.put(2020,"JaoV><rv/l fJi3i(vgK5m(hK$M%UF");//糖猫
        APP_SECRET_MAP.put(2022,"3JM8?d.0%;fT;s;I$CSs<3HVd2xX%#");//搜狗公交
        APP_SECRET_MAP.put(2023,"myws\\~ZZPm6rRK93z'Nf?I3lo5t2r-");//车载导航
        APP_SECRET_MAP.put(2024,"dUC_*|<97Xv^MsTjA&#*6>lG5k,4Hb");//手机浏览器
        APP_SECRET_MAP.put(2025,"voU-GwrPkd52WL(N@DsH72Wv0PRL0U");//手机搜狗输入法
        APP_SECRET_MAP.put(2026,"*5R9t9`r$%8$prdu{CR>YS%9$^w?z!");//一拍解题
        APP_SECRET_MAP.put(2027,"VBXOU|V!S(\"5;vI'LRrq%ISv-ae^*~");//导航内容平台系统
        APP_SECRET_MAP.put(2028,"s{(/zI4b9`8-8NRyyuir($Ue\"8@'_e");//搜狗尚美
        APP_SECRET_MAP.put(2029,"lt~-q^+#+t\\broJ$AkMZgGcQl.n<6b");//爱对讲
        APP_SECRET_MAP.put(2030,"(|,udq$)ImQ3tp=i3P4[ah~TSkg3ha");//微信头条
        APP_SECRET_MAP.put(2031,"e<T&/f`y~$&?L~(1p{txfKjkI7blnl");//食神APP
        APP_SECRET_MAP.put(2032,"sv'+2,U'0#PPL2DV;%pjGgBur?cerv");//搜狗暑期整合营销
        APP_SECRET_MAP.put(2033,"I0U)0eW5XcL0&Ju9]#0p l((No!%HY");//懒人跑步
        APP_SECRET_MAP.put(2034,"o[%H4)Z,H)\"ZXG&\\#m(!Vy2E<TGT9!");//不能不看
        APP_SECRET_MAP.put(2035,"+iH^LBLz(fnUyZ)1v<IJM&BE\\rzu*b");//搜狗百科IOS客户端
        APP_SECRET_MAP.put(2036,"2up\"];ANX{=7f'}}0coT>}1M1*)TQa");//大料APP
        APP_SECRET_MAP.put(2037,"4Cx9kRg)r)oew`/vh9;t,}FWu[:zux");//搜狗输入法论坛项目
        APP_SECRET_MAP.put(2038,"% rIn-yx#+FD$C|m7cC-bZf)2*>z4!");//搜狗导航IOS客户端
        APP_SECRET_MAP.put(2039,"M4(K?lT+UPtZ^f%~P9\":lP1!)aqL[o");//从前IOS客户端
        APP_SECRET_MAP.put(2015,"{Q;6sxEi[R8gj{g!k%w3'o%&8'xee*");//号码通
        APP_SECRET_MAP.put(2040,"w_6 pR|W]S/~}K^ j+y4X{`y>yb Ce");//好友链测试
        APP_SECRET_MAP.put(2041,"df{P;d=<Cz\"k'fyr2%u0FWDnwZ>D*T");//东风风行皮肤设计大赛
        APP_SECRET_MAP.put(2042,"SY2dXLm+DCfxcz&e(aok7uG3>Ptav&");//fuwuapp
        APP_SECRET_MAP.put(2043,"5Pb&Ze'IO}5N3*,t#(^o38H4k4pd_A");//健康宝典
        APP_SECRET_MAP.put(2044,"a5fKkv4fiv_sHk*0QK*6C8`3WW]hr>");//趣读
        APP_SECRET_MAP.put(2045,"CB7%aZe{K17IhH]!1O<Y0Wq0BtR>k");//小P测试
        APP_SECRET_MAP.put(2046,"GgLqe&72z#|xK1R.bsd,m@&S%2_R^>");//荐文
        APP_SECRET_MAP.put(2047,"<r)h3fj,s%6Z<.x=<mJKm<6IkLhhUm");//荐闻
    }

    /**
     * 计算code是否正确
     * @param baseSidParams
     * @return
     */
    public static boolean checkCode(BaseSidParams baseSidParams){
        int appid=baseSidParams.getClient_id();
        if(!APP_SECRET_MAP.containsKey(appid)){
           logger.warn("checkCode appid not contain "+appid);
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
            logger.warn("codeBuilder:"+codeBuilder.toString()+",params_code:"+baseSidParams.getCode()+",code:"+code+",result:"+result);
        }
        return result;
    }


}
