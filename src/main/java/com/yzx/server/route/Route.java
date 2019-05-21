package com.yzx.server.route;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Route的自定义注解
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {
    String value();
}
