/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import org.traccar.BaseProtocolDecoder;
import org.traccar.Context;
import org.traccar.DeviceSession;
import org.traccar.NetworkMessage;
import org.traccar.Protocol;
import org.traccar.helper.BitUtil;
import org.traccar.helper.Checksum;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.CellTower;
import org.traccar.model.Network;
import org.traccar.model.Position;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.traccar.helper.DistanceCalculator;

/**
 *
 * @author addon10
 */
public class TTteltonika920sProtocolDecoder extends BaseProtocolDecoder{
    
     private static final int IMAGE_PACKET_MAX = 2048;

    private boolean connectionless;
    private boolean extended;
    private Map<Long, ByteBuf> photos = new HashMap<>();

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public TTteltonika920sProtocolDecoder(Protocol protocol, boolean connectionless) {
        super(protocol);
        this.connectionless = connectionless;
        this.extended = Context.getConfig().getBoolean(getProtocolName() + ".extended");
    }

    private void parseIdentification(Channel channel, SocketAddress remoteAddress, ByteBuf buf) throws Exception {

        int length = buf.readUnsignedShort();
        String imei = buf.toString(buf.readerIndex(), length, StandardCharsets.US_ASCII);
//        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);


        
         DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
//        if (deviceSession == null) {
//            return null;
//        }

        if (channel != null) {
            ByteBuf response = Unpooled.buffer(1);
            if (deviceSession != null) {
                response.writeByte(1);
            } else {
                response.writeByte(0);
            }
            channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
        }
    }

    public static final int CODEC_GH3000 = 0x07;
    public static final int CODEC_8 = 0x08;
    public static final int CODEC_8_EXT = 0x8E;
    public static final int CODEC_12 = 0x0C;
    public static final int CODEC_16 = 0x10;

    private void sendImageRequest(Channel channel, SocketAddress remoteAddress, long id, int offset, int size) {
        if (channel != null) {
            ByteBuf response = Unpooled.buffer();
            response.writeInt(0);
            response.writeShort(0);
            response.writeShort(19); // length
            response.writeByte(CODEC_12);
            response.writeByte(1); // nod
            response.writeByte(0x0D); // camera
            response.writeInt(11); // payload length
            response.writeByte(2); // command
            response.writeInt((int) id);
            response.writeInt(offset);
            response.writeShort(size);
            response.writeByte(1); // nod
            response.writeShort(0);
            response.writeShort(Checksum.crc16(
                    Checksum.CRC16_IBM, response.nioBuffer(8, response.readableBytes() - 10)));
            channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
        }
    }

    private void decodeSerial(Channel channel, SocketAddress remoteAddress, Position position, ByteBuf buf) {

        getLastLocation(position, null);

        int type = buf.readUnsignedByte();
        if (type == 0x0D) {

            buf.readInt(); // length
            int subtype = buf.readUnsignedByte();
            if (subtype == 0x01) {

                long photoId = buf.readUnsignedInt();
                ByteBuf photo = Unpooled.buffer(buf.readInt());
                photos.put(photoId, photo);
                sendImageRequest(
                        channel, remoteAddress, photoId,
                        0, Math.min(IMAGE_PACKET_MAX, photo.capacity()));

            } else if (subtype == 0x02) {

                long photoId = buf.readUnsignedInt();
                buf.readInt(); // offset
                ByteBuf photo = photos.get(photoId);
                photo.writeBytes(buf, buf.readUnsignedShort());
                if (photo.writableBytes() > 0) {
                    sendImageRequest(
                            channel, remoteAddress, photoId,
                            photo.writerIndex(), Math.min(IMAGE_PACKET_MAX, photo.writableBytes()));
                } else {
                    String uniqueId = Context.getIdentityManager().getDeviceById(position.getDeviceId()).getUniqueId();
                    photos.remove(photoId);
                    try {
                        position.set(Position.KEY_IMAGE, Context.getMediaManager().writeFile(uniqueId, photo, "jpg"));
                    } finally {
                        photo.release();
                    }
                }

            }

        } else {

            position.set(Position.KEY_TYPE, type);

            int length = buf.readInt();
            boolean readable = true;
            for (int i = 0; i < length; i++) {
                byte b = buf.getByte(buf.readerIndex() + i);
                if (b < 32 && b != '\r' && b != '\n') {
                    readable = false;
                    break;
                }
            }

            if (readable) {
                String data = buf.readSlice(length).toString(StandardCharsets.US_ASCII).trim();
                if (data.startsWith("UUUUww") && data.endsWith("SSS")) {
                    String[] values = data.substring(6, data.length() - 4).split(";");
                    for (int i = 0; i < 8; i++) {
                        position.set("axle" + (i + 1), Double.parseDouble(values[i]));
                    }
                    position.set("loadTruck", Double.parseDouble(values[8]));
                    position.set("loadTrailer", Double.parseDouble(values[9]));
                    position.set("totalTruck", Double.parseDouble(values[10]));
                    position.set("totalTrailer", Double.parseDouble(values[11]));
                } else {
                    position.set(Position.KEY_RESULT, data);
                }
            } else {
                position.set(Position.KEY_RESULT, ByteBufUtil.hexDump(buf.readSlice(length)));
            }
        }
    }

