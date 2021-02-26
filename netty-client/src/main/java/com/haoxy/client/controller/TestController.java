package com.haoxy.client.controller;

import com.haoxy.client.heart.HeartbeatClient;
import com.haoxy.common.model.CustomProtocol;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author xian.wang
 * @since 上午11:20 2021/2/26
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private HeartbeatClient heartbeatClient;

    @RequestMapping("req")
    public void req(CustomProtocol req){
        heartbeatClient.sendData(req);
    }

}
