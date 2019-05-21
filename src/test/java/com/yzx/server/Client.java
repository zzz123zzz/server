package com.yzx.server;

import com.alibaba.fastjson.JSON;
import com.yzx.server.proto.MessageProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {
    public static void main(String args[]){
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline()
                                /* .addLast(new StringEncoder())
                                 .addLast(new StringDecoder())*/
                                .addLast(new ProtobufDecoder(MessageProto.Response.getDefaultInstance()))
                                .addLast(new ProtobufEncoder())
                                .addLast(new SimpleChannelInboundHandler<MessageProto.Response>() {
                                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageProto.Response s) throws Exception {
                                        System.out.println(s.getBody());
                                    }
                                });
                    }});

        /*Channel channel = bootstrap.connect("127.0.0.1", 8088).channel();*/
        try {
            final ChannelFuture future = bootstrap.connect("127.0.0.1",8088).sync();
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    Scanner scanner = new Scanner(System.in);
                    while (true) {
                        System.out.println("请输入路由.....");
                        Map<String,String > param = new HashMap<String, String>();
                        String input = scanner.next();
                        param.put("route",input);
                        System.out.println("请输入用户名...");
                        input = scanner.next();
                        param.put("name",input);
                        System.out.println("请输入密码...");
                        input = scanner.next();
                        param.put("password",input);
                        try {
                            MessageProto.Request request = MessageProto.Request.newBuilder().setBody(JSON.toJSONString(param)).setRoute(param.get("route")).build();
                            future.channel().writeAndFlush(request).sync();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.start();
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
