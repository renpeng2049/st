package com.soyoung.st.netty;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class NettyCloseHelper implements InitializingBean, DisposableBean {

    @Override
    public void afterPropertiesSet() throws Exception {

        NettyServerProxy.INSTANCE.getPort();
    }

    @Override
    public void destroy() throws Exception {
        NettyServerProxy.INSTANCE.close();
    }
}
