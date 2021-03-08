package com.haoxy.client.config;

import com.haoxy.common.model.CustomProtocol;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by haoxy on 2018/10/17. E-mail:hxyHelloWorld@163.com github:https://github.com/haoxiaoyong1014
 */
@Configuration
public class HeartBeatConfig {

    @Bean(value = "heartBeat")
    public CustomProtocol heartBeat() {
        return new CustomProtocol(0L, CustomProtocol.SendType.HEART.code, "heartContent","heartKey");
    }
}
