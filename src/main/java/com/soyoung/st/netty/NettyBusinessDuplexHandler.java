package com.soyoung.st.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyBusinessDuplexHandler extends ChannelDuplexHandler {
    private static Logger logger = LoggerFactory.getLogger(NettyBusinessDuplexHandler.class);

    private AppBusinessProcessor bizProcessor = null;

    public NettyBusinessDuplexHandler(AppBusinessProcessor appBizHandler) {
        super();
        this.bizProcessor = appBizHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyMessage bizMsg = (NettyMessage) msg; // 拆分好的消息

        if (bizMsg.getMessageType() == NettyMessage.MESSAGE_TYPE_HB) {
            logger.info("收到心跳  -- {}", bizMsg.toString());
        } else {
            // 处理业务消息
            logger.info("收到消息  -- {}", bizMsg.toString());
            bizProcessor.process(bizMsg);
            // 如果接收到的是请求，则需要写回响应消息
            if (bizMsg.getFlag() == 0) {
                bizMsg.setFlag((byte) 1);
                logger.info("写回消息  -- {}", bizMsg.toString());
                ByteBuf rspMsg = Unpooled.copiedBuffer(bizMsg.composeFull());
                ctx.writeAndFlush(rspMsg);
            }
        }
        // 继续传递给Pipeline下一个Handler
        // super.channelRead(ctx, msg);
        // ctx.fireChannelRead(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        logger.info("连接建立2" + ctx.channel());
        //Constants.channels.add(ctx.channel());
        NettyServerProxy.INSTANCE.addToChannelGroup(ctx.channel());
    }

}