    private long readValue(ByteBuf buf, int length, boolean signed) {
        switch (length) {
            case 1:
                return signed ? buf.readByte() : buf.readUnsignedByte();
            case 2:
                return signed ? buf.readShort() : buf.readUnsignedShort();
            case 4:
                return signed ? buf.readInt() : buf.readUnsignedInt();
            default:
                return buf.readLong();
        }
    }
    
    private Position getLastPosition(long deviceId) {
            if(Context.getConnectionManager() != null ){
                return Context.getConnectionManager().getLastPosition(deviceId);
            }
            return null;
        }

    private void decodeOtherParameter(Position position, int id, ByteBuf buf, int length) {
        Position last = getLastPosition(position.getDeviceId());
//          System.out.println("switch id---------->"+id);
        switch (id) {
            case 1:
                 
            case 2:
            case 3:
            case 4:
                long value =  readValue(buf, length, false);
                 position.set("di" + id, value);
//                 System.out.println("digit input---------->"+id);
//                     System.out.println("value 4---------->"+value);
              if(id == 1)
                {
                  if(value == 1)
                  {
                         position.set(Position.KEY_ALARM, Position.ALARM_SOS);
                  }
                }
//                  if(id == 2)
//                {
////                      System.out.println("digit input 2---------->"+id);
////                        System.out.println("value 2---------->"+value);
//                  if(value == 1)
//                  {
////                        System.out.println("ramp on =  1---------------------");
//                         position.set("ramp","on");
//                              position.setAc(1); 
//                  }
//                  if(value == 0){
////                        System.out.println("ramp off =  0 ---------------------");
//                        position.set("ramp","off");
//                        position.setAc(2);
//                  }
//                 }
                break;
            case 9:
                 long analog =  readValue(buf, length, false);
                  position.setFuel((int) analog);
                position.set(Position.PREFIX_ADC + 1,analog);
                break;
            case 17:
                position.set("axisX", readValue(buf, length, true));
                break;
            case 18:
                position.set("axisY", readValue(buf, length, true));
                break;
            case 19:
                position.set("axisZ", readValue(buf, length, true));
                break;
            case 21:
                position.set(Position.KEY_RSSI, readValue(buf, length, false));
                break;
            case 25:
            case 26:
            case 27:
            case 28:
                position.set(Position.PREFIX_TEMP + (id - 24), readValue(buf, length, true) * 0.1);
                break;
            case 66:
                double power = readValue(buf, length, false) * 0.001;
//                 System.out.println("power ---------------------"+power);
                position.set(Position.KEY_POWER, power);
                break;
            case 67:
                double battery = readValue(buf, length, false) * 0.001;
//                  System.out.println("battery ---------------------"+battery);
                position.set(Position.KEY_BATTERY, battery);
                position.setBattery(battery);
                break;
            case 69:
                position.set("gpsStatus", readValue(buf, length, false));
                break;
            case 72:
            case 73:
            case 74:
                position.set(Position.PREFIX_TEMP + (id - 71), readValue(buf, length, true) * 0.1);
                break;
            case 78:
                long driverUniqueId = readValue(buf, length, false);
                if (driverUniqueId != 0) {
                    position.set(Position.KEY_DRIVER_UNIQUE_ID, String.format("%016X", driverUniqueId));
                }
                break;
            case 80:
                position.set("workMode", readValue(buf, length, false));
                break;
            case 129:
            case 130:
            case 131:
            case 132:
            case 133:
            case 134:
                String driver = id == 129 || id == 132 ? "" : position.getString("driver1");
                position.set("driver" + (id >= 132 ? 2 : 1),
                        driver + buf.readSlice(length).toString(StandardCharsets.US_ASCII).trim());
                break;
            case 179:
                position.set(Position.PREFIX_OUT + 1, readValue(buf, length, false) == 1);
                break;
            case 180:
                position.set(Position.PREFIX_OUT + 2, readValue(buf, length, false) == 1);
                break;
            case 181:
                position.set(Position.KEY_PDOP, readValue(buf, length, false) * 0.1);
                break;
            case 182:
                position.set(Position.KEY_HDOP, readValue(buf, length, false) * 0.1);
                break;
            case 236:
                if (readValue(buf, length, false) == 1) {
                    position.set(Position.KEY_ALARM, Position.ALARM_OVERSPEED);
                }
                break;
            case 237:
                position.set(Position.KEY_MOTION, readValue(buf, length, false) == 0);
                break;
            case 238:
                switch ((int) readValue(buf, length, false)) {
                    case 1:
//                          System.out.println("ALARM_ACCELERATION-------------->");
                        position.set(Position.KEY_ALARM, Position.ALARM_ACCELERATION);
                        break;
                    case 2:
//                         System.out.println("ALARM_BRAKING-------------->");
                        position.set(Position.KEY_ALARM, Position.ALARM_BRAKING);
                        break;
                    case 3:
//                       System.out.println("ALARM_CORNERING-------------->");
                        position.set(Position.KEY_ALARM, Position.ALARM_CORNERING);
                        break;
                    default:
                        break;
                }
                break;
            case 239:
               long value1 =  readValue(buf, length, false);
                 if(value1 == 1)
                  {
//                         System.out.println("ignition on  ---------------------");
                        position.set("ignition_on", "yes");
                        position.setIgnition(1);
                        position.setTrip(1);
                  }else if(value1 == 0){
                      
//                         System.out.println("ignition off  ---------------------");
                       position.set("ignition_off","yes");
                       position.setIgnition(2);
                       position.setTrip(2);
                  }
//                System.out.println("ignition value-------->"+value1);
//                position.set(Position.KEY_IGNITION, readValue(buf, length, false) == 1);
                break;
            case 240:
                position.set(Position.KEY_MOTION, readValue(buf, length, false) == 1);
                
                break;
            case 241:
                position.set(Position.KEY_OPERATOR, readValue(buf, length, false));
                break;
            case 199:
               
                  double mileage =  readValue(buf, length, false);
                   position.set("p_mileage", mileage / 1000);
//                     System.out.println("prtocol mileage mileage meter--------->"+mileage);
//                   position.setMileage(mileage / 1000);
                   break;
             case 16:
                  double total_mileage =  readValue(buf, length, false);
                   position.set("p_mileage_total", total_mileage / 1000);
                   position.setMileage( total_mileage / 1000);
//                     System.out.println("prtocol total_mileage mileage meter--------->"+total_mileage);
//                    System.out.println("prtocol total_mileage mileage meter--------->"+total_mileage);
//                   System.out.println("prtocol total_mileage mileage--------->"+total_mileage / 1000);
                   break;
                   
             case 252:
                switch ((int) readValue(buf, length, false)) {
                    case 1:
                        position.set(Position.KEY_ALARM, "powerCut");
                        position.set(Position.KEY_ALARM, "unplugIn");
                        break;
                     case 0:
                         position.set(Position.KEY_ALARM, "pulgIn");
                        break;   
                    default:
                        break;
                }
                 break;
                 
               case 251:
                 double idle =  readValue(buf, length, false);
//                 System.out.println("idle-------------->"+idle);
                 break;
                 
              case 253:
//                 System.out.println("Green_Driving_Type-------------->"+Green_Driving_Type);
                 
                      
                      switch ((int) readValue(buf, length, false)) {
                    case 1:
                        position.set("hard", Position.ALARM_ACCELERATION);
                        break;
                    case 2:
                        position.set("break", Position.ALARM_BRAKING);
                        break;
                    case 3:
                        position.set(Position.KEY_ALARM, "HARD_CORNERING");
                        break;
                    default:
                        break;
                }
                 break;
                 
               case 246:
                 double Towing =  readValue(buf, length, false);
//                 System.out.println("Towing-------------->"+Towing);
                 break;
                 
               case 249:
                 double Jamming =  readValue(buf, length, false);
//                 System.out.println("Jamming-------------->"+Jamming);
                 break;
                 
               case 247:
                 int crash = ((int) readValue(buf, length, false));
                  if(crash == 1)
                  {
                     position.set("alarm", "Crash");
                  }
                 break;
            default:
                position.set(Position.PREFIX_IO + id, readValue(buf, length, false));
                break;
        }
    }

