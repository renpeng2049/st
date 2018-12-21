package com.soyoung.st.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class NettyServer {

    /**
     *
     * <p>
     * 使用Netty创建Server程序.
     * <p>
     * Tips:
     * <ol>
     * <li>Netty默认的线程数量为CPU个数*2；</li>
     * <li>netty默认使用SLF4j日志组件，可以通过代码设置:{@code InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);}</li>
     * <li>pipeline添加Handler时，可以通过参数指定执行Handler的线程池，默认为workerGroup执行；</li>
     * <li>LoggingHandler可以很方便观察到Netty方法执行过程，日志级别设置为DEBUG，log4j配置文件中也需要设置为debug；</li>
     * <li>注意Pipeline中事件处理，如果需要后续Handler接着处理，一定不要忘记调用ctx.fireXXX方法；</li>
     * </ol>
     *
     * @author
     * @date 2017.09.20
     *
     */
    private static Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private static final int DEFAULT_THREAD_NUM = Runtime.getRuntime().availableProcessors() * 2;

    private int port = 7890;

    private int nioThreadNum = DEFAULT_THREAD_NUM;

    private int bizThreadNum = DEFAULT_THREAD_NUM;

    private ChannelFuture future;

    private ServerBootstrap bootstrap;

    private EventExecutorGroup bizGroup = null;


    public NettyServer(int port) {
        this.port = port;
    }

    public NettyServer(int port, int nioThreadNum, int bizThreadNum) {
        this.port = port;
        this.nioThreadNum = nioThreadNum;
        this.bizThreadNum = bizThreadNum;
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(nioThreadNum); // netty默认NIO线程数为CPU数*2

        // netty默认使用SLF4j日志组件
        // InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
        // LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);
        // 业务线程池，用于执行业务
        bizGroup = new DefaultEventExecutorGroup(bizThreadNum);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
            // 设置用于ServerSocketChannel的属性和handler
            b.handler(new ChannelInitializer<ServerSocketChannel>() {

                protected void initChannel(ServerSocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                }
            });
            b.option(ChannelOption.SO_BACKLOG, 128);
            b.option(ChannelOption.SO_REUSEADDR, true);

            // 设置用于SocketChannel的属性和handler
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new IdleStateHandler(60, 20, 0, TimeUnit.SECONDS));
                    ch.pipeline().addLast(new NettyHeartBeatDuplexHandler());
                    ch.pipeline().addLast(new NettyMessageDecoder());
                    ch.pipeline().addLast(bizGroup, new NettyBusinessDuplexHandler(new ServerBusinessProcessor()));

                }
            });

            b.childOption(ChannelOption.SO_KEEPALIVE, true);
            b.childOption(ChannelOption.TCP_NODELAY, true);
            b.childOption(ChannelOption.SO_REUSEADDR, true);

            // Bind and start to accept incoming connections.
            future = b.bind(port).sync();
            bootstrap = b;
            logger.info("server started sucessfully.");
        } catch (Throwable t) {
            logger.error("异常", t);
        }
    }

    public void close() {
        // 关闭业务线程池
        if (bizGroup != null) {
            bizGroup.shutdownGracefully();
        }

        if (future != null) {
            future.channel().close();
            // future.channel().closeFuture().sync();
        }

        if (bootstrap != null) {
            if (bootstrap.config().group() != null) {
                bootstrap.config().group().shutdownGracefully();
            }
            if (bootstrap.config().childGroup() != null) {
                bootstrap.config().childGroup().shutdownGracefully();
            }
        }
    }

    public int getPort() {
        return port;
    }

    public static void main(String[] args) {
        NettyServer server = new NettyServer(7899);
        server.start();


        //控制台输入
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {

            String message = sc.next(); //TODO 控制台输入

            logger.info("控制台输入:{}",message);
            NettyMessage bizMsg = new NettyMessage(message);
            bizMsg.setLogId(123);


            ByteBuf buf = Unpooled.copiedBuffer(bizMsg.composeFull());
            Constants.channels.writeAndFlush(buf);
        }
    }

    private static void waitToQuit(NettyServer server) throws IOException {
        logger.info("Server is running on port {}, press q to quit.", server.getPort());
        boolean input = true;
        while (input) {
            int b = System.in.read();
            switch (b) {
                case 'q':
                    logger.info("Server关闭...");
                    server.close();
                    input = false;
                    break;
                case '\r':
                case '\n':
                    break;
                default:
                    logger.info("q -- quit.");
                    break;
            }
        }
    }

}
