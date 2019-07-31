package com.soyoung.st.utils;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class CloseHelper implements InitializingBean, DisposableBean {

    public void destroy() throws Exception {
        SerialFileOperator.INSTANCE.close();
    }

    public void afterPropertiesSet() throws Exception {

    }
}

