package com.yzx.server.net;

import org.springframework.context.ApplicationContext;

public class Init {
    public static void init(int port, ApplicationContext context){
        try {
            Server.serverInit(port,context);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
