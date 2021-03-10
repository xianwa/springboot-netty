package com.haoxy.client.handle;

import com.google.common.collect.Lists;

import com.fksaas.tms.common.utils.JacksonUtil;
import com.haoxy.client.heart.HeartbeatClient;
import com.haoxy.client.util.SpringBeanFactory;
import com.haoxy.common.model.CustomProtocol;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by haoxy on 2018/10/17. E-mail:hxyHelloWorld@163.com github:https://github.com/haoxiaoyong1014
 * <p>
 * EchoClientHandle继承了 ChannelInboundHandlerAdapter 的一个扩展(SimpleChannelInboundHandler),
 * 而ChannelInboundHandlerAdapter是ChannelInboundHandler的一个实现 ChannelInboundHandler提供了可以重写的各种事件处理程序方法
 * 目前，只需继承 SimpleChannelInboundHandler或ChannelInboundHandlerAdapter 而不是自己实现处理程序接口。
 * 我们在这里重写了channelRead0（）事件处理程序方法
 */
public class EchoClientHandle extends SimpleChannelInboundHandler<String> {

    private final static Logger logger = LoggerFactory.getLogger(EchoClientHandle.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state().equals(IdleState.READER_IDLE)) {
                System.out.println("长期没收到服务器推送数据");
                //可以选择重新连接
                HeartbeatClient heartbeatClient = SpringBeanFactory.getBean("heartbeatClient", HeartbeatClient.class);
                heartbeatClient.doConnect();
            } else if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                logger.info("已经10秒没推送消息了");
                //向服务端发送消息
                CustomProtocol heartBeat = SpringBeanFactory.getBean("heartBeat", CustomProtocol.class);
                ctx.writeAndFlush(JacksonUtil.serialize(heartBeat)).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }

        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 每当从服务端接收到新数据时，都会使用收到的消息调用此方法 channelRead0(),在此示例中，接收消息的类型是ByteBuf。
     *
     * @param channelHandlerContext
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        //从服务端收到消息时被调用
        logger.info("客户端收到消息={}", msg);
        CustomProtocol customProtocol = JacksonUtil.deSerialize(msg,CustomProtocol.class);
        if(customProtocol.getComId() == 1){
            // 测试并发时的代码，会内存溢出，谨慎打开
//            try {
//                ExecutorService executorService = Executors.newFixedThreadPool(10);
//                List<Callable<Object>> runnableList = Lists.newArrayList();
//                for (int i = 0; i < 3; i++) {
//                    final int finalI = i;
//                    runnableList.add(()->{
//                        CustomProtocol customProtocol1 = new CustomProtocol(finalI, 2, "$" + String.join("", Collections.nCopies(2048, String.valueOf(finalI)) + "$")
//                        );
//                        channelHandlerContext.writeAndFlush(JacksonUtil.serialize(customProtocol1));
//                        return null;
//                    });
//                }
//                executorService.invokeAll(runnableList);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.err.println("掉线了...");
        // todo 完善断线重连的逻辑
        //使用过程中断线重连
        final HeartbeatClient heartbeatClient = SpringBeanFactory.getBean("heartbeatClient", HeartbeatClient.class);
        final EventLoop eventLoop = ctx.channel().eventLoop();
        eventLoop.schedule(new Runnable() {
            @Override
            public void run() {
                heartbeatClient.doConnect();
            }
        }, 5L, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }
}
