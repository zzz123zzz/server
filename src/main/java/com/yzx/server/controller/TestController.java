package com.yzx.server.controller;

import com.yzx.server.net.Server;
import com.yzx.server.route.Route;
import com.yzx.server.route.Routes;
import com.yzx.server.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
@Routes
public class TestController {
    private TestService impl = (TestService) Server.getApplicationContext().getBean(TestService.class);
    @Route("login")
    public Object login(){

        impl.test();
        return "login";
    }
}
