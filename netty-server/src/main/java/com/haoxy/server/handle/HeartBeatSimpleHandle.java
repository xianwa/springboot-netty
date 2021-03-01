package com.haoxy.server.handle;

import com.haoxy.common.model.CustomProtocol;
import com.haoxy.server.util.NettySocketHolder;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by haoxy on 2018/10/17. E-mail:hxyHelloWorld@163.com github:https://github.com/haoxiaoyong1014
 */
public class HeartBeatSimpleHandle extends SimpleChannelInboundHandler<CustomProtocol> {

    private final static Logger LOGGER = LoggerFactory.getLogger(HeartBeatSimpleHandle.class);
    private static final CustomProtocol HEART_BEAT = new CustomProtocol(123456L, CustomProtocol.SendType.HEART, "pong");

    /**
     * 取消绑定
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettySocketHolder.remove((NioSocketChannel) ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                LOGGER.info("已经5秒没有收到信息！");
                //向客户端发送消息
                ctx.writeAndFlush(HEART_BEAT).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CustomProtocol customProtocol) throws Exception {
        // todo 服务端向客户端发送信息
        LOGGER.info("收到customProtocol={}", customProtocol);
        //我们调用writeAndFlush（Object）来逐字写入接收到的消息并刷新线路
        if (customProtocol != null) {
            if (customProtocol.getComId() == 1) {
                customProtocol.setContent("1的响应");
                ctx.writeAndFlush(customProtocol);
            }
            if (customProtocol.getComId() == 2) {
                customProtocol.setContent("2的响应");
                ctx.writeAndFlush(customProtocol);
            }
        }
        //保存客户端与 Channel 之间的关系
        NettySocketHolder.put(customProtocol.getComId(), (NioSocketChannel) ctx.channel());
    }
}
