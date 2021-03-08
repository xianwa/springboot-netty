package com.haoxy.common.decode;

import com.haoxy.common.model.CustomProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by haoxy on 2018/10/17.
 * E-mail:hxyHelloWorld@163.com
 * github:https://github.com/haoxiaoyong1014
 * 服务端解码器
 */
public class HeartbeatDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        long id = byteBuf.readLong();
        int sendType = byteBuf.readInt();
        byte[] contentBytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(contentBytes);
        byte[] keyBytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(keyBytes);


        String content = new String(contentBytes);
        CustomProtocol customProtocol = new CustomProtocol();
        customProtocol.setComId(id);
        customProtocol.setContent(content);
        customProtocol.setSendType(sendType);
        customProtocol.setKey(new String(keyBytes));
        list.add(customProtocol);
    }
}
