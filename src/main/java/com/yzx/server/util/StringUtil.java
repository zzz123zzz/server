package com.yzx.server.util;

/**
 * String工具类
 */
public class StringUtil {
    /**
     * 判断字符串是否为空
     * @param str
     * @return boolean
     */
    public static boolean isEmpty(String str){
        return str == null && "".equals(str.trim());
    }
}
