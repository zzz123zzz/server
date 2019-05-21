package com.yzx.server.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 读取类的工具类
 */
public class ClassUtil {
    /**
     * 读取所有的类
     * @param packageName
     * @return List<Class>
     */
    public static List<Class> getClass(String packageName){

        packageName = packageName.replaceAll("\\.","/");

        String [] paths = packageName.split(",");

        List<Class> classes = new ArrayList<Class>();
        for (int i = 0; i < paths.length; i++) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(paths[i]);
            if(url != null){
                File file = new File(url.getPath());
                getClass(classes,file,paths[i]);
            }

        }
        return classes;
    }

    /**
     * 读取所有类
     * @param name
     * @return
     */
    public static void getClass(List<Class> cls, File file, String name){
        if (file.exists()) {
            //是文件
            if (file.isFile()) {
                try {
                    String className = null;
                    if (name.contains(".class")) {
                        className = name.replace(".class", "");
                    }else {
                        className  = (name+"."+file.getName()).replace(".class", "");
                    }
                    className = className.replaceAll("/","\\.");
                    System.out.println(className);
                    cls.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            //是目录
            else if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    String packAge=name+"."+f.getName();
                    getClass(cls, f, packAge);
                }
            }
        }
    }
}
