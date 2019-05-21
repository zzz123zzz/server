package com.yzx.server.route;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yzx.server.proto.MessageProto;
import com.yzx.server.util.ClassUtil;
import io.netty.channel.Channel;
import org.apache.ibatis.javassist.ClassPool;
import org.apache.ibatis.javassist.CtClass;
import org.apache.ibatis.javassist.CtMethod;
import org.apache.ibatis.javassist.NotFoundException;
import org.apache.ibatis.javassist.bytecode.CodeAttribute;
import org.apache.ibatis.javassist.bytecode.LocalVariableAttribute;
import org.apache.ibatis.javassist.bytecode.MethodInfo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.yzx.server.util.PropertiesReadUtil.*;

/**
 * socke路由分发，模拟SpringMVC实现
 * 使用反射原理
 */
public class RouteUtil {
    /**
     * 内存中记录控制器类集合
     */
    private static Map<Class,Object> controllerClasses = new HashMap<Class, Object>();
    private static Map<String,ClassAndMethodDTO> map = new HashMap<String, ClassAndMethodDTO>();
    private static RouteUtil routeUtil;
    //读取包下有controller注解的控制器中的方法并储存
    static{
        try {
            Properties pro = read("route.properties");
            String  packageName=(String)pro.get("route-package");
            //获取此包在磁盘的位置
            List<Class> cls = ClassUtil.getClass(packageName);
            List<String> classTypes = new ArrayList<String>();
            for (Class clz:cls){
                if(clz.isAnnotationPresent(Routes.class)){
                    Route classRoute = (Route) clz.getAnnotation(Route.class);
                    String classRouteValue = "";
                    if (classRoute != null){
                        classRouteValue = classRoute.value();
                        classRouteValue = classRouteValue.indexOf("/")== 0 ? classRouteValue : "/"+classRouteValue;
                    }
                    Method[] methods = clz.getMethods();
                    for (Method method:methods) {
                        if (method.isAnnotationPresent(Route.class)){
                            String methodVaule = method.getAnnotation(Route.class).value();
                            methodVaule = methodVaule.indexOf("/") == 0 ? methodVaule : "/" + methodVaule;
                            String type = classRouteValue + methodVaule;
                            if (map.containsKey(type)){
                                throw new RuntimeException("装载失败，此路由已存在!");
                            }
                            ClassAndMethodDTO dot = new ClassAndMethodDTO();
                            dot.setClz(clz);
                            dot.setMethod(method);
                            map.put(type,dot);
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static RouteUtil newInstance(){
        if(routeUtil == null){
            routeUtil = new RouteUtil();
        }
        return  routeUtil;
    }
    public void route(Channel channel, MessageProto.Request request){
        JSONObject json = JSON.parseObject(request.getBody());
        String route = request.getRoute();
        route = route.indexOf("/") == 0?route:"/"+route;
        ClassAndMethodDTO dto = map.get(route);
        if (dto==null) {
            try {
                channel.writeAndFlush("资源不存在！").sync();
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Method method = dto.getMethod();
            Class clz = dto.getClz();
            List<Object> parameters = getMethodParameters(clz,method,json,channel);
            Object obj = getObject(clz);
            //执行方法
            Object result = method.invoke(obj,parameters.toArray());
            MessageProto.Response response = MessageProto.Response.newBuilder().setBody(JSON.toJSONString(result)).setStatus(200).build();
            channel.writeAndFlush(response).sync();
        } catch (Exception e) {
            try {
                e.printStackTrace();
                MessageProto.Response response = MessageProto.Response.newBuilder().setStatus(500).setMsg(e.getMessage()).build();
                channel.writeAndFlush(response).sync();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }


    }
    private Object getObject(Class clz){
        Object obj = controllerClasses.get(clz);
        if (obj==null) {
            try {
                obj = clz.newInstance();
                //放进缓存
                controllerClasses.put(clz, obj);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }
    private List<Object> getMethodParameters(Class clz, Method method, JSONObject json, Channel channel){
        List<Object> values = new ArrayList<Object>();
        ClassPool pool = ClassPool.getDefault();
        try {
            CtClass ctClass = pool.get(clz.getName());
            CtMethod ctMethod = ctClass.getDeclaredMethod(method.getName());
            // 使用javassist的反射方法的参数名
            MethodInfo methodInfo = ctMethod.getMethodInfo();
            CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
            LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
                    .getAttribute(LocalVariableAttribute.tag);
            if (attr != null) {
                CtClass[] types = ctMethod.getParameterTypes();
                // 非静态的成员函数的第一个参数是this
                int pos = Modifier.isStatic(ctMethod.getModifiers()) ? 0 : 1;
                for (int i = 0; i < types.length; i++) {
                    //参数名
                    String name = attr.variableName(i + pos);
                    //参数类型
                    String type = types[i].getName();
                    //解析值
                    if (type.contains("String")) {
                        values.add(json.getString(name));
                    }else if (type.contains("Boolean")) {
                        values.add(json.getBoolean(name));
                    }else if (type.contains("boolean")) {
                        values.add(json.getBooleanValue(name));
                    }else if (type.contains("Integer")) {
                        values.add(json.getInteger(name));
                    }else if (type.contains("int")) {
                        values.add(json.getIntValue(name));
                    }else if (type.contains("Long")) {
                        values.add(json.getLong(name));
                    }else if (type.contains("long")) {
                        values.add(json.getLongValue(name));
                    }else if (type.contains("Short")) {
                        values.add(json.getShort(name));
                    }else if (type.contains("short")) {
                        values.add(json.getShortValue(name));
                    }else if (type.contains("Byte")) {
                        values.add(json.getByte(name));
                    }else if (type.contains("byte")) {
                        values.add(json.getByteValue(name));
                    }else if (type.contains("Char")) {
                        values.add(json.getString(name).charAt(0));
                    }else if (type.contains("Character")) {
                        values.add(new Character(json.getString(name).charAt(0)));
                    }else if (type.contains("Float")) {
                        values.add(json.getFloat(name));
                    }else if (type.contains("float")) {
                        values.add(json.getFloatValue(name));
                    }else if (type.contains("Double")) {
                        values.add(json.getDouble(name));
                    }else if (type.contains("double")) {
                        values.add(json.getDoubleValue(name));
                    }else if (type.contains("Channel")){
                        values.add(channel);
                    }
                }
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return values;
    }

}
