package com.yzx.server.controller;

import com.yzx.server.route.Route;
import com.yzx.server.route.Routes;

/**
 * socket心跳返回
 */
@Routes
public final class HeartBeat {
    @Route("ping")
    public Object ping(){
        return "PONG";
    }
}
