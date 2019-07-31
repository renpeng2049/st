package com.soyoung.st.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class NettyClient {

    private static Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private String ip;

    private int port;

    Bootstrap bootstrap;

    ChannelFuture future;

    private EventExecutorGroup bizGroup = null;

    public NettyClient() {
        bizGroup = new DefaultEventExecutorGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new IdleStateHandler(60, 20, 0, TimeUnit.SECONDS));
                    ch.pipeline().addLast(new NettyClientHeartBeatDuplexHandler());
                    ch.pipeline().addLast(new NettyMessageDecoder());
                    ch.pipeline().addLast(bizGroup, new NettyClientBusinessDuplexHandler(new ClientBusinessProcessor()));
                }
            });

            // future = b.connect(ip, port);
            bootstrap = b;
            // logger.info("成功连接到 {}:{}", ip, port);
        } catch (Throwable t) {
            logger.error("异常", t);
        }
    }

    /**
     * 连接远程主机
     */
    public void connect() {
        try {
            future = bootstrap.connect(ip, port).sync();
            logger.info("成功连接到 {}:{}", ip, port);
        } catch (InterruptedException e) {
            logger.info("连接服务器异常", e);
        }
    }

    public void disConnect() {
        if (future != null) {
            future.channel().close();
            future = null;
        }
    }

    /**
     * 断开跟远程主机的连接，并关闭相应的线程等资源
     */
    public void close() {
        // 关闭业务线程池
        if (bizGroup != null) {
            bizGroup.shutdownGracefully();
        }
        if (future != null) {
            future.channel().close();
        }
        if (bootstrap != null) {
            if (bootstrap.config().group() != null) {
                bootstrap.config().group().shutdownGracefully();
            }
        }

        logger.info("成功关闭");
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        NettyClient client = new NettyClient();
        client.setIp("127.0.0.1");
        client.setPort(7899);

        client.connect();

        //控制台输入
        Scanner sc = new Scanner(System.in);

        while (sc.hasNext()) {

            String message = sc.next(); //TODO 控制台输入

            NettyMessage bizMsg = new NettyMessage(message);
            bizMsg.setLogId(newLogId());

            ByteBuf buf = Unpooled.copiedBuffer(bizMsg.composeFull());

            client.future.channel().writeAndFlush(buf);
        }

    }

    private static int newLogId() {
        int logId = randomInt(1000000, 10000000);
        if (logId % 2 == 0) {
            logId -= 1;
        }

        return logId;
    }

    public static int randomInt(int min, int max) {
        Random random = new Random(System.currentTimeMillis());
        // int x = (int) (Math.random() * max + min);
        int s = random.nextInt(max) % (max - min + 1) + min;
        return s;
    }
}
