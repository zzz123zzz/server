package com.yzx.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置类读取工具
 */
public class PropertiesReadUtil {
    /**
     * 根据读取properties文件
     * @param url
     * @return
     * @throws IOException
     */
    public static Properties read(String url) throws IOException {
        Properties properties = new Properties();
        InputStream in = PropertiesReadUtil.class.getClassLoader().getResourceAsStream(url);
        if(in != null){
            properties.load(in);
            in.close();
        }
        return properties;
    }
    public static void main(String args[]) throws IOException {
        Properties per = read("config/route.properties");
        System.out.println(per.get("route-package"));
    }
}
