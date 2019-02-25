package com.soyoung.st.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
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
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) { // 20s
                // throw new Exception("idle exception");
                logger.info("idle 20s, send heartbeat");
                ByteBuf buf = Unpooled.copiedBuffer(NettyMessage.HEATBEAT_MSG.composeFull());
                ctx.writeAndFlush(buf);
            } else if (state == IdleState.READER_IDLE) { // 60s
                logger.info("连接timeout,请求关闭 " + ctx.channel());
                ctx.close();
            }
            // 注意事项：
            // 因为我实现的逻辑是所有IdleStateEvent只由NettyHeartBeatHandler一个Handler处理即可；
            // 所以可以不需要将事件继续向pipeline后续的Handler传递，当然传递了也没什么事，因为其他的地方不能处理；
            // 在某些情况下，如果你定义的事件需要通知多个Handler处理，那么一定要加上下面这一句才行。
            // super.userEventTriggered(ctx, evt);
        } else {
            // 其他事件转发给Pipeline中其他的Handler处理
            logger.info(">>>>>>>");
            super.userEventTriggered(ctx, evt);
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        logger.info("连接建立" + ctx.channel());
        NettyServerProxy.INSTANCE.addToChannelGroup(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("连接关闭" + ctx.channel());
        super.channelInactive(ctx);
        NettyServerProxy.INSTANCE.removeFromChannelGroup(ctx.channel());
    }
}
