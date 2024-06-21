/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.traccar.BaseFrameDecoder;

/**
 *
 * @author addon10
 */
public class TTteltonika09FrameDecoder extends BaseFrameDecoder{
    
    private static final int MESSAGE_MINIMUM_LENGTH = 12;

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ByteBuf buf) throws Exception {

        while (buf.isReadable() && buf.getByte(buf.readerIndex()) == (byte) 0xff) {
            buf.skipBytes(1);
        }

        if (buf.readableBytes() < MESSAGE_MINIMUM_LENGTH) {
            return null;
        }

        int length = buf.getUnsignedShort(buf.readerIndex());
        if (length > 0) {
            if (buf.readableBytes() >= (length + 2)) {
                return buf.readRetainedSlice(length + 2);
            }
        } else {
            int dataLength = buf.getInt(buf.readerIndex() + 4);
            if (buf.readableBytes() >= (dataLength + 12)) {
                return buf.readRetainedSlice(dataLength + 12);
            }
        }

        return null;
    }

    
}
