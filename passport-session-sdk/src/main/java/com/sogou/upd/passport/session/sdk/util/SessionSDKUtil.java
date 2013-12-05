package com.sogou.upd.passport.session.sdk.util;


import java.util.Date;

/**
 * session server 的SDK 校验类
 * User: ligang201716@sogou-inc.com
 * Date: 13-12-2
 * Time: 上午10:45
 */
public class SessionSDKUtil {

    private static final byte VERSION=1;

    //创建SID的key
    private static String CHECK_SID_KEY = "FqMVs!@%&*$@#DWckun%%@@!@=*S:y^s0$Flw~yW>xZ~8#A4)bQ2Hr?";

    private static long EXPIRSE=6*30*24*60*60*1000l;


    /**
     * 检测sid是否正确
     * 1.自校验是否正确
     * 2.是否过有效期
     * @param sid
     * @return
     */
    public static boolean checkSid(String sid) {
        //校验版本是否是支持的版本
        if(!checkVersion(sid)){
            return true;
        }
        //检测sid是否为空
        if (StringUtil.isBlank(sid)||sid.length()!=23) {
            throw new IllegalArgumentException("passportid is blank or length!=23");
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
        if(version==VERSION){
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
    private static boolean checkSidExpDate(String sid) {
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
    private static Date getDate(String sid) {
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
        if (StringUtil.isBlank(sid)) {
            throw new IllegalArgumentException("passportid is blank");
        }
        byte[] sidBytes = Base62.decodeBase62(sid.toCharArray());
        byte[] checkByte = new byte[4];
        System.arraycopy(sidBytes, 13, checkByte, 0, checkByte.length);
        int sidCheckInt= SessionCommonUtil.bytes2Int(checkByte);

        byte[] sidCheckBytes = new byte[13];
        System.arraycopy(sidBytes, 0, sidCheckBytes, 0, sidCheckBytes.length);

        byte[] sidMd5Bytes = SessionCommonUtil.byteMerger(sidCheckBytes, CHECK_SID_KEY.getBytes());
        sidMd5Bytes = SessionCommonUtil.calculateMD5(sidMd5Bytes);
        byte[] calculateCheckByte=new byte[4];
        System.arraycopy(sidMd5Bytes, 0, calculateCheckByte, 0, calculateCheckByte.length);
        int calculateCheckInt= SessionCommonUtil.bytes2Int(calculateCheckByte);

        return calculateCheckInt == sidCheckInt;
    }

}
