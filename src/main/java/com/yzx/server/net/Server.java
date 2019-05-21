package com.yzx.server.net;

import com.yzx.server.proto.MessageProto;
import com.yzx.server.route.RouteUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class Server {
    static int count = 0;
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
                socketChannel.pipeline()
                        /*替换成protobuf*/
                      /*  .addLast(new StringDecoder())
                        .addLast(new StringEncoder())*/
                        .addLast(new ProtobufDecoder(MessageProto.Request.getDefaultInstance()))
                        .addLast(new ProtobufEncoder())
                        .addLast(new IdleStateHandler(60,0,0))
                        .addLast(new SimpleChannelInboundHandler<MessageProto.Request>() {

                                    protected void channelRead0(ChannelHandlerContext chc, MessageProto.Request msg) throws Exception {
                                        System.out.println(msg.getRoute());
                                        routeUtil.route(chc.channel(), msg);
                                    }

                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        session.put(ctx.channel().remoteAddress().toString(), ctx);
                                        count ++;
                                        System.out.println("in:"+count);
                                    }

                                    @Override
                                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                        session.remove(ctx.channel().remoteAddress().toString());
                                        for (Map.Entry<String, ChannelHandlerContext> entry : session.entrySet()) {
                                            entry.getValue().writeAndFlush(ctx.channel().remoteAddress() + "断开连接");
                                        }
                                        count --;
                                        System.out.println("out:"+count);
                                    }

                                    @Override
                                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                        System.out.println("total:"+count);
                                    }

                            @Override
                            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                if (evt instanceof IdleStateEvent){
                                    IdleStateEvent event = (IdleStateEvent) evt;
                                    if(event.state() == IdleState.READER_IDLE){
                                        session.remove(ctx.channel().remoteAddress()+"");
                                        ctx.disconnect();
                                    }
                                }else{
                                    super.userEventTriggered(ctx, evt);
                                }
                            }
                        });
            }
        });
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture future = bootstrap.bind(port).sync();
        future.channel().closeFuture().sync();

    }
}
