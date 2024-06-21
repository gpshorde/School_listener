/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.protocol;

import org.traccar.BaseProtocolEncoder;
import org.traccar.Protocol;
import org.traccar.helper.Checksum;
import org.traccar.helper.DataConverter;
import org.traccar.model.Command;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

/**
 *
 * @author addon10
 */
public class TTteltonika09ProtocolEncoder extends BaseProtocolEncoder{
    
   

    public ByteBuf encodeContent(byte[] content) {

        ByteBuf buf = Unpooled.buffer();

        buf.writeInt(0);
        buf.writeInt(content.length + 8);
        buf.writeByte(TTteltonikaProtocolDecoder.CODEC_12);
        buf.writeByte(1); // quantity
        buf.writeByte(5); // type
        buf.writeInt(content.length);
        buf.writeBytes(content);
        buf.writeByte(1); // quantity
        buf.writeInt(Checksum.crc16(Checksum.CRC16_IBM, buf.nioBuffer(8, buf.writerIndex() - 8)));

        return buf;
    }

    @Override
    protected Object encodeCommand(Command command) {

        if (command.getType().equals(Command.TYPE_CUSTOM)) {
            String data = command.getString(Command.KEY_DATA);
            if (data.matches("(\\p{XDigit}{2})+")) {
                return encodeContent(DataConverter.parseHex(data));
            } else {
                return encodeContent((data + "\r\n").getBytes(StandardCharsets.US_ASCII));
            }
        } else {
            return null;
        }
    }
    
}
