package com.soyoung.st.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public enum NettyServerProxy {

    INSTANCE;


    private Logger logger = LoggerFactory.getLogger(NettyServerProxy.class);

    //维护连接的所有channel
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private final ConcurrentHashMap<String,Channel> channelMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer /* opaque */, ResponseFuture> responseTable =
            new ConcurrentHashMap<Integer, ResponseFuture>(256);

    private final int DEFAULT_THREAD_NUM = Runtime.getRuntime().availableProcessors() * 2;

    private int port = 7890;

    private int nioThreadNum = DEFAULT_THREAD_NUM;

    private int bizThreadNum = DEFAULT_THREAD_NUM;

    private ChannelFuture future;

    private ServerBootstrap bootstrap;

    private EventExecutorGroup bizGroup = null;

    private NettyServerProxy(){

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(nioThreadNum); // netty默认NIO线程数为CPU数*2

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
            throw new IllegalStateException("通道服务netty启动失败");
        }
    }

    public void addToChannelGroup(Channel channel){
        //新连接，加入channelGroup
        channelGroup.add(channel);

        String ip = parseSocketAddressAddr(channel.remoteAddress());
        channelMap.put(ip,channel);
        logger.info("新连接建立，{},当前channelGroup个数:{}",channel,channelGroup.size());
    }

    public void removeFromChannelGroup(Channel channel){
        String ip = parseSocketAddressAddr(channel.remoteAddress());
        channelMap.remove(ip);
    }

    public ChannelOsRsp sendMsg(NettyMessage msg,String ip,Long timeoutMillis) throws RemotingException,InterruptedException, RemotingSendRequestException, RemotingTimeoutException{


        Channel channel = channelMap.get(ip);
        if(null == channel){
            throw new RemotingException("未发现此ip地址的channel:"+ip);
        }

        final int opaque = msg.getLogId();

        try {
            final ResponseFuture responseFuture = new ResponseFuture(channel, opaque, timeoutMillis, null, null);
            this.responseTable.put(opaque, responseFuture);
            final SocketAddress addr = channel.remoteAddress();

            ByteBuf buf = Unpooled.copiedBuffer(msg.composeFull());
            channel.writeAndFlush(buf).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture f) throws Exception {
                    if (f.isSuccess()) {
                        responseFuture.setSendRequestOK(true);
                        return;
                    } else {
                        responseFuture.setSendRequestOK(false);
                    }

                    responseTable.remove(opaque);
                    responseFuture.setCause(f.cause());
                    responseFuture.putResponse(null);
                    logger.warn("send a request command to channel <" + addr + "> failed.");
                }
            });

            ChannelOsRsp responseCommand = responseFuture.waitResponse(timeoutMillis);

            if (null == responseCommand) {
                if (responseFuture.isSendRequestOK()) {
                    throw new RemotingTimeoutException(parseSocketAddressAddr(addr), timeoutMillis,
                            responseFuture.getCause());
                } else {
                    throw new RemotingSendRequestException(parseSocketAddressAddr(addr), responseFuture.getCause());
                }
            }

            return responseCommand;

        } finally {
            this.responseTable.remove(opaque);
        }

    }

    public void putResponse(ChannelOsRsp msg,Integer logId){

        ResponseFuture responseFuture = this.responseTable.get(logId);
        if(null != responseFuture){

            this.responseTable.remove(logId);
            responseFuture.putResponse(msg);
        }
    }

    public int getPort(){
        return this.port;
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
        logger.info("netty server stop");
    }

    public String parseSocketAddressAddr(SocketAddress socketAddress) {
        if (socketAddress != null) {
            final String addr = socketAddress.toString();

            if (addr.length() > 0) {
                int index = addr.indexOf(":");
                return index != -1 ?addr.substring(1,index) : addr.substring(1);
            }
        }
        return "";
    }
}
