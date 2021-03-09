package com.haoxy.server.init;

import com.haoxy.common.encode.DelimiterBasedFrameEncoder;
import com.haoxy.server.handle.HeartBeatSimpleHandle;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by haoxy on 2018/10/17. E-mail:hxyHelloWorld@163.com github:https://github.com/haoxiaoyong1014
 */
public class HeartbeatInitializer extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel channel) throws Exception {
        String delimiter = "_$@^%";
        channel.pipeline()
                //五秒没有收到消息 将IdleStateHandler 添加到 ChannelPipeline 中
                .addLast(new IdleStateHandler(15, 0, 0))
                .addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE,
                        Unpooled.wrappedBuffer(delimiter.getBytes())))
                // 将分隔之后的字节数据转换为字符串数据
                .addLast(new StringDecoder())
                // 这是我们自定义的一个编码器，主要作用是在返回的响应数据最后添加分隔符
                .addLast(new DelimiterBasedFrameEncoder(delimiter))
                .addLast(new HeartBeatSimpleHandle());
    }
}
