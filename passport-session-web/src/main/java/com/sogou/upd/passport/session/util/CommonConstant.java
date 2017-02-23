package com.sogou.upd.passport.session.util;

/**
 * User: ligang201716@sogou-inc.com
 * Date: 13-11-28
 * Time: 上午11:02
 */
public class CommonConstant {

    public static final String SEPARATOR_1 = "|";

    public static final String PREFIX_SESSION = "sessionid_";

    /** session 过期时间，秒值，六个月 */
    public static int SESSION_EXPIRSE = 6 * 30 * 24 * 60 * 60;
    /** session 过期时间，毫秒值，六个月 */
    public static long SESSION_EXPIRSE_MILLIS = 6 * 30 * 24 * 60 * 60 * 1000L;

    /** session 过期时间一半，秒值， 三个月 */
    public static int SESSION_EXPIRSE_HALF = 3 * 30 * 24 * 60 * 60;

    /**
     * redis key -
     */
    public static final String REDIS_KEY_EXPIRE = "expire";
}
