package com.haoxy.client.heart;

import com.haoxy.client.init.CustomerHandleInitializer;
import com.haoxy.common.model.CustomProtocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import io.netty.channel.socket.SocketChannel;

/**
 * Created by haoxy on 2018/10/17. E-mail:hxyHelloWorld@163.com github:https://github.com/haoxiaoyong1014
 */
@Component
public class HeartbeatClient {
    private final static Logger logger = LoggerFactory.getLogger(HeartbeatClient.class);
    private EventLoopGroup group = new NioEventLoopGroup();
    @Value("${netty.server.port}")
    private int nettyPort;
    @Value("${netty.server.host}")
    private String host;

    private SocketChannel socketChannel;

    @PostConstruct
    public void start() {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class).handler(new CustomerHandleInitializer());
            while (true) {
                if (socketChannel != null && socketChannel.isActive()) {
                    return;
                }
                try {
                    ChannelFuture future = bootstrap.connect(host, nettyPort).sync();
                    if (future.isSuccess()) {
                        logger.info("启动 Netty 成功");
                        return;
                    }
                    socketChannel = (SocketChannel) future.channel();
                } catch (Exception e) {
                    logger.error("客户端连接服务端异常,e:", e);
                    Thread.sleep(3000);
                }
            }
        } catch (Exception e) {
            logger.error("客户端启动异常,e:", e);
        }
    }

    /**
     * 客户端向服务端发送消息
     */
    public void sendData(CustomProtocol customProtocol) {
        //创建连接成功之前停在这里等待
        while (socketChannel == null || !socketChannel.isActive()) {
            System.out.println("等待连接···");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        socketChannel.writeAndFlush(customProtocol);
    }

    /**
     * 连接服务端 and 重连
     */
    public void doConnect() {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class).handler(new CustomerHandleInitializer());
            if (socketChannel != null && socketChannel.isActive()) {
                return;
            }
            try {
                ChannelFuture future = bootstrap.connect(host, nettyPort).sync();
                if (future.isSuccess()) {
                    logger.info("连接成功");
                }
                socketChannel = (SocketChannel) future.channel();
            } catch (Exception e) {
                logger.error("客户端连接服务端异常,e:", e);
            }
        } catch (Exception e) {
            logger.error("客户端连接服务端异常,e:", e);
        }
    }

}