    private void decodeGh3000Parameter(Position position, int id, ByteBuf buf, int length) {
        
//        System.out.println("decodeGh3000Parameter-------------->");
        switch (id) {
            case 1:
                position.set(Position.KEY_BATTERY_LEVEL, readValue(buf, length, false));
                break;
            case 2:
                position.set("usbConnected", readValue(buf, length, false) == 1);
                break;
            case 5:
                position.set("uptime", readValue(buf, length, false));
                break;
            case 20:
                position.set(Position.KEY_HDOP, readValue(buf, length, false) * 0.1);
                break;
            case 21:
                position.set(Position.KEY_VDOP, readValue(buf, length, false) * 0.1);
                break;
            case 22:
                position.set(Position.KEY_PDOP, readValue(buf, length, false) * 0.1);
                break;
            case 67:
                position.set(Position.KEY_BATTERY, readValue(buf, length, false) * 0.001);
                break;
            case 221:
                position.set("button", readValue(buf, length, false));
                break;
            case 222:
                if (readValue(buf, length, false) == 1) {
                    position.set(Position.KEY_ALARM, Position.ALARM_SOS);
                }
                break;
            case 240:
                position.set(Position.KEY_MOTION, readValue(buf, length, false) == 1);
                break;
            case 244:
                position.set(Position.KEY_ROAMING, readValue(buf, length, false) == 1);
                break;
           
            default:
                position.set(Position.PREFIX_IO + id, readValue(buf, length, false));
                break;
        }
    }

