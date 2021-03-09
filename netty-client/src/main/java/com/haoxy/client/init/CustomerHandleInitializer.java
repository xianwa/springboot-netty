package com.haoxy.client.init;

import com.haoxy.client.handle.EchoClientHandle;
import com.haoxy.common.encode.DelimiterBasedFrameEncoder;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by haoxy on 2018/10/17.
 * E-mail:hxyHelloWorld@163.com
 * github:https://github.com/haoxiaoyong1014
 */
public class CustomerHandleInitializer extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel channel) throws Exception {
        String delimiter = "_$@^%";
        channel.pipeline()
                //10 秒没发送消息 将IdleStateHandler 添加到 ChannelPipeline 中
                .addLast(new IdleStateHandler(5, 5, 0))
                // 5M
                .addLast(new DelimiterBasedFrameDecoder(5242880,
                        Unpooled.wrappedBuffer(delimiter.getBytes())))
                // 将分隔之后的字节数据转换为字符串数据
                .addLast(new StringDecoder())
                // 这是我们自定义的一个编码器，主要作用是在返回的响应数据最后添加分隔符
                .addLast(new DelimiterBasedFrameEncoder(delimiter))
                .addLast(new EchoClientHandle());
    }
}
