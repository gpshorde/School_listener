/*
 * Copyright 2015 - 2020 Anton Tananaev (anton@traccar.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.database;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import java.io.File;
import java.io.IOException;
import org.traccar.BasePipelineFactory;
import org.traccar.Protocol;
import org.traccar.model.Command;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import org.traccar.NetworkMessage;
import org.traccar.helper.Checksum;
import org.traccar.protocol.Gt06ProtocolEncoder;
import org.traccar.protocol.TTteltonika09ProtocolEncoder;

public class ActiveDevice {

//    private final long deviceId;
//    private final Protocol protocol;
//    private final Channel channel;
//    private final SocketAddress remoteAddress;
//    private final boolean supportsLiveCommands;
//
//    public ActiveDevice(long deviceId, Protocol protocol, Channel channel, SocketAddress remoteAddress) {
//        this.deviceId = deviceId;
//        this.protocol = protocol;
//        this.channel = channel;
//        this.remoteAddress = remoteAddress;
//        supportsLiveCommands = BasePipelineFactory.getHandler(channel.pipeline(), HttpRequestDecoder.class) == null;
//    }
//
//    public Channel getChannel() {
//        return channel;
//    }
//
//    public long getDeviceId() {
//        return deviceId;
//    }
//
//    public boolean supportsLiveCommands() {
//        return supportsLiveCommands;
//    }
//
//    public void sendCommand(Command command) {
//        protocol.sendDataCommand(channel, remoteAddress, command);
//    }
// private final long deviceId;
//    private final Protocol protocol;
//    private final Channel channel;
//  private final String uniqueId;
//    private final SocketAddress remoteAddress;
//    public static final int SET_REQ = 0xAA04;
//
//    public ActiveDevice(long deviceId, Protocol protocol, Channel channel, SocketAddress remoteAddress, String uniqueId) {
//        this.deviceId = deviceId;
//        this.protocol = protocol;
//        this.channel = channel;
//        this.remoteAddress = remoteAddress;
//          this.uniqueId = uniqueId;
//       
//     
//    }
//
//    ActiveDevice(long deviceId, Protocol protocol, Channel channel, SocketAddress remoteAddress) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    
//
//    public Channel getChannel() {
//        return channel;
//    }
//
//    public long getDeviceId() {
//        return deviceId;
//    }
//
//    public void sendCommand(Command command) {
//        protocol.sendDataCommand(this, command);
//    }
//
//  public void write(Object message, String type, HttpServletResponse resp, HttpServletRequest req) throws IOException {
//      
//      
//      System.out.println("type---------->"+type);
//       System.out.println("message---------->"+message.toString());
//        System.out.println("req---------->"+req.toString());
//      
//        if (type.equals("1")) { //esky
//
//            Integer value = 0;
//            if (req.getParameterMap().containsKey("value")) {
//                value = Integer.parseInt(req.getParameter("value"));
//            }
//            switch (message.toString()) {
//                case "Interval":
//                    channel.writeAndFlush(new NetworkMessage(ByteBufUtil.hexDump(String.format("INTERVAL,000000,%S#", value).getBytes()), remoteAddress));
//                    break;
//                case "Location":
//                    resp.getWriter().write("Device is not supported.");
//                    break;
//                case "Reboot":
//                    channel.writeAndFlush(new NetworkMessage(ByteBufUtil.hexDump(String.format("Reboot,0#").getBytes()), remoteAddress));
//                    break;
//                case "Speed":
//                    if (value == 0) {
//                        value = 30;
//                    }
//                    channel.writeAndFlush(new NetworkMessage(ByteBufUtil.hexDump(String.format("Speedalarm,000000,%S#", value).getBytes()), remoteAddress));
//                    break;
//
//                default:
//                    break;
//            }
//
//            //getChannel().write(ChannelBuffers.wrappedBuffer(message.toString().getBytes()),remoteAddress);
//        } else if (type.equals("2")) {
//            Gt06ProtocolEncoder g = new Gt06ProtocolEncoder();
////            System.out.println("type ----2---"+message.toString());
//            
//            switch (message.toString()) {
//                case "engine_on":
//                    channel.writeAndFlush(new NetworkMessage(g.encodeContent("HFYD,6666#"), remoteAddress));
//                    break;
//                case "engine_off":
//                    channel.writeAndFlush(new NetworkMessage(g.encodeContent("DYD,6666#"), remoteAddress));
//                    break;
//                 default:
//                    break;
//            }
////            getChannel().write(g.encodeContent(message.toString()), remoteAddress);
//        }else if (type.equals("3")) {//gps watch
//               
//            channel.writeAndFlush(new NetworkMessage(String.format("[%s*%s*%04x*%s]", "3G", uniqueId ,message.toString().length(), message.toString()), remoteAddress));
//       
//          byte[] array = Files.readAllBytes(new File("/home/vishal/Desktop/sound/6005566744/20170618103636m.amr").toPath());
//
//          //  String data = "TK," + File2Hex.bytesToHex(array).replace("7d", "7d01")
//                  //  .replace("5b", "7d02")
//                   // .replace("5d", "7d03")
//                   // .replace("2c", "7d04")
//                   // .replace("2a", "7d05");
//
//            String d2 = "TK,";
//
//        //    String data_only = File2Hex.getHexOfAudio(new File("/home/vishal/Desktop/sound/6005566744/20170618103636m.amr")).toString().replace("0x7D,0x1", "0x7d")
//                  //  .replace("0x7D,0x2", "0x5B")
//                  //  .replace("0x7D,0x3", "0x5D")
//                   // .replace("0x7D,0x4", "0x2C")
//                   // .replace("0x7D,0x5", "0x2A");
//        
//        }else if (type.equals("5")) {//360A
//            channel.writeAndFlush(new NetworkMessage(message.toString(), remoteAddress));
//        }else if(type.equals("6")) { // for AT OR Huabao 
//            ByteBuf id = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                 
//            ByteBuf st = Unpooled.buffer();
//            st.writeByte(0x01);
//            st.writeByte(0x00);
//            st.writeByte(0x00);
//            st.writeByte(0x00);
//            st.writeByte(0x55);
//            st.writeByte(0x04);
//            st.writeByte(0x00);
//            st.writeByte(0x00);
//            st.writeShort(0x14);
//            
//            //  PrintOut.PrintChannelBuffer("........ inside huabao.........",st);
//          //  channel.writeAndFlush(new NetworkMessage(HuabaoProtocolDecoder.formatMessage(0x8103, id, st), remoteAddress));
//                
//        }else if (type.equals("9")) {
//            
//            ByteBuf st = Unpooled.buffer();
//              st.writeByte(0x02);
//            st.writeByte(0x00);
//            st.writeByte(0x01);
////            st.writeByte(0x20);
//             st.writeByte(0x28);
//            st.writeByte(0x01);
//           //   ByteBuf buf1 = CastelProtocolEncoder.encodeContent(deviceId, st);
//             channel.writeAndFlush(new NetworkMessage(null, remoteAddress));
//        }else if (type.equals("7")) { //EV07
//            Integer value = 0;
//            if (req.getParameterMap().containsKey("value")) {
//                value = Integer.parseInt(req.getParameter("value"));
//            }
//
//            switch (message.toString()) {
//                case "Interval":
//                    channel.write(String.format("%sM,%s", "123456", +value, remoteAddress));
//                    break;
//                case "Location":
//                    channel.write(String.format("%sLOC", "123456", remoteAddress));
//                    break;
//                case "Reboot":
//                    channel.write(String.format("%sT", "123456", remoteAddress));
//                case "custom":
//                    channel.write(String.format("%st", "g23", remoteAddress));
//                    break;
//                case "Speed":
//                    if (value == 0) {
//                        value = 30;
//                    }
//                    channel.write(String.format("%sJ1,%s", "123456", +value, remoteAddress));
//                    break;
//                default:
//                    break;
//            }
//            channel.write(String.format("%sLOC", "123456", remoteAddress));
//            //"{%s}LED{%s}", Command.KEY_DEVICE_PASSWORD 
//        } else if (type.equals("11")) {
//              switch (message.toString()) {
//              case "capture":
//                ByteBuf st = Unpooled.buffer();
//                st.writeByte(0x7e);
//                st.writeShort(0x8801);
//                st.writeShort(0x000C);
//                ByteBuf id = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                  st.writeBytes(id);      // imei
//                    st.writeByte(0x00);     // channelId
//                    st.writeShort(0x0001);  //Taken command
//                    st.writeShort(0x0001);  //Taken interval/recording
//                    st.writeShort(0x0000); // Saving sign
//                    st.writeShort(0x08);  //Resolution
//                    st.writeByte(0x0a);     // Picture quality
//                    st.writeByte(0x00);     // Brightness
//                    st.writeByte(0x00);     //Contrast
//                    st.writeByte(0x00);     // Saturation
//                    st.writeByte(0x00);      //Chroma
//                    st.writeByte(Checksum.xor(st.nioBuffer(1, st.readableBytes() - 1)));
//                     st.writeByte(0x7e);
//                    channel.writeAndFlush(new NetworkMessage(st, remoteAddress));
//                    break;
//            case "oli_cut":
//                  ByteBuf oli_cut = Unpooled.buffer();
//                  
//                  channel.writeAndFlush(new NetworkMessage(Unpooled.wrappedBuffer(String.format("<EXTD80076011>").getBytes()), remoteAddress));
//                 break;
//            case "engine_on":
//               
//             ByteBuf steon = Unpooled.buffer();
//                steon.writeByte(0x7e);
//                steon.writeShort(0x8105);
//                steon.writeShort(0x0001);
//                ByteBuf idon11 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                steon.writeBytes(idon11);      
//                steon.writeShort(0x0000);   
//                steon.writeByte(0x65);  
//                steon.writeByte(Checksum.xor(steon.nioBuffer(1, steon.readableBytes() - 1)));
//                steon.writeByte(0x7e);
//                channel.writeAndFlush(new NetworkMessage(steon, remoteAddress));
//                   break;
//
//    case "engine_on1":
//               
//             ByteBuf steon1 = Unpooled.buffer();
//                steon1.writeByte(0x7e);
//                steon1.writeShort(0x8105);
//                steon1.writeShort(0x0001);
//                ByteBuf idon111 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                steon1.writeBytes(idon111);      
//                steon1.writeByte(0x65);  
//                steon1.writeByte(Checksum.xor(steon1.nioBuffer(1, steon1.readableBytes() - 1)));
//                steon1.writeByte(0x7e);
//                channel.writeAndFlush(new NetworkMessage(steon1, remoteAddress));
//                   break;
//
//            case "engine_off":
//
//            ByteBuf steoff = Unpooled.buffer();
//                steoff.writeByte(0x7e);
//                steoff.writeShort(0x8105);
//                steoff.writeShort(0x0001);
//                ByteBuf idoff11 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                steoff.writeBytes(idoff11);      
//                steoff.writeShort(0x0000);   
//                steoff.writeByte(0x64);  
//                steoff.writeByte(Checksum.xor(steoff.nioBuffer(1, steoff.readableBytes() - 1)));
//                steoff.writeByte(0x7e);
//                channel.writeAndFlush(new NetworkMessage(steoff, remoteAddress));    
//                break;
//
//            case "engine_off1":
//
//        System.out.println("engine 0ff------1");
//
//            ByteBuf steoff1 = Unpooled.buffer();
//                steoff1.writeByte(0x7e);
//                steoff1.writeShort(0x8105);
//                steoff1.writeShort(0x0001);
//                ByteBuf idoff111 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                steoff1.writeBytes(idoff111);      
//                steoff1.writeByte(0x64);  
//                steoff1.writeByte(Checksum.xor(steoff1.nioBuffer(1, steoff1.readableBytes() - 1)));
//                steoff1.writeByte(0x7e);
//                channel.writeAndFlush(new NetworkMessage(steoff1, remoteAddress));
//                break;
//
//            case "Speed":     
//                 
//            Integer value1 = 0;
//            if (req.getParameterMap().containsKey("value")) {
//                value1 = Integer.parseInt(req.getParameter("value"));
//            }
//            
//             ByteBuf st1 = Unpooled.buffer();
//                st1.writeByte(0x7e);
//                st1.writeShort(0x8103);
//                st1.writeShort(0x000A);
//                ByteBuf id1 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                st1.writeBytes(id1);      
//                st1.writeShort(0x002A);   
//                st1.writeByte(0x01);  
//                st1.writeInt(0x00000055);  
//                st1.writeByte(0x04); 
//                st1.writeInt(value1); 
//                st1.writeByte(Checksum.xor(st1.nioBuffer(1, st1.readableBytes() - 1)));
//                st1.writeByte(0x7e);
//                channel.writeAndFlush(new NetworkMessage(st1, remoteAddress));
//                
//             ByteBuf st2 = Unpooled.buffer();
//                st2.writeByte(0x7e);
//                st2.writeShort(0x8103);
//                st2.writeShort(0x000A);
//                ByteBuf id2 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                st2.writeBytes(id2);      
//                st2.writeShort(0x002A);   
//                st2.writeByte(0x01);  
//                st2.writeInt(0x00000056);  
//                st2.writeByte(0x04); 
//                st2.writeInt(0x0000000a); 
//                st2.writeByte(Checksum.xor(st2.nioBuffer(1, st2.readableBytes() - 1)));
//                st2.writeByte(0x7e);
//                channel.writeAndFlush(new NetworkMessage(st2, remoteAddress));
//                break;
//                
//            case "accon":     
//                 
//            Integer valueon = 0;
//            if (req.getParameterMap().containsKey("value")) {
//                valueon = Integer.parseInt(req.getParameter("value"));
//            }
//            
//             ByteBuf ston = Unpooled.buffer();
//                ston.writeByte(0x7e);
//                ston.writeShort(0x8103);
//                ston.writeShort(0x000A);
//                ByteBuf idon = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                ston.writeBytes(idon);      
//                ston.writeShort(0x002A);   
//                ston.writeByte(0x01);  
//                ston.writeInt(0x00000029);  
//                ston.writeByte(0x04); 
//                ston.writeInt(valueon); 
//                ston.writeByte(Checksum.xor(ston.nioBuffer(1, ston.readableBytes() - 1)));
//                ston.writeByte(0x7e);
//                channel.writeAndFlush(new NetworkMessage(ston, remoteAddress));
//                  break;
//            case "accoff":     
//                 
//            Integer valueoff = 0;
//            if (req.getParameterMap().containsKey("value")) {
//                valueoff = Integer.parseInt(req.getParameter("value"));
//            }
//            
//             ByteBuf stoff = Unpooled.buffer();
//                stoff.writeByte(0x7e);
//                stoff.writeShort(0x8103);
//                stoff.writeShort(0x000A);
//                ByteBuf idoff = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                stoff.writeBytes(idoff);      
//                stoff.writeShort(0x002A);   
//                stoff.writeByte(0x01);  
//                stoff.writeInt(0x00000027);  
//                stoff.writeByte(0x04); 
//                stoff.writeInt(valueoff); 
//                stoff.writeByte(Checksum.xor(stoff.nioBuffer(1, stoff.readableBytes() - 1)));
//                stoff.writeByte(0x7e);
//                channel.writeAndFlush(new NetworkMessage(stoff, remoteAddress));
//                  break;
//                  
//                 case "backupIP":     
//                              
//                       
//             ByteBuf stob = Unpooled.buffer();
//                stob.writeByte(0x7e);
//                stob.writeShort(0x8103);
//                stob.writeShort(0x0013);
//                ByteBuf idob = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                stob.writeBytes(idob);      
//                stob.writeShort(0x002A);   
//                stob.writeByte(0x01);  
//                stob.writeInt(0x00000017);  
//                stob.writeByte(0x0D); 
//                stob.writeInt(0x3136372E);
//                stob.writeInt(0x38362E31); 
//                stob.writeInt(0x32342E38);
//                stob.writeByte(0x38); 
//
//                stob.writeByte(Checksum.xor(stob.nioBuffer(1, stob.readableBytes() - 1)));
//                stob.writeByte(0x7e);
//                channel.writeAndFlush(new NetworkMessage(stob, remoteAddress));
//                  break;
//                  
//            case "data":     
//                              
//                 
//               ByteBuf stnew = Unpooled.buffer();
//                stnew.writeByte(0x7e);
//                stnew.writeShort(0x8104);
//                stnew.writeShort(0x0000);
//                ByteBuf idnew = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                stnew.writeBytes(idnew);      
//                stnew.writeShort(0x002A);   
//                stnew.writeByte(Checksum.xor(stnew.nioBuffer(1, stnew.readableBytes() - 1)));
//                stnew.writeByte(0x7e);
//                channel.writeAndFlush(new NetworkMessage(stnew, remoteAddress));
//            
//                   break;
//          case "Video":
//                
//                 Integer value = 0;
//            if (req.getParameterMap().containsKey("value")) {
//                value = Integer.parseInt(req.getParameter("value"));
//            }
//                  String ip = null;
//            if (req.getParameterMap().containsKey("ip")) {
//                ip = String.valueOf(req.getParameter("ip"));
//            }
//            
//                Integer logicalValue = 0;
//            if (req.getParameterMap().containsKey("logicalValue")) {
//                logicalValue = Integer.parseInt(req.getParameter("logicalValue"));
//            }    Integer typeValue = 0;
//            if (req.getParameterMap().containsKey("typeValue")) {
//                typeValue = Integer.parseInt(req.getParameter("typeValue"));
//            }    Integer StreamTpe = 0;
//            if (req.getParameterMap().containsKey("StreamTpe")) {
//                StreamTpe = Integer.parseInt(req.getParameter("StreamTpe"));
//            }
////            Timestamp  start_time= null;
////             String  starttime= null;
////            if (req.getParameterMap().containsKey("start_time")) {
////                start_time = Timestamp.valueOf(req.getParameter("start_time"));
////                starttime = start_time.toString();
////            }
////                        Timestamp  end_time= null;
////                        String endtime= null;
////
////            if (req.getParameterMap().containsKey("end_time")) {
////                end_time = Timestamp.valueOf(req.getParameter("end_time"));
////                                endtime = end_time.toString();
////
////            }
//                 if(ip.equals("202.131.106.55"))
//               {
//                 // 3230322e 3133312 e3130362e 3535
////                      System.out.println("ip---------->"+ip);
//             
//                  ByteBuf st12 = Unpooled.buffer();
//                st12.writeByte(0x7e);
//                st12.writeShort(0x9101);
//                st12.writeShort(0x0016);
//                ByteBuf id12 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                st12.writeBytes(id12);      
//                st12.writeShort(0x0001);   
//                st12.writeByte(0x0e); 
//                st12.writeInt(0x3230322e);
//                st12.writeInt(0x3133312e);
//                st12.writeInt(0x3130362e);
//                st12.writeShort(0x3535);
//                st12.writeShort(value);  
//                st12.writeShort(0x0000); 
//                st12.writeByte(logicalValue); 
//                st12.writeByte(typeValue); 
//                st12.writeByte(StreamTpe); 
//                st12.writeByte(Checksum.xor(st12.nioBuffer(1, st12.readableBytes() - 1)));
//                st12.writeByte(0x7e);
//                 channel.writeAndFlush(new NetworkMessage(st12, remoteAddress));
//               }
//                 else if(ip.equals("213.136.73.98"))
//               {
//              
//              // 3139322e 3136382e 312e3832
////             
//                ByteBuf st13 = Unpooled.buffer();
//                st13.writeByte(0x7e);
//                st13.writeShort(0x9101);
//                st13.writeShort(0x0015);
//                ByteBuf id13 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                st13.writeBytes(id13);      
//                st13.writeShort(0x0001);   
//                st13.writeByte(0x0d); 
//                st13.writeInt(0x3231332e);
//                st13.writeInt(0x3133362e);
//                st13.writeInt(0x37332e39);
//                st13.writeByte(0x38);
//                st13.writeShort(value);  
//                st13.writeShort(0x0000); 
//                st13.writeByte(logicalValue); 
//                st13.writeByte(typeValue); 
//                st13.writeByte(StreamTpe); 
//                st13.writeByte(Checksum.xor(st13.nioBuffer(1, st13.readableBytes() - 1)));
//                st13.writeByte(0x7e);
//                 channel.writeAndFlush(new NetworkMessage(st13, remoteAddress));
//               }   else if(ip.equals("124.123.122.85"))
//               {
////              
//                 
//             
//                 
//                ByteBuf st13 = Unpooled.buffer();
//                st13.writeByte(0x7e);
//                st13.writeShort(0x9101);
//                st13.writeShort(0x0016);
//                ByteBuf id13 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                st13.writeBytes(id13);      
//                st13.writeShort(0x0001);   
//                 st13.writeByte(0x0e);     // IP Lenght 124.123.122.85
//                st13.writeInt(0x3132342e); // IP 
//                st13.writeInt(0x3132332e);  // IP
//                st13.writeInt(0x3132322e);  //IP
//                st13.writeShort(0x3835);   // IP
//                st13.writeShort(value);  
//                st13.writeShort(0x0000); 
//                st13.writeByte(logicalValue); 
//                st13.writeByte(typeValue); 
//                st13.writeByte(StreamTpe); 
//                st13.writeByte(Checksum.xor(st13.nioBuffer(1, st13.readableBytes() - 1)));
//                st13.writeByte(0x7e);
//                 channel.writeAndFlush(new NetworkMessage(st13, remoteAddress));
//               }
//              else if(ip.equals("192.168.1.82"))
//               {
//                   
////                   System.out.println("192.168.1.82");
//              
//              // 3139322e 3136382e 312e3832
//                                
//                  ByteBuf st13 = Unpooled.buffer();
//                st13.writeByte(0x7e);
//                st13.writeShort(0x9101);
//                st13.writeShort(0x0014);
//                ByteBuf id13 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                st13.writeBytes(id13);      
//                st13.writeShort(0x0001);   
//                st13.writeByte(0x0c); 
////                ByteBuf ip1 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(ip));
////                   st13.writeBytes(ip1);  
//                st13.writeInt(0x3139322e);
//                st13.writeInt(0x3136382e);
//                st13.writeInt(0x312e3832);
//                st13.writeShort(value);  
//                st13.writeShort(0x0000); 
//                st13.writeByte(logicalValue); 
//                st13.writeByte(typeValue); 
//                st13.writeByte(StreamTpe); 
//                st13.writeByte(Checksum.xor(st13.nioBuffer(1, st13.readableBytes() - 1)));
//                st13.writeByte(0x7e);
//                 channel.writeAndFlush(new NetworkMessage(st13, remoteAddress));
//               }
//              else
//               {
//                
//                   // 167.86.124.88
//                               
//                ByteBuf st14 = Unpooled.buffer();
//                st14.writeByte(0x7e);
//                st14.writeShort(0x9101);
//                st14.writeShort(0x0015);
//                ByteBuf id14 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                st14.writeBytes(id14);      
//                st14.writeShort(0x0001);   
//                st14.writeByte(0x0d); 
//                st14.writeInt(0x3136372E);
//                st14.writeInt(0x38362E31); 
//                st14.writeInt(0x32342E38);
//                st14.writeByte(0x38); 
//                st14.writeShort(value);  
//                st14.writeShort(0x0000); 
//                st14.writeByte(logicalValue); 
//                st14.writeByte(typeValue); 
//                st14.writeByte(StreamTpe); 
//                st14.writeByte(Checksum.xor(st14.nioBuffer(1, st14.readableBytes() - 1)));
//                st14.writeByte(0x7e);
//                   channel.writeAndFlush(new NetworkMessage(st14, remoteAddress));
//                       }
//             break;
//        case "playback":
//                
//            Integer value2 = 0;
//            if (req.getParameterMap().containsKey("value")) {
//                value2 = Integer.parseInt(req.getParameter("value"));
//            }
//                  String ip1 = null;
//            if (req.getParameterMap().containsKey("ip")) {
//                ip1 = String.valueOf(req.getParameter("ip"));
//            }
//            
//                Integer logicalValue1 = 0;
//            if (req.getParameterMap().containsKey("logicalValue")) {
//                logicalValue1 = Integer.parseInt(req.getParameter("logicalValue"));
//            }    Integer typeValue1 = 0;
//            if (req.getParameterMap().containsKey("typeValue")) {
//                typeValue1 = Integer.parseInt(req.getParameter("typeValue"));
//            }    Integer StreamTpe1 = 0;
//            if (req.getParameterMap().containsKey("StreamTpe")) {
//                StreamTpe1 = Integer.parseInt(req.getParameter("StreamTpe"));
//            }
//             String  starttime= null;
//            if (req.getParameterMap().containsKey("start_time")) {
//                starttime = String.valueOf(req.getParameter("start_time"));
//             }
//                       String endtime= null;
//
//            if (req.getParameterMap().containsKey("end_time")) {
//                endtime = String.valueOf(req.getParameter("end_time"));
//                            
//
//            }
//                if(ip1.equals("213.136.73.98"))
//               {
//                   if(starttime == null)
//                   {
//                     ByteBuf st13 = Unpooled.buffer();
//                st13.writeByte(0x7e); // Header
//                st13.writeShort(0x9201); // MessageID
//                st13.writeShort(0x0018);  // Message Length
//                ByteBuf id13 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                st13.writeBytes(id13);      // DeviceID
//                st13.writeShort(0x0001);   // Messae Type
//               st13.writeByte(0x0d); 
//                st13.writeInt(0x3231332e);
//                st13.writeInt(0x3133362e);
//                st13.writeInt(0x37332e39);
//                st13.writeByte(0x38);
//                st13.writeShort(value2);   // TCP PORT
//                st13.writeShort(0x0000);    // UDP PORT
//                st13.writeByte(logicalValue1); // Channel ID
//                st13.writeByte(0x00);   // Video - audio type
//                st13.writeByte(0x00); //  MAin Stream / Sub Stream
//               
//                st13.writeByte(0x01);    // Memory Type
//                st13.writeByte(0x00);    // Playback
//                st13.writeByte(0x01);     // Fast Forward
//                st13.writeByte(Checksum.xor(st13.nioBuffer(1, st13.readableBytes() - 1)));
//                st13.writeByte(0x7e);
//                channel.writeAndFlush(new NetworkMessage(st13, remoteAddress));
//                   }else{
//                        ByteBuf st13 = Unpooled.buffer();
//                st13.writeByte(0x7e); // Header
//                st13.writeShort(0x9201); // MessageID
//                st13.writeShort(0x0024);  // Message Length
//                ByteBuf id13 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                st13.writeBytes(id13);      // DeviceID
//                st13.writeShort(0x0001);   // Messae Type
//               st13.writeByte(0x0d); 
//                st13.writeInt(0x3231332e);
//                st13.writeInt(0x3133362e);
//                st13.writeInt(0x37332e39);
//                st13.writeByte(0x38);
//                st13.writeShort(value2);   // TCP PORT
//                st13.writeShort(0x0000);    // UDP PORT
//                st13.writeByte(logicalValue1); // Channel ID
//                st13.writeByte(0x00);   // Video - audio type
//                st13.writeByte(0x00); //  MAin Stream / Sub Stream
//               
//                st13.writeByte(0x01);    // Memory Type
//                st13.writeByte(0x00);    // Playback
//                st13.writeByte(0x01);     // Fast Forward
//                   ByteBuf starttime1 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(starttime));
//                st13.writeBytes(starttime1); 
//                ByteBuf endtime1 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(endtime));
//                st13.writeBytes(endtime1); 
//                st13.writeByte(Checksum.xor(st13.nioBuffer(1, st13.readableBytes() - 1)));
//                st13.writeByte(0x7e);
//                channel.writeAndFlush(new NetworkMessage(st13, remoteAddress));
//                   }
//                
//               }   else if(ip1.equals("124.123.122.85"))
//               {
//                   if(starttime == null)
//                   {
//               /////0x9201
//                ByteBuf st13 = Unpooled.buffer();
//                st13.writeByte(0x7e); // Header
//                st13.writeShort(0x9201); // MessageID
//               //// USE this lenght when  not passing date time
//                st13.writeShort(0x0019);  // Message Length
//                 ///// USE this lenght when passing date time
////                st13.writeShort(0x0025);  // Message Length
//                ByteBuf id13 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                st13.writeBytes(id13);      // DeviceID
//                st13.writeShort(0x0001);   // Messae Type
//                st13.writeByte(0x0e);     // IP Lenght 124.123.122.85
//                st13.writeInt(0x3132342e); // IP 
//                st13.writeInt(0x3132332e);  // IP
//                st13.writeInt(0x3132322e);  //IP
//                st13.writeShort(0x3835);   // IP
//                st13.writeShort(value2);   // TCP PORT
//                st13.writeShort(0x0000);    // UDP PORT
//                st13.writeByte(logicalValue1); // Channel ID
//                st13.writeByte(0x00);   // Video - audio type
//                st13.writeByte(0x00); //  MAin Stream / Sub Stream
//                st13.writeByte(0x01);    // Memory Type
//                st13.writeByte(0x00);    // Playback
//                st13.writeByte(0x01);     // Fast Forward
//              st13.writeByte(Checksum.xor(st13.nioBuffer(1, st13.readableBytes() - 1)));
//                st13.writeByte(0x7e);
//                channel.writeAndFlush(new NetworkMessage(st13, remoteAddress));
//                   }else{
//                         /////0x9201
//                         
////                         System.out.println("ip----"+ip1);
//                ByteBuf st13 = Unpooled.buffer();
//                st13.writeByte(0x7e); // Header
//                st13.writeShort(0x9201); // MessageID
//               //// USE this lenght when  not passing date time
//                st13.writeShort(0x0025);  // Message Length
//                 ///// USE this lenght when passing date time
////                st13.writeShort(0x0025);  // Message Length
//                ByteBuf id13 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                st13.writeBytes(id13);      // DeviceID
//                st13.writeShort(0x0001);   // Messae Type
//                st13.writeByte(0x0e);     // IP Lenght 124.123.122.85
//                st13.writeInt(0x3132342e); // IP 
//                st13.writeInt(0x3132332e);  // IP
//                st13.writeInt(0x3132322e);  //IP
//                st13.writeShort(0x3835);   // IP
//                st13.writeShort(value2);   // TCP PORT
//                st13.writeShort(0x0000);    // UDP PORT
//                st13.writeByte(logicalValue1); // Channel ID
//                st13.writeByte(0x00);   // Video - audio type
//                st13.writeByte(0x00); //  MAin Stream / Sub Stream
//                st13.writeByte(0x01);    // Memory Type
//                st13.writeByte(0x00);    // Playback
//                st13.writeByte(0x01);     // Fast Forward
//                 ByteBuf start = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(starttime));
//                st13.writeBytes(start); 
//                 ByteBuf end = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(endtime));
//                st13.writeBytes(end); 
//                st13.writeByte(Checksum.xor(st13.nioBuffer(1, st13.readableBytes() - 1)));
//                st13.writeByte(0x7e);
//                channel.writeAndFlush(new NetworkMessage(st13, remoteAddress));
//                 
//                   }
//              }
//             break;   
//            
//            case "playback1":
//         
//                Integer logicalValue2 = 0;
//            if (req.getParameterMap().containsKey("logicalValue")) {
//                logicalValue2 = Integer.parseInt(req.getParameter("logicalValue"));
//            }  
//             String  starttime2= null;
//            if (req.getParameterMap().containsKey("start_time")) {
//                starttime2 = String.valueOf(req.getParameter("start_time"));
//             }
//                       String endtime2= null;
//
//            if (req.getParameterMap().containsKey("end_time")) {
//                endtime2 = String.valueOf(req.getParameter("end_time"));
//                            
//
//            }
//             
//                ByteBuf st13 = Unpooled.buffer();
//                st13.writeByte(0x7e); // Header
//                st13.writeShort(0x9205); // MessageID
//                st13.writeShort(0x0018);  // Message Length
//                ByteBuf id13 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
//                st13.writeBytes(id13);      // DeviceID
//               st13.writeShort(0x0001);
//                st13.writeByte(logicalValue2); // Channel ID
//                 // Date Time Passing in command
//                ByteBuf starttime1 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(starttime2));
//                st13.writeBytes(starttime1); 
//                ByteBuf endtime1 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(endtime2));
//                st13.writeBytes(endtime1); 
//
//                st13.writeInt(0x00000000); // alaram
//                st13.writeInt(0x00000000);
//                
//                st13.writeByte(0x00);    //audio and video,
//                st13.writeByte(0x01);    // stream,
//                st13.writeByte(0x00);     //Memory type
//                
//                // Check Sum and End FLag
//                st13.writeByte(Checksum.xor(st13.nioBuffer(1, st13.readableBytes() - 1)));
//                st13.writeByte(0x7e);
//                channel.writeAndFlush(new NetworkMessage(st13, remoteAddress));
//
//             default:
//           }
//        
//        }else {
//            resp.getWriter().write("may be type is not valid, it should be 2:GT06");
//            return;
//        }
//
//    }
    private final long deviceId;
    private final Protocol protocol;
    private final Channel channel;
    private final String uniqueId;
    private final SocketAddress remoteAddress;
    public static final int SET_REQ = 0xAA04;

    public ActiveDevice(long deviceId, Protocol protocol, Channel channel, SocketAddress remoteAddress, String uniqueId) {
        this.deviceId = deviceId;
        this.protocol = protocol;
        this.channel = channel;
        this.remoteAddress = remoteAddress;
        this.uniqueId = uniqueId;

    }

    public Channel getChannel() {
        return channel;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void sendCommand(Command command) {
        protocol.sendDataCommand(this, command);
    }

    public void write(Object message, String type, HttpServletResponse resp, HttpServletRequest req) throws IOException {
        //This is for the Esky protocol command 
    
      System.out.println("type---------->"+type);
       System.out.println("message---------->"+message.toString());
        System.out.println("req---------->"+req.toString());
              
        
        if (type.equals("1")) {
            String value = null;
            if (req.getParameterMap().containsKey("value")) {
                value = req.getParameter("value");
            }
            switch (message.toString()) {
                case "Interval": // INTERVAL,000000,%S#
                    channel.writeAndFlush(new NetworkMessage(ByteBufUtil.hexDump(String.format(value).getBytes()), remoteAddress));
                    break;
                case "Location":
                    resp.getWriter().write("Device is not supported.");
                    break;
                case "Reboot": //Reboot,0#
                    channel.writeAndFlush(new NetworkMessage(ByteBufUtil.hexDump(String.format(value).getBytes()), remoteAddress));
                    break;
                case "Speed": // Speedalarm,000000,%S#
                    channel.writeAndFlush(new NetworkMessage(ByteBufUtil.hexDump(String.format(value).getBytes()), remoteAddress));
                    break;
                    
                case "Custom": // any command
                    channel.writeAndFlush(new NetworkMessage(ByteBufUtil.hexDump(String.format(value).getBytes()), remoteAddress));
                    break;
                default:
                    break;
            }
            //this is for the teltonika command for type 10
        } else if (type.equals("10")) {

            TTteltonika09ProtocolEncoder g = new TTteltonika09ProtocolEncoder(); //setdigout 0 //setdigout 1
            String value1 = null;
            if (req.getParameterMap().containsKey("value")) {
                value1 = req.getParameter("value");
            }
            System.out.println("value1");
            switch (message.toString()) {
                case "Custom": // in this we pass the any custom command
                    channel.writeAndFlush(new NetworkMessage(g.encodeContent((value1 + "\r\n").getBytes(StandardCharsets.US_ASCII)), remoteAddress));
                    break;
                case "engine_on": 
                    channel.writeAndFlush(new NetworkMessage(g.encodeContent(("setdigout 1" + "\r\n").getBytes(StandardCharsets.US_ASCII)), remoteAddress));
                    break;
                case "engine_off":
                    channel.writeAndFlush(new NetworkMessage(g.encodeContent(("setdigout 0" + "\r\n").getBytes(StandardCharsets.US_ASCII)), remoteAddress));
                    break;
                default:
                    break;
            }
        } else if (type.equals("58") || type.equals("39") || type.equals("2")) {
            Gt06ProtocolEncoder g = new Gt06ProtocolEncoder();
            String value1 = null;

            //Relay,0#            engine off 
            //Relay,1#            engine on
            //SPEED,ON,2,20,0#    SPEED
            if (req.getParameterMap().containsKey("value")) {
                value1 = req.getParameter("value");
            }
            switch (message.toString()) {
                case "Custom":
                    channel.writeAndFlush(new NetworkMessage(g.encodeContent(value1), remoteAddress));
                    break;
                case "engine_on":
                    channel.writeAndFlush(new NetworkMessage(g.encodeContent("Relay,0#"), remoteAddress));
                    break;
                case "engine_off":
                    channel.writeAndFlush(new NetworkMessage(g.encodeContent("Relay,1#"), remoteAddress));
                    break;
                default:
                    break;
            }
        
        } else if (type.equals("11")) {
            String value1 = null;
                    if (req.getParameterMap().containsKey("value")) {
                        value1 = String.valueOf(req.getParameter("value"));
                    }
            System.out.println("message.toString()===============>" +message.toString());
                        System.out.println("value1===============>"+value1);

            switch (message.toString()) {

                // in custom command we have a pass command like this  #000000,STOC:CUT ,  #000000,STOC:OK ,  #000000,STDL:1  ,#000000,STDL:0 ,*SPJLX*P:753869*C:# 
                case "Custom":
                    ByteBuf cutoff1 = Unpooled.buffer();
                    cutoff1.writeByte(0x7e);
                    cutoff1.writeShort(0x8300);// COMMAND
                    cutoff1.writeShort(0x0014);// MESSAGE length
                    ByteBuf idon11a = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));// device_id
                    cutoff1.writeBytes(idon11a);
                                        cutoff1.writeShort(0x008E);
  //Platform serial number   
                    cutoff1.writeByte(0x01);  //Flag position: Emergency
                    ByteBuf idoff11w = Unpooled.wrappedBuffer(String.format(value1).getBytes()); // command in hex format.
                    cutoff1.writeBytes(idoff11w);
                    cutoff1.writeByte(Checksum.xor(cutoff1.nioBuffer(1, cutoff1.readableBytes() - 1))); //CRC
                    cutoff1.writeByte(0x7e);
                    channel.writeAndFlush(new NetworkMessage(cutoff1, remoteAddress));
                    break;

                case "engine_on":

                    System.out.println("engine_on another case");
                    ByteBuf steon = Unpooled.buffer();
                    steon.writeByte(0x7e);
                    steon.writeShort(0x8105);
                    steon.writeShort(0x0001);
                    ByteBuf idon11 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
                    steon.writeBytes(idon11);
                    steon.writeShort(0x0000);
                    steon.writeByte(0x65);
                    steon.writeByte(Checksum.xor(steon.nioBuffer(1, steon.readableBytes() - 1)));
                    steon.writeByte(0x7e);
                    channel.writeAndFlush(new NetworkMessage(steon, remoteAddress));
                    break;

                case "engine_on1":

                    ByteBuf steon1 = Unpooled.buffer();
                    steon1.writeByte(0x7e);
                    steon1.writeShort(0x8105);
                    steon1.writeShort(0x0001);
                    ByteBuf idon111 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
                    steon1.writeBytes(idon111);
                    steon1.writeByte(0x65);
                    steon1.writeByte(Checksum.xor(steon1.nioBuffer(1, steon1.readableBytes() - 1)));
                    steon1.writeByte(0x7e);
                    channel.writeAndFlush(new NetworkMessage(steon1, remoteAddress));
                    break;

                case "engine_off":

                    ByteBuf steoff = Unpooled.buffer();
                    steoff.writeByte(0x7e);
                    steoff.writeShort(0x8105);
                    steoff.writeShort(0x0001);
                    ByteBuf idoff11 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
                    steoff.writeBytes(idoff11);
                    steoff.writeShort(0x0000);
                    steoff.writeByte(0x64);
                    steoff.writeByte(Checksum.xor(steoff.nioBuffer(1, steoff.readableBytes() - 1)));
                    steoff.writeByte(0x7e);
                    channel.writeAndFlush(new NetworkMessage(steoff, remoteAddress));
                    break;

                case "engine_off1":

                    System.out.println("engine 0ff------1");

                    ByteBuf steoff1 = Unpooled.buffer();
                    steoff1.writeByte(0x7e);
                    steoff1.writeShort(0x8105);
                    steoff1.writeShort(0x0001);
                    ByteBuf idoff111 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
                    steoff1.writeBytes(idoff111);
                    steoff1.writeByte(0x64);
                    steoff1.writeByte(Checksum.xor(steoff1.nioBuffer(1, steoff1.readableBytes() - 1)));
                    steoff1.writeByte(0x7e);
                    channel.writeAndFlush(new NetworkMessage(steoff1, remoteAddress));
                    break;

                case "Speed":

                    Integer valuea1 = 0;
                    if (req.getParameterMap().containsKey("value")) {
                        valuea1 = Integer.parseInt(req.getParameter("value"));
                    }

                    ByteBuf st1 = Unpooled.buffer();
                    st1.writeByte(0x7e);
                    st1.writeShort(0x8103);
                    st1.writeShort(0x000A);
                    ByteBuf id1 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
                    st1.writeBytes(id1);
                    st1.writeShort(0x002A);
                    st1.writeByte(0x01);
                    st1.writeInt(0x00000055);
                    st1.writeByte(0x04);
                    st1.writeInt(valuea1);
                    st1.writeByte(Checksum.xor(st1.nioBuffer(1, st1.readableBytes() - 1)));
                    st1.writeByte(0x7e);
                    channel.writeAndFlush(new NetworkMessage(st1, remoteAddress));

                    ByteBuf st2 = Unpooled.buffer();
                    st2.writeByte(0x7e);
                    st2.writeShort(0x8103);
                    st2.writeShort(0x000A);
                    ByteBuf id2 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
                    st2.writeBytes(id2);
                    st2.writeShort(0x002A);
                    st2.writeByte(0x01);
                    st2.writeInt(0x00000056);
                    st2.writeByte(0x04);
                    st2.writeInt(0x0000000a);
                    st2.writeByte(Checksum.xor(st2.nioBuffer(1, st2.readableBytes() - 1)));
                    st2.writeByte(0x7e);
                    channel.writeAndFlush(new NetworkMessage(st2, remoteAddress));
                    break;
                case "capture":
                    ByteBuf st = Unpooled.buffer();
                    st.writeByte(0x7e);
                    st.writeShort(0x8801);
                    st.writeShort(0x000C);
                    ByteBuf id = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
                    st.writeBytes(id);      // imei
                    st.writeByte(0x00);     // channelId
                    st.writeShort(0x0001);  //Taken command
                    st.writeShort(0x0001);  //Taken interval/recording
                    st.writeShort(0x0000); // Saving sign
                    st.writeShort(0x08);  //Resolution
                    st.writeByte(0x0a);     // Picture quality
                    st.writeByte(0x00);     // Brightness
                    st.writeByte(0x00);     //Contrast
                    st.writeByte(0x00);     // Saturation
                    st.writeByte(0x00);      //Chroma
                    st.writeByte(Checksum.xor(st.nioBuffer(1, st.readableBytes() - 1)));
                    st.writeByte(0x7e);
                    channel.writeAndFlush(new NetworkMessage(st, remoteAddress));
                    break;
                case "oli_cut":
                    ByteBuf oli_cut = Unpooled.buffer();
                    channel.writeAndFlush(new NetworkMessage(Unpooled.wrappedBuffer(String.format("<EXTD80076011>").getBytes()), remoteAddress));
                    break;

                case "Text":

                    String value1a = null;
                    if (req.getParameterMap().containsKey("value")) {
                        value1a = req.getParameter("value");
                    }
                    String anotherString = new String(value1a.getBytes("GBK"), "ISO8859_1");
                    char[] chars = anotherString.toCharArray();
                    StringBuffer strBuffer = new StringBuffer();
                    for (int i = 0; i < chars.length; i++) {
                        strBuffer.append(Integer.toHexString((int) chars[i]));
                    }

                    ByteBuf st1aaa = Unpooled.buffer();
                    st1aaa.writeByte(0x7e);
                    st1aaa.writeShort(0x8300);
                    st1aaa.writeShort(0x0002);
                    ByteBuf ida1 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
                    st1aaa.writeBytes(ida1);
                    ByteBuf ida1a = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(strBuffer.toString()));
                    st1aaa.writeBytes(ida1a);
                    st1aaa.writeByte(Checksum.xor(st1aaa.nioBuffer(1, st1aaa.readableBytes() - 1)));
                    st1aaa.writeByte(0x7e);
                    channel.writeAndFlush(new NetworkMessage(st1aaa, remoteAddress));

                    break;
                case "Video":

                    Integer value = 0;
                    if (req.getParameterMap().containsKey("value")) {
                        value = Integer.parseInt(req.getParameter("value"));
                    }

                    String ip = null;
                    if (req.getParameterMap().containsKey("ip")) {
                        ip = String.valueOf(req.getParameter("ip"));
                    }

                    Integer logicalValue = 0;
                    if (req.getParameterMap().containsKey("logicalValue")) {
                        logicalValue = Integer.parseInt(req.getParameter("logicalValue"));
                    }

                    Integer typeValue = 0;
                    if (req.getParameterMap().containsKey("typeValue")) {
                        typeValue = Integer.parseInt(req.getParameter("typeValue"));
                    }

                    Integer StreamTpe = 0;
                    if (req.getParameterMap().containsKey("StreamTpe")) {
                        StreamTpe = Integer.parseInt(req.getParameter("StreamTpe"));
                    }

                    if (ip.equals("213.136.73.98")) {
                        ByteBuf st13 = Unpooled.buffer();
                        st13.writeByte(0x7e);   // start byte
                        st13.writeShort(0x9101);  // type
                        st13.writeShort(0x0015);  // lenght
                        ByteBuf id13 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));  // imei
                        st13.writeBytes(id13);
                        st13.writeShort(0x0001);    // Messae Type
                        st13.writeByte(0x0d);       //server ip lenght
                        st13.writeInt(0x3231332e);
                        st13.writeInt(0x3133362e);
                        st13.writeInt(0x37332e39);
                        st13.writeByte(0x38);          // server ip
                        st13.writeShort(value);       // tcp port
                        st13.writeShort(0x0000);      // udp port
                        st13.writeByte(logicalValue);  // logincal channel
                        st13.writeByte(typeValue);   // typeValue
                        st13.writeByte(StreamTpe);    // Stream Type
                        st13.writeByte(Checksum.xor(st13.nioBuffer(1, st13.readableBytes() - 1)));
                        st13.writeByte(0x7e);
                        channel.writeAndFlush(new NetworkMessage(st13, remoteAddress));
                    } else if (ip.equals("124.123.122.25")) {

                        ByteBuf st13 = Unpooled.buffer();
                        st13.writeByte(0x7e);
                        st13.writeShort(0x9101);
                        st13.writeShort(0x0016);
                        ByteBuf id13 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
                        st13.writeBytes(id13);
                        st13.writeShort(0x0001);
                        st13.writeByte(0x0e);     // IP Lenght 124.123.122.25
                        st13.writeInt(0x3132342e); // IP
                        st13.writeInt(0x3132332e);  // IP
                        st13.writeInt(0x3132322e);  //IP
                        st13.writeShort(0x3235);
                        st13.writeShort(value);
                        st13.writeShort(0x0000);
                        st13.writeByte(logicalValue);
                        st13.writeByte(typeValue);
                        st13.writeByte(StreamTpe);
                        st13.writeByte(Checksum.xor(st13.nioBuffer(1, st13.readableBytes() - 1)));
                        st13.writeByte(0x7e);
                        channel.writeAndFlush(new NetworkMessage(st13, remoteAddress));
                    } else {

                        // 167.86.124.88
                        ByteBuf st14 = Unpooled.buffer();
                        st14.writeByte(0x7e);
                        st14.writeShort(0x9101);
                        st14.writeShort(0x0015);
                        ByteBuf id14 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
                        st14.writeBytes(id14);
                        st14.writeShort(0x0001);
                        st14.writeByte(0x0d);
                        st14.writeInt(0x3136372E);
                        st14.writeInt(0x38362E31);
                        st14.writeInt(0x32342E38);
                        st14.writeByte(0x38);
                        st14.writeShort(value);
                        st14.writeShort(0x0000);
                        st14.writeByte(logicalValue);
                        st14.writeByte(typeValue);
                        st14.writeByte(StreamTpe);
                        st14.writeByte(Checksum.xor(st14.nioBuffer(1, st14.readableBytes() - 1)));
                        st14.writeByte(0x7e);
                        channel.writeAndFlush(new NetworkMessage(st14, remoteAddress));
                    }
                    break;

                case "playback":

                    Integer value2 = 0;
                    if (req.getParameterMap().containsKey("value")) {
                        value2 = Integer.parseInt(req.getParameter("value"));
                    }
                    String ip1 = null;
                    if (req.getParameterMap().containsKey("ip")) {
                        ip1 = String.valueOf(req.getParameter("ip"));
                    }

                    Integer logicalValue1 = 0;
                    if (req.getParameterMap().containsKey("logicalValue")) {
                        logicalValue1 = Integer.parseInt(req.getParameter("logicalValue"));
                    }
                    Integer typeValue1 = 0;
                    if (req.getParameterMap().containsKey("typeValue")) {
                        typeValue1 = Integer.parseInt(req.getParameter("typeValue"));
                    }
                    Integer StreamTpe1 = 0;
                    if (req.getParameterMap().containsKey("StreamTpe")) {
                        StreamTpe1 = Integer.parseInt(req.getParameter("StreamTpe"));
                    }
                    String starttime = null;
                    if (req.getParameterMap().containsKey("start_time")) {
                        starttime = String.valueOf(req.getParameter("start_time"));
                    }
                    String endtime = null;

                    if (req.getParameterMap().containsKey("end_time")) {
                        endtime = String.valueOf(req.getParameter("end_time"));

                    }
                    if (ip1.equals("213.136.73.98")) {
                        if (starttime == null) {
                            ByteBuf st13 = Unpooled.buffer();
                            st13.writeByte(0x7e); // Header
                            st13.writeShort(0x9201); // MessageID
                            st13.writeShort(0x0018);  // Message Length
                            ByteBuf id13 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
                            st13.writeBytes(id13);      // DeviceID
                            st13.writeShort(0x0001);   // Messae Type
                            st13.writeByte(0x0d);
                            st13.writeInt(0x3231332e);
                            st13.writeInt(0x3133362e);
                            st13.writeInt(0x37332e39);
                            st13.writeByte(0x38);
                            st13.writeShort(value2);   // TCP PORT
                            st13.writeShort(0x0000);    // UDP PORT
                            st13.writeByte(logicalValue1); // Channel ID
                            st13.writeByte(0x00);   // Video - audio type
                            st13.writeByte(0x00); //  MAin Stream / Sub Stream

                            st13.writeByte(0x01);    // Memory Type
                            st13.writeByte(0x00);    // Playback
                            st13.writeByte(0x01);     // Fast Forward
                            st13.writeByte(Checksum.xor(st13.nioBuffer(1, st13.readableBytes() - 1)));
                            st13.writeByte(0x7e);
                            channel.writeAndFlush(new NetworkMessage(st13, remoteAddress));

                        } else {

                            ByteBuf st13 = Unpooled.buffer();
                            st13.writeByte(0x7e); // Header
                            st13.writeShort(0x9201); // MessageID
                            st13.writeShort(0x0024);  // Message Length
                            ByteBuf id13 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
                            st13.writeBytes(id13);      // DeviceID
                            st13.writeShort(0x0001);   // Messae Type
                            st13.writeByte(0x0d);
                            st13.writeInt(0x3231332e);
                            st13.writeInt(0x3133362e);
                            st13.writeInt(0x37332e39);
                            st13.writeByte(0x38);
                            st13.writeShort(value2);   // TCP PORT
                            st13.writeShort(0x0000);    // UDP PORT
                            st13.writeByte(logicalValue1); // Channel ID
                            st13.writeByte(0x00);   // Video - audio type
                            st13.writeByte(0x00); //  MAin Stream / Sub Stream

                            st13.writeByte(0x01);    // Memory Type
                            st13.writeByte(0x00);    // Playback
                            st13.writeByte(0x01);     // Fast Forward
                            ByteBuf starttime1 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(starttime));
                            st13.writeBytes(starttime1);
                            ByteBuf endtime1 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(endtime));
                            st13.writeBytes(endtime1);
                            st13.writeByte(Checksum.xor(st13.nioBuffer(1, st13.readableBytes() - 1)));
                            st13.writeByte(0x7e);
                            channel.writeAndFlush(new NetworkMessage(st13, remoteAddress));
                        }

                    } else if (ip1.equals("124.123.122.25")) {
                        if (starttime == null) {
                            /////0x9201
                            ByteBuf st13 = Unpooled.buffer();
                            st13.writeByte(0x7e); // Header
                            st13.writeShort(0x9201); // MessageID
                            //// USE this lenght when  not passing date time
                            st13.writeShort(0x0019);  // Message Length
                            ///// USE this lenght when passing date time
//                          st13.writeShort(0x0025);  // Message Length
                            ByteBuf id13 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
                            st13.writeBytes(id13);      // DeviceID
                            st13.writeShort(0x0001);   // Messae Type
                            st13.writeByte(0x0e);     // IP Lenght 124.123.122.85
                            st13.writeInt(0x3132342e); // IP 
                            st13.writeInt(0x3132332e);  // IP
                            st13.writeInt(0x3132322e);  //IP
                            st13.writeShort(0x3235);   // IP
                            st13.writeShort(value2);   // TCP PORT
                            st13.writeShort(0x0000);    // UDP PORT
                            st13.writeByte(logicalValue1); // Channel ID
                            st13.writeByte(0x00);   // Video - audio type
                            st13.writeByte(0x00); //  MAin Stream / Sub Stream
                            st13.writeByte(0x01);    // Memory Type
                            st13.writeByte(0x00);    // Playback
                            st13.writeByte(0x01);     // Fast Forward
                            st13.writeByte(Checksum.xor(st13.nioBuffer(1, st13.readableBytes() - 1)));
                            st13.writeByte(0x7e);
                            channel.writeAndFlush(new NetworkMessage(st13, remoteAddress));
                        } else {

                            ByteBuf st13 = Unpooled.buffer();
                            st13.writeByte(0x7e); // Header
                            st13.writeShort(0x9201); // MessageID
                            //// USE this lenght when  not passing date time
                            st13.writeShort(0x0025);  // Message Length
                            ///// USE this lenght when passing date time
//                st13.writeShort(0x0025);  // Message Length
                            ByteBuf id13 = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(uniqueId));
                            st13.writeBytes(id13);      // DeviceID
                            st13.writeShort(0x0001);   // Messae Type
                            st13.writeByte(0x0e);     // IP Lenght 124.123.122.85
                            st13.writeInt(0x3132342e); // IP 
                            st13.writeInt(0x3132332e);  // IP
                            st13.writeInt(0x3132322e);  //IP
                            st13.writeShort(0x3235);   // IP
                            st13.writeShort(value2);   // TCP PORT
                            st13.writeShort(0x0000);    // UDP PORT
                            st13.writeByte(logicalValue1); // Channel ID
                            st13.writeByte(0x00);   // Video - audio type
                            st13.writeByte(0x00); //  MAin Stream / Sub Stream
                            st13.writeByte(0x01);    // Memory Type
                            st13.writeByte(0x00);    // Playback
                            st13.writeByte(0x01);     // Fast Forward
                            ByteBuf start = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(starttime));
                            st13.writeBytes(start);
                            ByteBuf end = Unpooled.wrappedBuffer(DatatypeConverter.parseHexBinary(endtime));
                            st13.writeBytes(end);
                            st13.writeByte(Checksum.xor(st13.nioBuffer(1, st13.readableBytes() - 1)));
                            st13.writeByte(0x7e);
                            channel.writeAndFlush(new NetworkMessage(st13, remoteAddress));

                        }
                    }
                    break;
                default:
                    break;
            }
        } else if (type.equals("1")) {

        }

    }
//     public void write(Object message) {
//        channel.writeAndFlush(new NetworkMessage(message, remoteAddress));
//    }
}
