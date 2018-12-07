package com.soyoung.st.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyHeartBeatDuplexHandler  extends ChannelDuplexHandler {

    private static Logger logger = LoggerFactory.getLogger(NettyHeartBeatDuplexHandler.class);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("连接关闭" + ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("连接建立" + ctx.channel());
        super.channelActive(ctx);
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
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("异常: ", cause);
        ctx.close();
    }
}
