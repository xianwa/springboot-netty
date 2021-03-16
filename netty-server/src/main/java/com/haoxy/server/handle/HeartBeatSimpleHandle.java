package com.haoxy.server.handle;

import com.google.common.collect.Maps;

import com.fksaas.tms.common.utils.JacksonUtil;
import com.haoxy.common.model.CustomProtocol;
import com.haoxy.server.util.NettySocketHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by haoxy on 2018/10/17. E-mail:hxyHelloWorld@163.com github:https://github.com/haoxiaoyong1014
 */
public class HeartBeatSimpleHandle extends SimpleChannelInboundHandler<String> {

    private final static Logger logger = LoggerFactory.getLogger(HeartBeatSimpleHandle.class);
    private static final CustomProtocol HEART_BEAT = new CustomProtocol(123456L, CustomProtocol.SendType.HEART.code, "pong");

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
                logger.info("已经10秒没有收到信息！");
                // todo 下线
                //向客户端发送消息
                ctx.writeAndFlush(JacksonUtil.serialize(HEART_BEAT)).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // todo 上线
        logger.info("收到customProtocol={}", msg);
        CustomProtocol customProtocol = JacksonUtil.deSerialize(msg,CustomProtocol.class);
        //我们调用writeAndFlush（Object）来逐字写入接收到的消息并刷新线路
        try {
            if (customProtocol != null) {
                if(customProtocol.getComId() == 1){
                    // 测试并发时的代码，会内存溢出，谨慎打开
//                    try {
//                        ExecutorService executorService = Executors.newFixedThreadPool(10);
//                        List<Callable<Object>> runnableList = Lists.newArrayList();
//                        for (int i = 0; i < 10; i++) {
//                            final int finalI = i;
//                            runnableList.add(()->{
//                                CustomProtocol customProtocol1 = new CustomProtocol(finalI, 1, "$" + String.join("", Collections.nCopies(2048, String.valueOf(finalI)) + "$")
//                                );
//                                ctx.writeAndFlush(JacksonUtil.serialize(customProtocol1));
//                                return null;
//                            });
//                        }
//                        executorService.invokeAll(runnableList);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
                if (customProtocol.getComId() == 2) {
                    customProtocol.setContent("2的响应");
                    ctx.writeAndFlush(JacksonUtil.serialize(customProtocol));
                }
                if(customProtocol.getSendType() == CustomProtocol.SendType.TMS_LOGIN.code){
                    Map<String,Object> respMap = Maps.newHashMap();
                    respMap.put("successFlag",true);
                    respMap.put("comShortName","城南华丰");
                    customProtocol.setContent(JacksonUtil.serialize(respMap));
                    ctx.writeAndFlush(JacksonUtil.serialize(customProtocol));
                }
            }
        } catch (Exception e) {
            logger.error("服务端读取客户端消息异常,e:", e);
        }
        //保存客户端与 Channel 之间的关系
        NettySocketHolder.put(customProtocol.getComId(), (NioSocketChannel) ctx.channel());
    }
}