    private void decodeParameter(Position position, int id, ByteBuf buf, int length, int codec) {
        if (codec == CODEC_GH3000) {
            decodeGh3000Parameter(position, id, buf, length);
        } else {

            decodeOtherParameter(position, id, buf, length);
        }
    }

    private void decodeNetwork(Position position) {
        long cid = position.getLong(Position.PREFIX_IO + 205);
        int lac = position.getInteger(Position.PREFIX_IO + 206);
        if (cid != 0 && lac != 0) {
            CellTower cellTower = CellTower.fromLacCid(lac, cid);
            long operator = position.getInteger(Position.KEY_OPERATOR);
            if (operator != 0) {
                cellTower.setOperator(operator);
            }
            position.setNetwork(new Network(cellTower));
        }
    }

    private int readExtByte(ByteBuf buf, int codec, int... codecs) {
        boolean ext = false;
        for (int c : codecs) {
            if (codec == c) {
                ext = true;
                break;
            }
        }
        if (ext) {
            return buf.readUnsignedShort();
        } else {
            return buf.readUnsignedByte();
        }
    }

    private void decodeLocation(Position position, ByteBuf buf, int codec) {

        int globalMask = 0x0f;

        if (codec == CODEC_GH3000) {

            long time = buf.readUnsignedInt() & 0x3fffffff;
            time += 1167609600; // 2007-01-01 00:00:00

            globalMask = buf.readUnsignedByte();
            if (BitUtil.check(globalMask, 0)) {

                position.setTime(new Date(time * 1000));

                int locationMask = buf.readUnsignedByte();

                if (BitUtil.check(locationMask, 0)) {
                    position.setLatitude(buf.readFloat());
                    position.setLongitude(buf.readFloat());
                }

                if (BitUtil.check(locationMask, 1)) {
                    position.setAltitude(buf.readUnsignedShort());
                }

                if (BitUtil.check(locationMask, 2)) {
                    position.setCourse(buf.readUnsignedByte() * 360.0 / 256);
                }

                if (BitUtil.check(locationMask, 3)) {
                    position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte() * 2));
                }

                if (BitUtil.check(locationMask, 4)) {
                    position.set(Position.KEY_SATELLITES, buf.readUnsignedByte());
                }

                if (BitUtil.check(locationMask, 5)) {
                    CellTower cellTower = CellTower.fromLacCid(buf.readUnsignedShort(), buf.readUnsignedShort());

                    if (BitUtil.check(locationMask, 6)) {
                        cellTower.setSignalStrength((int) buf.readUnsignedByte());
                    }

                    if (BitUtil.check(locationMask, 7)) {
                        cellTower.setOperator(buf.readUnsignedInt());
                    }

                    position.setNetwork(new Network(cellTower));

                } else {
                    if (BitUtil.check(locationMask, 6)) {
                        position.set(Position.KEY_RSSI, buf.readUnsignedByte());
                    }
                    if (BitUtil.check(locationMask, 7)) {
                        position.set(Position.KEY_OPERATOR, buf.readUnsignedInt());
                    }
                }

            } else {

                getLastLocation(position, new Date(time * 1000));

            }

        } else {

            position.setTime(new Date(buf.readLong()));

            position.set("priority", buf.readUnsignedByte());

            position.setLongitude(buf.readInt() / 10000000.0);
            position.setLatitude(buf.readInt() / 10000000.0);
            position.setAltitude(buf.readShort());
            position.setCourse(buf.readUnsignedShort());

            int satellites = buf.readUnsignedByte();
            position.set(Position.KEY_SATELLITES, satellites);

            position.setValid(satellites != 0);

            position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort() * 2));

            position.set(Position.KEY_EVENT, readExtByte(buf, codec, CODEC_8_EXT, CODEC_16));
            if (codec == CODEC_16) {
                buf.readUnsignedByte(); // generation type
            }

            readExtByte(buf, codec, CODEC_8_EXT); // total IO data records

        }

        // Read 1 byte data
        if (BitUtil.check(globalMask, 1)) {
            int cnt = readExtByte(buf, codec, CODEC_8_EXT);
            for (int j = 0; j < cnt; j++) {
                decodeParameter(position, readExtByte(buf, codec, CODEC_8_EXT, CODEC_16), buf, 1, codec);
            }
        }

        // Read 2 byte data
        if (BitUtil.check(globalMask, 2)) {
            int cnt = readExtByte(buf, codec, CODEC_8_EXT);
            for (int j = 0; j < cnt; j++) {
                decodeParameter(position, readExtByte(buf, codec, CODEC_8_EXT, CODEC_16), buf, 2, codec);
            }
        }

        // Read 4 byte data
        if (BitUtil.check(globalMask, 3)) {
            int cnt = readExtByte(buf, codec, CODEC_8_EXT);
            for (int j = 0; j < cnt; j++) {
                decodeParameter(position, readExtByte(buf, codec, CODEC_8_EXT, CODEC_16), buf, 4, codec);
            }
        }

        // Read 8 byte data
        if (codec == CODEC_8 || codec == CODEC_8_EXT || codec == CODEC_16) {
            int cnt = readExtByte(buf, codec, CODEC_8_EXT);
            for (int j = 0; j < cnt; j++) {
                decodeOtherParameter(position, readExtByte(buf, codec, CODEC_8_EXT, CODEC_16), buf, 8);
            }
        }

        // Read 16 byte data
        if (extended) {
            int cnt = readExtByte(buf, codec, CODEC_8_EXT);
            for (int j = 0; j < cnt; j++) {
                int id = readExtByte(buf, codec, CODEC_8_EXT, CODEC_16);
                position.set(Position.PREFIX_IO + id, ByteBufUtil.hexDump(buf.readSlice(16)));
            }
        }

        // Read X byte data
        if (codec == CODEC_8_EXT) {
            int cnt = buf.readUnsignedShort();
            for (int j = 0; j < cnt; j++) {
                int id = buf.readUnsignedShort();
                int length = buf.readUnsignedShort();
                if (id == 256) {
                    position.set(Position.KEY_VIN, buf.readSlice(length).toString(StandardCharsets.US_ASCII));
                } else {
                    position.set(Position.PREFIX_IO + id, ByteBufUtil.hexDump(buf.readSlice(length)));
                }
            }
        }

        decodeNetwork(position);

    }

    private List<Position> parseData(
            Channel channel, SocketAddress remoteAddress, ByteBuf buf, int locationPacketId, String imei) throws Exception {
        List<Position> positions = new LinkedList<>();

        if (!connectionless) {
            buf.readUnsignedInt(); // data length
        }

        int codec = buf.readUnsignedByte();
        int count = buf.readUnsignedByte();

//        System.out.println("imei------>"+imei);
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
        

        if (deviceSession == null) {
            return null;
        }

        for (int i = 0; i < count; i++) {
            Position position = new Position(getProtocolName());
SocketAddress data = channel.localAddress();
              InetSocketAddress address = (InetSocketAddress) data;
               int port = address.getPort();
             position.setPort(port);
            position.setDeviceId(deviceSession.getDeviceId());
//            System.out.println("business_device id--------->"+(int)getDeviceId());
            position.setBusiness_device_id(Context.getDataManager().getBusinessDeviceId((int)getDeviceId()));
            position.setValid(true);
//            position.setMileage(0);

            if (codec == CODEC_12) {
//                 System.out.println("********************* parse serail data ***********************************");
                decodeSerial(channel, remoteAddress, position, buf);
            } else {
//                                 System.out.println("********************* parse location data ***********************************");

                decodeLocation(position, buf, codec);
//                calculatedmileage(position);
            }

            if (!position.getOutdated() || !position.getAttributes().isEmpty()) {
                positions.add(position);
            }
        }

        if (channel != null) {
            if (connectionless) {
                ByteBuf response = Unpooled.buffer();
                response.writeShort(5);
                response.writeShort(0);
                response.writeByte(0x01);
                response.writeByte(locationPacketId);
                response.writeByte(count);
                channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
            } else {
                ByteBuf response = Unpooled.buffer();
                response.writeInt(count);
                channel.writeAndFlush(new NetworkMessage(response, remoteAddress));
            }
        }

        return positions.isEmpty() ? null : positions;
    }

    @Override
    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        if (connectionless) {
//            System.out.println("********************* Upd data ***********************************");
            return decodeUdp(channel, remoteAddress, buf);
        } else {
//                System.out.println("********************* Tcp data ***********************************");
            return decodeTcp(channel, remoteAddress, buf);
        }
    }

    private Object decodeTcp(Channel channel, SocketAddress remoteAddress, ByteBuf buf) throws Exception {

        if (buf.getUnsignedShort(0) > 0) {
//             System.out.println("********************* Tcp parseIdentification data ***********************************");
            parseIdentification(channel, remoteAddress, buf);
        } else {
//             System.out.println("********************* Tcp parseData data ***********************************");
            buf.skipBytes(4);
//            return parseData(channel, remoteAddress, buf, 0);
           return parseData(channel, remoteAddress, buf, 0, null);
        }

        return null;
    }
    
    
     private Position calculatedmileage (Position position) {
            
            double mileage = 0.0;
            double last_mileage = 0.0;
             double total_mileage = 0.0;
            
            Position last = getLastPosition(position.getDeviceId());
            if(last != null) {

                mileage += DistanceCalculator.distance(position.getLatitude(), position.getLongitude(),
                        last.getLatitude(), last.getLongitude());
               
                mileage = BigDecimal.valueOf(mileage).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                
//                System.out.println(".........______Mileage_____........"+mileage);
//                System.out.println(".........______last Mileage_____........"+last.getMileage());
//                System.out.println(".........______total Mileage_____........"+last_mileage);

          last_mileage =  mileage * 0.001;
                  total_mileage  = last.getMileage() + last_mileage;
                    
//            position.setMileage(total_mileage);
//             System.out.println(".........______total Mileage_____........"+total_mileage);
            position.set("mileage", last_mileage);
              position.set("total_mileage", total_mileage);
            }else{
                   position.setMileage(0);
            }
            
                 
            return position ;
        }

    
    
    

    private Object decodeUdp(Channel channel, SocketAddress remoteAddress, ByteBuf buf) throws Exception {

        buf.readUnsignedShort(); // length
        buf.readUnsignedShort(); // packet id
        buf.readUnsignedByte(); // packet type
        int locationPacketId = buf.readUnsignedByte();
        String imei = buf.readSlice(buf.readUnsignedShort()).toString(StandardCharsets.US_ASCII);

        return parseData(channel, remoteAddress, buf, locationPacketId, imei);

    }

    
}
