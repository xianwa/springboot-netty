package com.haoxy.client.handle;

import com.fksaas.tms.common.utils.JacksonUtil;
import com.haoxy.client.util.SpringBeanFactory;
import com.haoxy.common.model.CustomProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by haoxy on 2018/10/17.
 * E-mail:hxyHelloWorld@163.com
 * github:https://github.com/haoxiaoyong1014
 *
 * EchoClientHandle继承了 ChannelInboundHandlerAdapter 的一个扩展(SimpleChannelInboundHandler),
 * 而ChannelInboundHandlerAdapter是ChannelInboundHandler的一个实现
 * ChannelInboundHandler提供了可以重写的各种事件处理程序方法
 *  目前，只需继承 SimpleChannelInboundHandler或ChannelInboundHandlerAdapter 而不是自己实现处理程序接口。
 *  我们在这里重写了channelRead0（）事件处理程序方法
 */
public class EchoClientHandle extends SimpleChannelInboundHandler<CustomProtocol> {

    private final static Logger LOGGER = LoggerFactory.getLogger(EchoClientHandle.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                LOGGER.info("已经10秒没收到消息了");
                //向服务端发送消息
                CustomProtocol heartBeat = SpringBeanFactory.getBean("heartBeat",CustomProtocol.class);
                ctx.writeAndFlush(heartBeat).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }

        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     *  每当从服务端接收到新数据时，都会使用收到的消息调用此方法 channelRead0(),在此示例中，接收消息的类型是ByteBuf。
     * @param channelHandlerContext
     * @param byteBuf
     * @throws Exception
     */
    // todo 把byteBuf改了就报错了，哈哈哈
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, CustomProtocol obj) throws Exception {
        //从服务端收到消息时被调用
//        if(obj instanceof ByteBuf){
//        LOGGER.info("客户端收到消息={}", ((ByteBuf)obj).toString(CharsetUtil.UTF_8));
//        }
//
//        if(obj instanceof CustomProtocol){
            LOGGER.info("客户端收到消息={}", JacksonUtil.serialize(obj));
//        }
    }
}
