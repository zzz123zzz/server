package com.yzx.server.net;

import com.yzx.server.route.RouteUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class Server {
    final static RouteUtil routeUtil = RouteUtil.newInstance();
    private static ApplicationContext context;
    final static Map<String, ChannelHandlerContext> session = new HashMap<String, ChannelHandlerContext>();
    public static void setApplicationContext(ApplicationContext contexts){
        context = contexts;
    }
    public static ApplicationContext getApplicationContext(){
        return context;
    }
    public static  void serverInit(int port,ApplicationContext context) throws InterruptedException {
        setApplicationContext(context);
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new StringDecoder()).addLast(new StringEncoder()).addLast(new SimpleChannelInboundHandler<String>() {

                    protected void channelRead0(ChannelHandlerContext chc, String s) throws Exception {
                        System.out.println(s);
                        routeUtil.route(chc.channel(), s);
                    }

                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        session.put(ctx.channel().remoteAddress().toString(), ctx);
                        System.out.println(ctx.channel().remoteAddress() + " 进入服务器");
                    }

                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        session.remove(ctx.channel().remoteAddress().toString());
                        for (Map.Entry<String, ChannelHandlerContext> entry : session.entrySet()) {
                            entry.getValue().writeAndFlush(ctx.channel().remoteAddress() + "断开连接");
                        }
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

                    }
                });
            }
        });
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture future = bootstrap.bind(port).sync();
        future.channel().closeFuture().sync();

    }
}
