package com.gtdq.netty;

import com.gtdq.netty.nettyCollections.TestNetty;
import com.gtdq.netty.util.SpringContextUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NettyApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettyApplication.class, args);
        TestNetty bean = SpringContextUtil.getBean(TestNetty.class);
//        bean.testAllSend();
//        bean.testContinueSend();
        bean.testAllSendConcur();
    }

}
