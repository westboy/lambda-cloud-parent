package com.jingfang.cloud.core.utils;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xiongmao
 * @date 2024/7/17
 **/
public class StringUtils {
    private static final String CHAR_LOWER = "abcdefghjklmnpqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+=-[]{};:,.<>?/";
    private static final String ALL_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBERS+SPECIAL_CHARS ;
    private static Random random = new Random();

    /**
     * 校验多个字符串是否有空值
     *
     * @param strings 字符串组
     * @return 是否有空值 为空返回true
     */
    public static boolean isEmpty(String... strings) {
        boolean flag = false;
        for (String str : strings) {
            if (null == str || "".equals(str) || "null".equals(str)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public static boolean isNull(Object... objects) {
        boolean flag = false;
        for (Object obj : objects) {
            if (null == obj) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 校验多个字符串是否有空值
     *
     * @param strings 字符串组
     * @return 是否有空值 不为空返回true
     */
    public static boolean isNotEmpty(String... strings) {
        boolean flag = false;
        for (String str : strings) {
            if (null == str || "".equals(str) || "null".equals(str)) {
                flag = true;
                break;
            }
        }
        return !flag;
    }

    public static boolean isNotNull(Object... objects) {
        boolean flag = false;
        for (Object obj : objects) {
            if (null == obj) {
                flag = true;
                break;
            }
        }
        return !flag;
    }

    /**
     * 大陆号码或香港号码都可以
     *
     * @param str
     * @return 符合规则返回true
     */
    public static boolean isPhoneLegal(String str) {
        return isChinaPhoneLegal(str) || isHongKongPhoneLegal(str);
    }

    /**
     * 大陆手机号码11位数，匹配格式：前三位固定格式+后8位任意数
     * 此方法中前三位格式有：
     * 13+任意数
     * 145,147,149
     * 15+除4的任意数(不要写^4，这样的话字母也会被认为是正确的)
     * 166
     * 17+3,5,6,7,8
     * 18+任意数
     * 198,199
     *
     * @param str
     * @return 正确返回true
     */
    private static boolean isChinaPhoneLegal(String str) {
        // ^ 匹配输入字符串开始的位置
        // \d 匹配一个或多个数字，其中 \ 要转义，所以是 \\d
        // $ 匹配输入字符串结尾的位置
        String regExp = "^((13[0-9])|(14[5,7,9])|(15[0-3,5-9])|(166)|(17[3,5,6,7,8])" +
                "|(18[0-9])|(19[8,9]))\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 香港手机号码8位数，5|6|8|9开头+7位任意数
     *
     * @param str
     * @return 正确返回true
     */
    private static boolean isHongKongPhoneLegal(String str) {
        // ^ 匹配输入字符串开始的位置
        // \d 匹配一个或多个数字，其中 \ 要转义，所以是 \\d
        // $ 匹配输入字符串结尾的位置
        String regExp = "^(5|6|8|9)\\d{7}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 字符串校验空，如果为空返回空字符串
     */
    public static String reStr(String str) {
        if (null == str) {
            return "";
        } else {
            return str;
        }
    }

    /***
     * 随机生成8位密码 ，包含大小写字母、数字、特殊字符
     * */
    public static String genRandomPassword(){
        return genRandomPassword(8);
    }


    /***
     * 随机生成密码，最少4位 ，包含大小写字母、数字、特殊字符
     * */
    public static String genRandomPassword(int len){
        StringBuilder builder = new StringBuilder(len);
        builder.append(genRandomStr(CHAR_LOWER,1));
        builder.append(genRandomStr(CHAR_UPPER,1));
        builder.append(genRandomStr(NUMBERS,1));
        builder.append(genRandomStr(SPECIAL_CHARS,1));
        builder.append(genRandomStr(ALL_CHARS,len-4));
        for(int i=0;i<len;i++){
            int index = random.nextInt(len);
            char temp=builder.charAt(i);
            builder.setCharAt(i,builder.charAt(index));
            builder.setCharAt(index,temp);
        }
        return builder.toString();
    }

    /***
     * 随机生成字符
     * @param str  元字符
     * @param len  生成字符长度
     * */
    public static String genRandomStr(String str,int len){
        StringBuilder builder = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            int index = random.nextInt(str.length());
             char randomChar = str.charAt(index);
            builder.append(randomChar);
        }
        return builder.toString();
    }

    /***
     * 正则表达式，要求至少包含一个小写字母、一个大写字母、一个数字和一个特殊字符，总长度至少为8位
     * @param str  元字符
     * */
    public static boolean validPasswordString(String str) {
         return validPasswordString(str,8);
    }

    /***
     * 正则表达式，要求至少包含一个小写字母、一个大写字母、一个数字和一个特殊字符
     * @param str  元字符
     * @param len  密码长度
     * */
    public static boolean validPasswordString(String str, int len) {
        // 正则表达式，要求至少包含一个小写字母、一个大写字母、一个数字或一个特殊字符
        //"^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\!\\@\\#\\$\\%\\^&\\*\\(\\)_\\+\\=\\-\\[\\]\\{\\};\\:\\,\\.\\<\\>\\?\\/]).{8,}";
        String passwordRegex="^(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d\\!\\@\\#\\$\\%\\^&\\*\\(\\)_\\+\\=\\-\\[\\]\\{\\};\\:\\,\\.\\<\\>\\?\\/]).{"+len+",}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        return pattern.matcher(str).matches();
    }

}
