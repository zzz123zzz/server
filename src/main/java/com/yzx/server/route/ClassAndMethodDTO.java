package com.yzx.server.route;

import java.lang.reflect.Method;

public class ClassAndMethodDTO {
    private Class clz;
    private Method method;
    public Class getClz() {
        return clz;
    }
    public void setClz(Class clz) {
        this.clz = clz;
    }
    public Method getMethod() {
        return method;
    }
    public void setMethod(Method method) {
        this.method = method;
    }
}
