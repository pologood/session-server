package com.sogou.upd.passport.session.sdk.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: hujunfei
 * Date: 13-11-29
 * Time: 上午11:52
 * To change this template use File | Settings | File Templates.
 */
public class StringUtil {

    static final Logger logger = LoggerFactory.getLogger(StringUtil.class);

    /**
     * 空字符串。
     */
    public static final String EMPTY_STRING = "";

    /**
     * 检查字符串是否为数字
     */
    public static boolean checkIsDigit(String str) {
        return str.matches("[0-9]*");
    }

    /*
     *校验是否是@sohu.com
     */
    public static boolean isSohuUserName(String username) {
        if (username.endsWith("@sohu.com")) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0 || str.equalsIgnoreCase("null");
    }

    /**
     * <p>Checks if a CharSequence is whitespace, empty ("") or null.</p>
     * <p/>
     * <pre>
     * StringUtil.isBlank(null)      = true
     * StringUtil.isBlank("null")     = true
     * StringUtil.isBlank("")        = true
     * StringUtil.isBlank(" ")       = true
     * StringUtil.isBlank("bob")     = false
     * StringUtil.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is null, empty or whitespace
     */
    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        str = str.trim();
        return str.length() == 0 || str.equalsIgnoreCase("null");
    }

    /**
     * 如果字符串是<code>null</code>或空字符串<code>""</code>，则返回指定默认字符串，否则返回字符串本身。
     * <pre>
     * StringUtil.defaultIfEmpty(null, "default")  = "default"
     * StringUtil.defaultIfEmpty("", "default")    = "default"
     * StringUtil.defaultIfEmpty("  ", "default")  = "  "
     * StringUtil.defaultIfEmpty("bat", "default") = "bat"
     * </pre>
     *
     * @param str        要转换的字符串
     * @param defaultStr 默认字符串
     * @return 字符串本身或指定的默认字符串
     */
    public static String defaultIfEmpty(String str, String defaultStr) {
        return ((str == null) || (str.length() == 0)) ? defaultStr : str;
    }

    /**
     * 字符串数组用分隔符拼接成字符串
     */
    public static String joinStrArray(String[] array, String split) {
        String tmp = "";
        for (String ss : array) {
            tmp += ss + split;
        }
        String str = tmp.substring(0, tmp.length() - split.length());
        return str;
    }

    /**
     * 多个字符串用分隔符拼接成字符串
     */
    public static String joinStrings(String split, String... strs) {
        List<String> strList = Arrays.asList(strs);
        String tmp = "";
        for (String ss : strList) {
            tmp += ss + split;
        }
        String str = tmp.substring(0, tmp.length() - split.length());
        return str;
    }

    /**
     * 多个字符串用分隔符拼接成字符串
     */
    public static <T> String joinCollection(Collection<T> collection, String split) {
        StringBuffer tmp = new StringBuffer();
        for (T obj : collection) {
            tmp.append(obj).append(split);
        }
        String str = tmp.deleteCharAt(tmp.length() - 1).toString();
        return str;
    }

    /**
     * 输入为map 输出为：appid=xxx&openid=xxx&...
     */
    public static String formRequestParam(Map<String, String> params) {
        String requestParam = params.toString();
        requestParam = requestParam.substring(1, requestParam.length() - 1);
        requestParam = requestParam.replace(", ", "&");

        return requestParam;
    }




    /**
     * 检测多个字符串参数中是否含有null或空值串，有则返回false，无则返回true。 不传入参数，则返回false
     */
    public static boolean checkExistNullOrEmpty(String... args) {
        for (String str : args) {
            if (str == null || str.length() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 模糊处理字符串，除头部保留字符和尾部保留字符之外的字符变为"*****"
     *
     * @param str      待处理字符串
     * @param head     头部保留字符数
     * @param tail     尾部保留字符数
     * @param separate 分隔字符串：只处理分隔字符串首次出现位置之前或之后的字符串
     * @param mode     分隔符处理方式：1代表处理分隔串前字符串，2代表处理分隔串后字符串，其他功能暂未设计
     *                 目前只考虑首次查找分隔串位置。默认为方式1。
     * @return 处理之后的字符串，若str为null或空串，返回null
     *         <p>processStr("abcde@sogou.com", 2, 1, "@", 1) 返回 ab*****e@sogou.com<br/>
     *         processStr("13812345678", 3, 2, null, 1) 返回 138*****78</p>
     */
    public static String processStr(String str, int head, int tail, String separate, int mode) {
        String replacer = "*****";
        if (str == null || str.length() == 0) {
            return null;
        }
        if (separate == null || separate.length() == 0) {
            // 没有分隔符
            if (str.length() < head + tail) {
                // 字符串长度小于需要保留的字符个数
                return replacer;
            }
            return str.substring(0, head) + replacer + str.substring(str.length() - tail);
        } else {
            int begin = 0, end = str.length();
            switch (mode) {
                case 1:
                    end = str.indexOf(separate);
                    if (end == -1) {
                        end = str.length();
                    }
                    break;
                case 2:
                    begin = str.indexOf(separate) + separate.length();
                    if (begin < separate.length()) {
                        begin = 0;
                    }
                    break;
                default:
                    end = str.indexOf(separate);
                    if (end == -1) {
                        end = str.length();
                    }
                    break;
            }
            if (end - begin < head + tail) {
                return str.substring(0, begin) + replacer + str.substring(end, str.length());
            }
            return str.substring(0, begin + head) + replacer + str.substring(end - tail, str.length());
        }
    }

    /**
     * 模糊处理邮件地址，只保留首次@出现位置前字符串的前两个和最后一个，如果不足则全为*****
     * <p>processEmail("abcde@sogou.com") 返回 ab*****e@sogou.com</br>
     * processEmail("abc@sogou.com")返回*****@sogou.com</p>
     *
     * @param email
     * @return
     */
    public static String processEmail(String email) {
        return processStr(email, 2, 1, "@", 1);
    }

    /**
     * 模糊处理手机号码，只保留前三位和最后三位
     * <p>processMobile("13800001234")返回 138*****234</p>
     *
     * @param mobile
     * @return
     */
    public static String processMobile(String mobile) {
        return processStr(mobile, 3, 3, null, 1);
    }

    /**
     * 判断给定字符串是否包含中文
     *
     * @param str
     * @return
     */
    public static boolean containChinese(String str) {
        int i = str.length();
        for (; --i >= 0; ) {
            String b = str.substring(i, i + 1);
            boolean c = Pattern.matches("[\u4E00-\u9FA5]", b);
            if (c) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        String a = "中国China";
        System.out.println("isChinese:" + containChinese(a));
    }
}
