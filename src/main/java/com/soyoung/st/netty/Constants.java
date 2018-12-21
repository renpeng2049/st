package com.soyoung.st.netty;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class Constants {


    /** 魔法数 */
    public static final int MAGIC_NUMBER = 16787777;

    /** 编码 */
    public static final String ENCODING = "UTF-8"; //

    public static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
}
