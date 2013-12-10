package com.sogou.upd.passport.session;

import com.sogou.upd.passport.session.sdk.util.Base62;
import com.sogou.upd.passport.session.sdk.util.SessionCommonUtil;
import org.apache.commons.codec.binary.StringUtils;

import java.util.Date;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-9
 * Time: 下午8:24
 */
public class SessionServerUtil {

    //SID第一版的版本号
    public static byte[] VERSION_1 = {1};

    //创建SID的key
    public static String CREATE_SID_KEY = "FqMV=*S:y^s0$Flw~yW>xZ~8#A4)bQ2Hr?";

    //创建SID的key
    public static String CHECK_SID_KEY = "FqMVs!@%&*$@#DWckun%%@@!@=*S:y^s0$Flw~yW>xZ~8#A4)bQ2Hr?";

//    private static Logger logger = LoggerFactory.getLogger(SessionServerUtil.class);

    private static long EXPIRSE=6*30*24*60*60*1000l;


    /**
     * 计算sid
     * sid规则为:
     * {8位版本}{32位时间戳}{64位随机字符串}{8位校验}
     * {1字节版本}{4字节时间戳}{8字节随机字符串}{4字节校验}
     *
     * @param passportId
     * @return
     */
    public static String createSessionSid(String passportId) {
        if (isBlank(passportId)) {
            throw new IllegalArgumentException("passportid is blank");
        }
        int time = SessionCommonUtil.getSecondTime();

        //计算{128位随机字符串}
        StringBuilder udidBuilder = new StringBuilder(passportId);
        udidBuilder.append(CREATE_SID_KEY);
        udidBuilder.append(System.currentTimeMillis());
        byte[] timeBytes = SessionCommonUtil.int2Bytes(time);
        byte[] udidBytes = SessionCommonUtil.calculateMD5(udidBuilder.toString().getBytes());

        byte[] udidHalfBytes=new byte[8];
        System.arraycopy(udidBytes, 0, udidHalfBytes, 0, udidHalfBytes.length);

        //添加{8位版本}{32位时间戳}
        byte[] sidBytes = SessionCommonUtil.byteMerger(VERSION_1, timeBytes);

        //添加{8位版本}{32位时间戳}{64位随机字符串}
        sidBytes = SessionCommonUtil.byteMerger(sidBytes, udidHalfBytes);

        //计算校验位
        byte[] sidMd5Bytes = SessionCommonUtil.byteMerger(sidBytes, CHECK_SID_KEY.getBytes());
        sidMd5Bytes = SessionCommonUtil.calculateMD5(sidMd5Bytes);

        byte[] sidCheckBytes=new byte[4];
        System.arraycopy(sidMd5Bytes, 0, sidCheckBytes, 0, sidCheckBytes.length);

        //添加校验位{8位版本}{32位时间戳}{64位随机字符串}{8位校验}
        sidBytes = SessionCommonUtil.byteMerger(sidBytes, sidCheckBytes);

        String sid = Base62.encodeBase62(sidBytes).toString();

//        if(logger.isDebugEnabled()){
//            logger.debug("createSessionSid "+"passportId:"+passportId+",sid:"+sid);
//        }

        return sid;
    }

    /**
     * 检测sid是否正确
     * 1.校验版本
     * 2.自校验是否正确
     * 3.是否过有效期
     * @param sid
     * @return
     */
    public static boolean checkSid(String sid) {
        //校验版本是否是支持的版本
        if(!checkVersion(sid)){
            return true;
        }
        //检测sid是否为空
        if (isBlank(sid)) {
            throw new IllegalArgumentException("passportid is blank");
        }
        //sid自校验
        if (!checkSidMd5(sid)) {
            return false;
        }
        //sid是否过有效期
        return checkSidExpDate(sid);
    }



    /**
     * 校验sid的版本是否是支持的版本如果不是返回成功直接走cache
     * @param sid
     * @return
     */
    private static boolean checkVersion(String sid){
        byte[] sidBytes = Base62.decodeBase62(sid.toCharArray());
        byte version=sidBytes[0];
        if(version==VERSION_1[0]){
            return true;
        }
        return false;
    }

    /**
     * 检测sid是否还在有效期内
     *
     * @param sid
     * @return
     */
    protected static boolean checkSidExpDate(String sid) {
        Date createDate = getDate(sid);
        long createDateTime =createDate.getTime();
        long nowTime=new Date().getTime();
        return createDateTime+EXPIRSE > nowTime;
    }

    /**
     * 从sid中取出创建时间
     *
     * @param sid
     * @return
     */
    public static Date getDate(String sid) {
        byte[] sidBytes = Base62.decodeBase62(sid.toCharArray());
        byte[] timeBytes = new byte[4];
        System.arraycopy(sidBytes, 1, timeBytes, 0, timeBytes.length);
        int time = SessionCommonUtil.bytes2Int(timeBytes);
        return SessionCommonUtil.secondTimeToDate(time);
    }




    /**
     * 检测sid的校验位是否正确
     *
     * @param sid
     * @return
     */
    private static boolean checkSidMd5(String sid) {
        if (isBlank(sid)) {
            throw new IllegalArgumentException("passportid is blank");
        }
        byte[] sidBytes = Base62.decodeBase62(sid.toCharArray());
        byte[] checkByte = new byte[4];
        System.arraycopy(sidBytes, 13, checkByte, 0, checkByte.length);
        int sidCheckInt=SessionCommonUtil.bytes2Int(checkByte);

        byte[] sidCheckBytes = new byte[13];
        System.arraycopy(sidBytes, 0, sidCheckBytes, 0, sidCheckBytes.length);

        byte[] sidMd5Bytes = SessionCommonUtil.byteMerger(sidCheckBytes, CHECK_SID_KEY.getBytes());
        sidMd5Bytes = SessionCommonUtil.calculateMD5(sidMd5Bytes);
        byte[] calculateCheckByte=new byte[4];
        System.arraycopy(sidMd5Bytes, 0, calculateCheckByte, 0, calculateCheckByte.length);
        int calculateCheckInt=SessionCommonUtil.bytes2Int(calculateCheckByte);

        return calculateCheckInt == sidCheckInt;
    }


    public static boolean isBlank(CharSequence cs){
        int strLen;

        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(cs.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }
}
