/*
 * Copyright 2015 - 2018 Anton Tananaev (anton@traccar.org)
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
package org.traccar.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;

import org.traccar.*;
import org.traccar.helper.BcdUtil;
import org.traccar.helper.BitUtil;
import org.traccar.helper.Checksum;
import org.traccar.helper.DateBuilder;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.Position;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import org.jboss.netty.buffer.ChannelBuffers;
import org.traccar.helper.DistanceCalculator;
import org.traccar.model.VideoData;

public class TTDCAMProtocolDecoder extends BaseProtocolDecoder {
    
    private final TimeZone timeZone = TimeZone.getTimeZone("UTC");
     int total_video=0;

    public TTDCAMProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    public static final int MSG_GENERAL_RESPONSE = 0x8001;
    public static final int MSG_TERMINAL_HB_RESPONSE = 0x0002;

    public static final int MSG_TERMINAL_REGISTER = 0x0100;
    public static final int MSG_TERMINAL_REGISTER_RESPONSE = 0x8100;
    public static final int MSG_TERMINAL_AUTH = 0x0102;
    public static final int MSG_LOCATION_REPORT = 0x0200;
    public static final int MSG_LOCATION_BATCH = 0x0705;
    public static final int MSG_OIL_CONTROL = 0XA006;
    public static final int MSG_TERMINAL_UNIVERSAL_ANSWER = 0x0001;
    public static final int PARAMETER_DATA = 0x0104;
     public static final int VIDEO_DATA = 0x1205;



    public static final int RESULT_SUCCESS = 0;

    public static ByteBuf formatMessage(int type, ByteBuf id, ByteBuf data) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(0x7e);
        buf.writeShort(type);
        buf.writeShort(data.readableBytes());
        buf.writeBytes(id);
        buf.writeShort(0); // index
        buf.writeBytes(data);
        data.release();
        buf.writeByte(Checksum.xor(buf.nioBuffer(1, buf.readableBytes() - 1)));
        buf.writeByte(0x7e);
        return buf;
    }

    private void sendGeneralResponse(
            Channel channel, SocketAddress remoteAddress, ByteBuf id, int type, int index) {
        if (channel != null) {
            
//            PrintOut.PrintOutString("id---------------> "+ByteBufUtil.hexDump(id));

            ByteBuf response = Unpooled.buffer();
            response.writeShort(index);
            response.writeShort(type);
            response.writeByte(RESULT_SUCCESS);
            channel.writeAndFlush(new NetworkMessage(
                    formatMessage(MSG_GENERAL_RESPONSE, id, response), remoteAddress));
        }
    }
    
     private void decodeAlarm1(Position position,long value) {
//        System.out.println("alarm value-----"+value);
        if (BitUtil.check(value, 0)) {
//              System.out.println("alaram sos-----");
           position.set("alarm", "sos");
        }
        if (BitUtil.check(value, 1)) {
//              System.out.println("alaram ALARM_OVERSPEED-----");
             position.set("speed", "overspeed");
        }
        
        if (BitUtil.check(value, 2)) {
//              System.out.println("alaram Tired-----");
             position.set("alarm", "tired");
        }
        if (BitUtil.check(value, 5)) {
//               System.out.println("alaram ALARM_GPS_ANTENNA_CUT-----");
             position.set("gps_antenna","disconnect");
        }
        if (BitUtil.check(value, 4) || BitUtil.check(value, 9)
                || BitUtil.check(value, 10) || BitUtil.check(value, 11)) {
//             System.out.println("alaram fault-----");
             position.set("alarm", "fault");
        }
        
         if (BitUtil.check(value, 7)) {
//               System.out.println("alaram power low-----");
        position.set("power", "low");
        }
        if (BitUtil.check(value, 8)) {
//               System.out.println("alaram powerCut-----");
        position.set("alarm", "powerCut");
        }
           if (BitUtil.check(value, 15)) {
//              System.out.println("alaram motion-----");
             position.set("alarm", "motion");
        }
        if (BitUtil.check(value, 20)) {
//            System.out.println("alaram geofence-----");
               position.set("alarm", "geofence");
        }
        if (BitUtil.check(value, 29)) {
//            System.out.println("alaram ALARM_Crash-----");
             position.set("alarm", "Crash");
        }
      
         if (BitUtil.check(value, 25)) {
//            System.out.println("alaram ALARM-----25");
             position.set("alarm", "Crash");
        }
         
          if (BitUtil.check(value, 26)) {
//            System.out.println("alaram ALARM-----26");
             position.set("alarm", "Crash");
        }
           if (BitUtil.check(value, 27)) {
//            System.out.println("alaram ALARM-----27");
             position.set("alarm", "Crash");
        }
           
            if (BitUtil.check(value, 28)) {
//            System.out.println("alaram ALARM-----28");
             position.set("alarm", "Crash");
        }
             if (BitUtil.check(value, 24)) {
            System.out.println("alaram ALARM-----29");
             position.set("alarm", "Crash");
        }
    }

    private String decodeAlarm(long value) {
        
//        System.out.println("alaraaaaaaaaaaaam value------"+value);
        if (BitUtil.check(value, 0)) {
            return Position.ALARM_SOS;
        }
        if (BitUtil.check(value, 1)) {
            return Position.ALARM_OVERSPEED;
        }
        if (BitUtil.check(value, 5)) {                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   
            return Position.ALARM_GPS_ANTENNA_CUT;
        }
        if (BitUtil.check(value, 4) || BitUtil.check(value, 9)
                || BitUtil.check(value, 10) || BitUtil.check(value, 11)) {
            return Position.ALARM_FAULT;
        }
        if (BitUtil.check(value, 8)) {
            return Position.ALARM_POWER_CUT;
        }
        if (BitUtil.check(value, 20)) {
            return Position.ALARM_GEOFENCE;
        }
        if (BitUtil.check(value, 29)) {
            return Position.ALARM_ACCIDENT;
        }
        return null;
    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

//         System.out.println("*******************TT DCAM decode PROTOCOL*************************");
       
  
        ByteBuf buf = (ByteBuf) msg;
      buf.readUnsignedByte(); // start marker
        int type = buf.readUnsignedShort();
      int l =  buf.readUnsignedShort(); // body length
        ByteBuf id = buf.readSlice(6); // phone number  IMEI
        int index = buf.readUnsignedShort();

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, ByteBufUtil.hexDump(id));
        if (deviceSession == null) {
            return null;
        }
        
        

//        if (deviceSession.getTimeZone() == null) {
//            deviceSession.setTimeZone(getTimeZone(deviceSession.getDeviceId(), "GMT+8"));
//        }

        if (type == MSG_TERMINAL_REGISTER) {
             	PrintOut.PrintOutString("**************************Comes under terminal*Register*************************");
//             	System.out.println("**************************Comes under terminal*Register*************************");


            if (channel != null) {
                ByteBuf response = Unpooled.buffer();
                response.writeShort(index);
                response.writeByte(RESULT_SUCCESS);
                response.writeBytes("authentication".getBytes(StandardCharsets.US_ASCII));
                     channel.writeAndFlush(new NetworkMessage(
                        formatMessage(MSG_TERMINAL_REGISTER_RESPONSE, id, response), remoteAddress));
            }
            
                         	PrintOut.PrintOutString("**************************end under terminal*Register*************************");


        } else if (type == MSG_TERMINAL_AUTH) {
                    	PrintOut.PrintOutString("**************************Comes under terminal*AUTH*************************");
           sendGeneralResponse(channel, remoteAddress, id, type, index);
          PrintOut.PrintOutString("**************************end under terminal*AUTH*************************");

        } 
         else if (type == MSG_TERMINAL_HB_RESPONSE) {
        	PrintOut.PrintOutString("**************************Comes under terminal*HEART BEAT*************************");
        	sendGeneralResponse(channel, remoteAddress, id, type, index);
                PrintOut.PrintOutString("**************************end  under terminal*HEART BEAT*************************");
        }
         // Following part added on 12-02-2018 by Dev and Moin 
        else if (type == MSG_TERMINAL_UNIVERSAL_ANSWER){
        	PrintOut.PrintOutString("**************************Comes under terminal*heart BEAT*************************");
        	sendGeneralResponse(channel, remoteAddress, id, type, index);
                PrintOut.PrintOutString("**************************end under terminal*heart BEAT*************************");
        }
        else if (type == MSG_LOCATION_BATCH) {

//            return decodeLocationBatch(deviceSession, buf);
//                 System.out.println("**********************Inside Batch Data****************************");

        List<Position> positions = new LinkedList<>();

        int count = buf.readUnsignedShort();
        
//           System.out.println("Number of data items========Blind============> "+count);
         int location_type =  buf.readUnsignedByte(); // location type
           
           
           
         //toal length 58 
               if( location_type == 0)
               {
                    buf.skipBytes(4);
                     
               }

         

        for (int i = 0; i < count; i++) {
            
//             int endIndex = buf.readUnsignedShort() + buf.readerIndex();
//            positions.add(decodeLocation(deviceSession, buf));
//            buf.readerIndex(endIndex);
            
                Position position = new Position();
                   SocketAddress data = channel.localAddress();
              InetSocketAddress address = (InetSocketAddress) data;
               int port = address.getPort();
             position.setPort(port);
                  position.setProtocol(getProtocolName());
                    position.setDeviceId(getDeviceId());
                 position.setBusiness_device_id(Context.getDataManager().getBusinessDeviceId((int)getDeviceId()));
                 
                  int length= buf.readUnsignedShort();
                  
//                     System.out.println("count------------>"+count);
                   if(length == 37 || length == 40)
                   {
                       
//                         System.out.println("length------------>"+length);
                  
               position.set(Position.KEY_ALARM, decodeAlarm(buf.readUnsignedInt()));
               
                               	int flags=buf.readInt();
                                
                                if(BitUtil.check(flags, 0)){
        	        	position.setIgnition(1);
        	        	position.set("ignition_on", "yes");
        	        	position.setTrip(1);
        	        }else{
        	        	position.setIgnition(2);
        	        	position.set("ignition_off", "yes");
        	        	position.setTrip(2);
        	        }
//                        System.out.println("flag is=======================> "+flags);//4
                      
    	        position.setValid(BitUtil.check(flags, 1));

                                
                        double lat = buf.readUnsignedInt() * 0.000001;
                        double lon = buf.readUnsignedInt() * 0.000001;
                        
                         if (BitUtil.check(flags, 2)) {
                        position.setLatitude(-lat);
                    } else {
                        position.setLatitude(lat);
                    }

                    if (BitUtil.check(flags, 3)) {
                        position.setLongitude(-lon);// device_id 39 replace imei
                    } else {
                        position.setLongitude(lon);
                    }
                     position.setAltitude(buf.readShort());
                    
                    PrintOut.PrintOutString("Latitude=================> "+position.getLatitude());
                    PrintOut.PrintOutString("Longitude=================> "+position.getLongitude());
                   
                     
                   
                    
//                    System.out.println("speed from tracker "+buf.readUnsignedShort());
                    
//                    DecimalFormat df = new DecimalFormat();
//                    df.setMaximumFractionDigits(2);
//                 double spd=Double.parseDouble(df.format(UnitsConverter.knotsFromKph(buf.readUnsignedShort() * 0.1)));
////                    
//                    position.setSpeed(spd);
//                    System.out.println("spd location---------->"+spd);

                    position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort() * 0.1) * 2);
                    
                     //position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort() * 0.1) * 2);
                    position.setCourse(buf.readUnsignedShort());
//                    System.out.println("Altitude=================> "+position.getAltitude());
//                    System.out.println("kt_Speed=================> "+position.getSpeed());
//                     System.out.println("Course=================> "+position.getCourse());

//        DateBuilder dateBuilder = new DateBuilder(timeZone)
//                        .setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
//                        .setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
//                position.setTime(dateBuilder.getDate());

                         DateBuilder dateBuilder = new DateBuilder(TimeZone.getTimeZone("GMT+5:30"))
                            .setYear(BcdUtil.readInteger(buf, 2))
                            .setMonth(BcdUtil.readInteger(buf, 2))
                            .setDay(BcdUtil.readInteger(buf, 2))
                            .setHour(BcdUtil.readInteger(buf, 2))
                            .setMinute(BcdUtil.readInteger(buf, 2))
                            .setSecond(BcdUtil.readInteger(buf, 2));
                    position.setTime(dateBuilder.getDate());
//                    
                    
                    
                if(length == 37)
                {
                    
                    buf.skipBytes(5);
                      double mileage =  new Double(buf.readInt()/10.0);
                           position.setMileage(mileage);
                }
                
                 if(length == 40)
                {
                    buf.skipBytes(8);
                      double mileage =  new Double(buf.readInt()/10.0);
                           position.setMileage(mileage);
                }
               
                
                    
                    positions.add(position);
                   }
                
        }
        PrintOut.PrintOutString("***************outside Batch Data********************");
        return positions;

        }
        else if (type == MSG_LOCATION_REPORT) {
            Position position = new Position();
//                      System.out.println("****************Inside Logical Report****************");
                position.setProtocol(getProtocolName());
                  SocketAddress data = channel.localAddress();
              InetSocketAddress address = (InetSocketAddress) data;
               int port = address.getPort();
             position.setPort(port);
                position.setDeviceId(getDeviceId());
                  
                position.setBusiness_device_id(Context.getDataManager().getBusinessDeviceId((int)getDeviceId()));
    	      
//    	 PrintOut.PrintOutString("**************************Comes under MSG LOCATION REPORT**************************");

    			    	        decodeAlarm1(position,buf.readUnsignedInt());

//    	        position.set(Position.KEY_ALARM, decodeAlarm(buf.readUnsignedInt()));
    	        
    	        int flags = buf.readInt();
    	        
    	        if(BitUtil.check(flags, 0)){
    	        	position.setIgnition(1);
    	        	position.set("ignition_on", "yes");
    	        	position.setTrip(1);
    	        }else{
    	        	position.setIgnition(2);
    	        	position.set("ignition_off", "yes");
    	        	position.setTrip(2);
    	        }


               
    	        position.setValid(BitUtil.check(flags, 1));


    	        double lat = buf.readUnsignedInt() * 0.000001;
    	        double lon = buf.readUnsignedInt() * 0.000001;
    	        if (BitUtil.check(flags, 2)) {
    	            position.setLatitude(-lat);
    	        } else {
    	            position.setLatitude(lat);
    	        }

    	        if (BitUtil.check(flags, 3)) {
    	            position.setLongitude(-lon);
    	        } else {
    	            position.setLongitude(lon);
    	        }
    	        position.setAltitude(buf.readShort());
                
                 PrintOut.PrintOutString("lat location---------->"+lat);
                    
                     PrintOut.PrintOutString("lon location---------->"+lon);
   
    	        
                position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort() * 0.1) * 2);
    	        position.setCourse(buf.readUnsignedShort());
                
//                  DateBuilder dateBuilder = new DateBuilder(timeZone)
//                .setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
//                .setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
//        position.setTime(dateBuilder.getDate());

    	        DateBuilder dateBuilder = new DateBuilder(TimeZone.getTimeZone("GMT+5:30"))
    	                .setYear(BcdUtil.readInteger(buf, 2))
    	                .setMonth(BcdUtil.readInteger(buf, 2))
    	                .setDay(BcdUtil.readInteger(buf, 2))
    	                .setHour(BcdUtil.readInteger(buf, 2))
    	                .setMinute(BcdUtil.readInteger(buf, 2))
    	                .setSecond(BcdUtil.readInteger(buf, 2));
    	        position.setTime(dateBuilder.getDate());
                
                while (buf.readableBytes() > 2) {
                       
                  int subtype = buf.readUnsignedByte();
                  int length = buf.readUnsignedByte();
                  int endIndex = buf.readerIndex() + length;
                  
                  
//                    System.out.println("subtype------>"+subtype);
//                        System.out.println("length------>"+length);
//                          System.out.println("endIndex------>"+endIndex);
                  
              switch (subtype) {
                case 0x01:
//                    position.set(Position.KEY_ODOMETER, buf.readUnsignedInt() * 100);
                 double mileage =  new Double(buf.readInt()/10.0);
                           position.set("mileage",mileage);
                            calculatedmileage(position);
                      System.out.println("mileage------>"+mileage);

                          
                    break;
                case 0x02:
                     double fuel = buf.readUnsignedShort() * 0.1;
                     
                     position.setFuel((int) fuel);

//                    position.set(Position.KEY_FUEL_LEVEL, fuel);
                    break;
                    
               case 0x03:
                  long data_03 =  buf.readUnsignedShort();
//                    position.set("data_03",data_03);
//                    System.out.println("data_03------>"+data_03);

                     break;
              
                    
                case 0x14:
                  long data_14 =  buf.readUnsignedInt();
                    position.set("data_14",data_14);
                       
//                      System.out.println("data_14------>"+data_14);
                    break;    
                 
                case 0x15:
                  long data_15 =  buf.readUnsignedInt();
                    position.set("data_15",data_15);
                       
//                      System.out.println("data_15------>"+data_15);
                    break;
                    
                case 0x16:
                  long data_16 =  buf.readUnsignedInt();
                    position.set("data_16",data_16);
                       
//                      System.out.println("data_16------>"+data_16);
                    break;
                
                    
                    
                case 0x2A:
                  long io_status =  buf.readUnsignedShort();
                    position.set("io_status",io_status);
                       
//                      System.out.println("io_status------>"+io_status);
                    break;
                    
                 case 0x2b:
                  long data_2b=  buf.readUnsignedInt();
                    position.set("data_2b",data_2b);
                       
//                      System.out.println("data_2b------>"+data_2b);
                    break;
                    
                    
                case 0x30:
                  int rssi =  buf.readUnsignedByte();
                    position.set(Position.KEY_RSSI,rssi);
                       
//                      System.out.println("rssi------>"+rssi);
                    break;
                case 0x31:
                       int stellites =  buf.readUnsignedByte();
                    position.set(Position.KEY_SATELLITES,stellites);
//                      System.out.println("stellites------>"+stellites);
                    break;
                    
                  case 0xe6:
                       int data_e6 =  buf.readUnsignedByte();
                    position.set("data_e6",data_e6);
//                      System.out.println("data_e6------>"+data_e6);
                    break;
                    
                  case 0xfe:
                           StringBuilder sb = new StringBuilder();
                     sb.append(ByteBufUtil.hexDump(buf.readBytes(26)));
                     position.set("data_fe", sb.toString());
//                      System.out.println("data_fe------>"+sb.toString());
                      
                     break;
                     
                     case 0xe8:
                           StringBuilder sb1 = new StringBuilder();
                     sb1.append(ByteBufUtil.hexDump(buf.readBytes(6)));
                     position.set("data_e8", sb1.toString());
//                      System.out.println("data_e8------>"+sb1.toString());
                      
                     break;
                     
                  case 0xE3:
                      
                        buf.skipBytes(2); // unused
                      double v= new Double(buf.readShort());
                        double v1= new Double(v*0.01);
                         position.setBattery(v1);
//                         System.out.println("battery------>"+v1);
                    break;
                    
                case 0xF3:
//                    System.out.println("obd data------>"+length);
//                        System.out.println("obd data------>"+buf.readerIndex());
                     while (buf.readerIndex() < endIndex) {
//                       
//                        int tenetType = buf.readUnsigned();
//                         int tenetLength = buf.readUnsignedShort();
                          int tenetType = buf.readUnsignedShort();
                             int tenetLength = buf.readUnsignedByte();
//                          System.out.println("f3 tenetType------>"+tenetType);
//                        System.out.println("f3 tenetLength------>"+tenetLength);
                      
                        switch (tenetType) {
                          
                            case 0x0002:
                               double speed = UnitsConverter.knotsFromKph(buf.readUnsignedShort() * 0.1);
                                 position.set("obd_speed",speed);
//                                      System.out.println("obd_speed------>"+speed);
                                break;
                                
                            case 0x0003:
                               double rpm =  buf.readUnsignedShort();
                                  position.set("obd_rpm",rpm);
//                                      System.out.println("obd_rpm------>"+rpm);
                                break;

                       
                            case 0x0004:
                              double battery = buf.readUnsignedShort() * 0.001;
                                position.set("obd_battery",battery);
//                                 System.out.println("obd_battery------>"+battery);
                                break;
                            case 0x0005:
                                double mileage1 =new Double(buf.readInt()/10.0);
                                position.set("obd_mileage",mileage1);
//                                 System.out.println("obd_mileage------>"+mileage1);
                                break;
                                
                             case 0x0006:
                                double fuel_idling =buf.readUnsignedShort() * 0.1;
                                 position.set("fuel_idling",fuel_idling);
//                                 System.out.println("fuel_idling------>"+fuel_idling);
                                break;
                                
                                
                              case 0x0007:
                                double fuel_driving =buf.readUnsignedShort() * 0.1;
                                 position.set("fuel_driving",fuel_driving);
//                                 System.out.println("fuel_driving------>"+fuel_driving);
                                break;
                                
                            case 0x0008:
                                double engine_load = buf.readUnsignedByte() * 100 / 255;
                                 position.set("obd_engine_load",engine_load);
//                                 System.out.println("obd_engine_load------>"+engine_load);
                                break;
                            case 0x0009:
                                double Coolant = buf.readUnsignedShort() - 40;
                                position.set("obd_coolant",Coolant);
//                                 System.out.println("obd_Coolant------>"+Coolant);
                                break;
                        
                                
                            case 0x000B:
                                double map = buf.readUnsignedShort();
                                position.set("obd_map",map);
//                                 System.out.println("obd_map------>"+map);
                                break;
                                
                                        
                            case 0x000C:
                                double mat = buf.readUnsignedShort() - 40;
                                position.set("obd_mat",mat);
//                                 System.out.println("obd_mat------>"+mat);
                                break;
                                
                            case 0x000D:
                                double flow_rate = buf.readUnsignedShort() % 100;
                                position.set("flow_rate",flow_rate);
//                                 System.out.println("flow_rate------>"+flow_rate);
                                break;  
                                
                             case 0x000E:
                                double Throttle = buf.readUnsignedByte()  * 100 / 255;
                                  position.set("obd_throttle",Throttle);
//                                 System.out.println("Throttle------>"+Throttle);
                                break;
                        
                                  case 0x000F:
                                double ignition_time = buf.readUnsignedByte()  * 0.5;
                                  position.set("ignition_time",ignition_time);
//                                 System.out.println("ignition_time------>"+ignition_time);
                                break;
                                
                                  case 0x0050:
                                            StringBuilder sb2 = new StringBuilder();
                                    sb2.append(ByteBufUtil.hexDump(buf.readBytes(17)));
                                    position.set("VehicleVin", sb2.toString());
//                                 System.out.println("VehicleVin------>"+sb.toString());
                                break;
                                
                                  case 0x0051:
//                                       System.out.println("fault_code------>"+tenetLength);
                                      if(tenetLength == 0)
                                      {
                                           position.set("fault_code",0.0);
                                      }else if(tenetLength == 1)
                                      {
                                            position.set("fault_code",buf.readUnsignedByte());
                                      }else if(tenetLength == 2){
                                          position.set("fault_code",buf.readUnsignedShort());
                                      }
                              
                                
                                
                                break;
                                
                               case 0x0052:
                                int trip_id = buf.readInt();
                                  position.set("trip_id",trip_id);
//                                 System.out.println("trip_id------>"+trip_id);
                                break;
                                
                                
                              case 0x0100:
                                double trip_mileage = new Double(buf.readShort()/10.0);
                                  position.set("obd_trip_mileage",trip_mileage);
//                                 System.out.println("trip_mileage------>"+trip_mileage);
                                break;
                                
                               case 0x0101:
                                double total_mileage = new Double(buf.readInt()/10.0);
                                   position.set("obd_total_mileage",total_mileage);
//                                 System.out.println("total_mileage------>"+total_mileage);
                                break;
                                
                                case 0x0102:
                                double trip_fuel =  buf.readUnsignedShort() * 0.1;
                                   position.set("obd_trip_fuel",trip_fuel);
//                                 System.out.println("obd_trip_fuel------>"+trip_fuel);
                                break;
                                
                               case 0x0103:
                                double total_fuel =buf.readUnsignedInt() * 0.1;
                                  position.set("obd_total_fuel",total_fuel);
//                                 System.out.println("total_fuel------>"+total_fuel);
                                break;
                                
                               case 0x0104:
                                double total_fuel_average =buf.readUnsignedShort() * 0.1;
                                 position.set("total_fuel_average",total_fuel_average);
//                                 System.out.println("total_fuel_average------>"+total_fuel_average);
                                break;
                                
                               case 0x010c:
                                double average_speed =UnitsConverter.knotsFromKph(buf.readUnsignedShort() * 0.1);
                                  position.set("obd_average_speed",average_speed);
//                                 System.out.println("average_speed------>"+average_speed);
                                break;
                                
                            case 0x010d:
                                double maximum_speed = UnitsConverter.knotsFromKph(buf.readUnsignedShort() * 0.1);
//                                System.out.println("obd_maximum_speed------>"+maximum_speed);
                                    position.set("obd_maximum_speed",maximum_speed);
                                break;
                                
                              case 0x010e:
                                double obd_trip_rpm= buf.readUnsignedShort();
//                                System.out.println("obd_trip_rpm------>"+obd_trip_rpm);
                                    position.set("obd_trip_rpm",obd_trip_rpm);
                                break;
                                
                                case 0x010f:
                                double obd_trip_coolant= buf.readUnsignedShort();
//                                System.out.println("obd_trip_coolant------>"+obd_trip_coolant);
                                    position.set("obd_trip_coolant",obd_trip_coolant);
                                break;
                                
                             case 0x0110:
                                double obd_trip_voltage= buf.readUnsignedShort()* 0.001;
//                                System.out.println("obd_trip_voltage------>"+obd_trip_voltage );
                                    position.set("obd_trip_voltage",obd_trip_voltage);
                                break;
                           
                           case 0x0112:
                                double obd_trip_rapid_accelerate= buf.readUnsignedShort();
//                                System.out.println("obd_trip_rapid_accelerate------>"+obd_trip_rapid_accelerate);
                                    position.set("obd_trip_rapid_accelerate",obd_trip_rapid_accelerate);
                                break;
                                
                              case 0x0113:
                                double obd_trip_rapid_decelerate= buf.readUnsignedShort();
//                                System.out.println("obd_trip_rapid_decelerate------>"+obd_trip_rapid_decelerate);
                                    position.set("obd_trip_rapid_decelerate",obd_trip_rapid_decelerate);
                                break;
                                
                                 case 0x0116:
                                double obd_trip_rapid_stop= buf.readUnsignedShort();
//                                System.out.println("obd_trip_rapid_stop------>"+obd_trip_rapid_stop);
                                    position.set("obd_trip_rapid_stop",obd_trip_rapid_stop);
                                break;
                                
                                default:
                                buf.skipBytes(endIndex - 2);
                                break;
                        }
                    }
                    break; 
                    
             
                default:
                    break;
            }
            buf.readerIndex(endIndex);
                }
    	        
//    	        
//    	        new Byte(buf.readByte()).intValue();    // ---- 
//                new Byte(buf.readByte()).intValue();    //-----
////    	        
//    	        position.setMileage(new Double(buf.readInt()/10.0));
//    	        
//    	        
//    	        buf.readByte(); // eb_type
////    	        
//    	        new Byte(buf.readByte()).intValue();    //"length--------------->"+
////    	        
//    	        buf.readShort();     // oooc
////    	        
//    	        buf.readShort();    // ood2
////    	        
//                 StringBuilder sb = new StringBuilder();
//                     sb.append(ByteBufUtil.hexDump(buf.readBytes(10)));
//                     position.set("ICCID", sb.toString());
//////    	        System.out.println("ICCID---------------->"+ByteBufUtil.hexDump(buf.readBytes(10)));
////
//    	        buf.readShort();        //alert type
////    	        
//    	        buf.readShort();        // alarm bit
    	        
                  sendGeneralResponse(channel, remoteAddress, id, type, index);

    		PrintOut.PrintOutString("***************outside Logical Report**************");
            	return position;


        } 
        else if(type==PARAMETER_DATA){
            
            
    			buf.skipBytes(2);
    			int number_of_paramter=new Byte(buf.readByte()).intValue();
//                        System.out.println("Number of paramter in answere====================> "+number_of_paramter);
                        //for(int i=1;i<=number_of_paramter;i++){
            		int paramter_id=buf.readInt();
//            		System.out.println("-----------parameter id------"+paramter_id);
            		int length_of_typed=new Byte(buf.readByte()).intValue();
//            		System.out.println("-----------parameter length------"+length_of_typed);
            		buf.readBytes(4).array(); //System.out.println("-----------parameter value------"+buf.readBytes(4).array());
                        ByteBuffer.wrap(buf.readBytes(length_of_typed).array()).getInt();// System.out.println("-----------paramter value--------"+ByteBuffer.wrap(buf.readBytes(length_of_typed).array()).getInt());
            		
            		buf.readInt() ;  //System.out.println("type-2-----------------------> "+buf.readInt());
            		new Byte(buf.readByte()).intValue();//System.out.println("length for type 2------------> "+new Byte(buf.readByte()).intValue());
            		buf.readInt();   //System.out.println("paramter value for type 2----> "+buf.readInt());
            		
            		buf.readInt();  //System.out.println("type-3-----------------------> "+buf.readInt());
            		new Byte(buf.readByte()); //System.out.println("length for type 3------------> "+new Byte(buf.readByte()).intValue());
            		buf.readInt();   //System.out.println("paramter value for type 3----> "+buf.readInt());
            		
            		buf.readInt();  //System.out.println("type-4-----------------------> "+buf.readInt());
            		new Byte(buf.readByte());    //System.out.println("length for type 4------------> "+new Byte(buf.readByte()).intValue());
            		buf.readInt();  //    System.out.println("paramter value for type 5----> "+buf.readInt());
            		
            		buf.readInt();  //System.out.println("type-5-----------------------> "+buf.readInt());
            		new Byte(buf.readByte());    //System.out.println("length for type 5------------> "+new Byte(buf.readByte()).intValue());
            		buf.readInt();  //System.out.println("paramter value for type 5----> "+buf.readInt());
            		
            		buf.readInt();  //System.out.println("type-6-----------------------> "+buf.readInt());
            		new Byte(buf.readByte()).intValue();    //System.out.println("length for type 6------------> "+new Byte(buf.readByte()).intValue());
            		buf.readInt();  //System.out.println("paramter value for type 6----> "+buf.readInt());
            		
            		buf.readInt();  //System.out.println("type-7-----------------------> "+buf.readInt());
            		new Byte(buf.readByte()).intValue();    //System.out.println("length for type 7------------> "+new Byte(buf.readByte()).intValue());
            		buf.readInt();  //System.out.println("paramter value for type 7----> "+buf.readInt());
            		
            		buf.readInt();//System.out.println("type-10-----------------------> "+buf.readInt());
            		new Byte(buf.readByte()).intValue();    //System.out.println("length for type 10------------> "+new Byte(buf.readByte()).intValue());
            		  ByteBufUtil.hexDump(buf.readBytes(8));//System.out.println("paramter value for type 10----> "+ChannelBuffers.hexDump((buf.readBytes(8))));

            		buf.readInt();//System.out.println("type-11-----------------------> "+buf.readInt());
            		new Byte(buf.readByte()).intValue();    //System.out.println("length for type 11------------> "+new Byte(buf.readByte()).intValue());
            		
            		
            		buf.readInt();  //System.out.println("type-12-----------------------> "+buf.readInt());
            		new Byte(buf.readByte()).intValue();    //System.out.println("length for type 12------------> "+new Byte(buf.readByte()).intValue());
            		
            		buf.readInt();  //System.out.println("type-13-----------------------> "+buf.readInt());
            		new Byte(buf.readByte()).intValue();//System.out.println("length for type 13------------> "+new Byte(buf.readByte()).intValue());
            		
                        ByteBufUtil.hexDump(buf.readBytes(7));//System.out.println("paramter value for type 13----> "+ChannelBuffers.hexDump((buf.readBytes(7))));
            		
            		buf.skipBytes(20+63);
            		
            		buf.skipBytes(54); //29 to 53
            		
            		
            		buf.readInt();  //System.out.println("type-55-----------------------> "+buf.readInt());
            		new Byte(buf.readByte()).intValue();    //System.out.println("length for type 55------------> "+new Byte(buf.readByte()).intValue());
              		buf.readInt();  //System.out.println("paramter value for type 55--Highest speed--> "+buf.readInt());
            		
            		
            		buf.readInt();  //System.out.println("type-56-----------------------> "+buf.readInt());
            		new Byte(buf.readByte()).intValue();    //System.out.println("length for type 56------------> "+new Byte(buf.readByte()).intValue());
            		buf.readInt();  //System.out.println("paramter value for type 56--Overspeed duration--> "+buf.readInt());
            		
            		
            		buf.skipBytes(81);
            		
            		
            		buf.readInt();  //System.out.println("type-80-----------------------> "+buf.readInt());
            		new Byte(buf.readByte()).intValue();    //System.out.println("length for type 80------------> "+new Byte(buf.readByte()).intValue());
                        int abc = buf.readInt()/10; //System.out.println("paramter value for type 80--Vehical odograph--> "+buf.readInt()/10);
            		
            		buf.readInt();  //System.out.println("type-81-----------------------> "+buf.readInt());
            		new Byte(buf.readByte()).intValue();    //System.out.println("length for type 81------------> "+new Byte(buf.readByte()).intValue());
            		buf.readShort();    //System.out.println("paramter value for type 81--province ID of the car--> "+buf.readShort());
            		
            		buf.readInt();  //System.out.println("type-82-----------------------> "+buf.readInt());
            		new Byte(buf.readByte()).intValue();    //System.out.println("length for type 82------------> "+new Byte(buf.readByte()).intValue());
            		buf.readShort();    //System.out.println("paramter value for type 82--county ID of the car place--> "+buf.readShort());
            		
            		
            		/*System.out.println("id number 10=============> "+buf.readInt());
            		int len2=new Byte(buf.readByte()).intValue();
            		System.out.println("length len2============> "+len2);*/
            		//ChannelBuffer apn = buf.readBytes(len2);
            		//System.out.println("-----------paramter value apn detail--------"+ChannelBuffers.hexDump(apn));
            		
            	//} //372
            	
                    new Byte(buf.readByte()).intValue();
//            	System.out.println("Last fo paramter in answere====================> "+new Byte(buf.readByte()).intValue());
            
            
        
        
        } 
        else if(type==VIDEO_DATA){
            
            System.out.println("VIDEO_DATA"+VIDEO_DATA);
            
                  Position position = new Position();
               position.setProtocol(getProtocolName());
               SocketAddress data = channel.localAddress();
               InetSocketAddress address = (InetSocketAddress) data;
               int port = address.getPort();
               position.setPort(port);
               position.setDeviceId(getDeviceId());
               position.setBusiness_device_id(Context.getDataManager().getBusinessDeviceId((int) getDeviceId()));
      
          int s_n =   buf.readUnsignedShort();
          
          if(s_n != 1)
          {
            int number =   buf.readUnsignedShort();
            
            if(number == 1)
            {
                buf.skipBytes(2);
                 total_video  =(int)  buf.readUnsignedInt();
                 System.out.println("total_video"+total_video);
            }
          
          }else{
               total_video  =(int)  buf.readUnsignedInt();
                                System.out.println("total_video"+total_video);

          }
          
       
           for (int i = 0; i < total_video; i++) {
                                System.out.println("=================>"+total_video);

               VideoData vd = new VideoData();
           vd.setChannel((int) buf.readUnsignedByte());
//          position.set("logical _channel",buf.readUnsignedByte());
           
            
//                         DateBuilder dateBuilder = new DateBuilder(TimeZone.getTimeZone("GMT+5:30"))
                          DateBuilder dateBuilder = new DateBuilder(TimeZone.getTimeZone("UTC"))
                            .setYear(BcdUtil.readInteger(buf, 2))
                            .setMonth(BcdUtil.readInteger(buf, 2))
                            .setDay(BcdUtil.readInteger(buf, 2))
                            .setHour(BcdUtil.readInteger(buf, 2))
                            .setMinute(BcdUtil.readInteger(buf, 2))
                            .setSecond(BcdUtil.readInteger(buf, 2));
//                    position.setTime(dateBuilder.getDate());
 DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 position.set("start_date", dateFormat.format(dateBuilder.getDate()));
               System.out.println("start_time "+dateFormat.format(dateBuilder.getDate()));

// DateBuilder dateBuilder1 = new DateBuilder(TimeZone.getTimeZone("GMT+5:30"))
                          DateBuilder dateBuilder1 = new DateBuilder(TimeZone.getTimeZone("UTC"))
                            .setYear(BcdUtil.readInteger(buf, 2))
                            .setMonth(BcdUtil.readInteger(buf, 2))
                            .setDay(BcdUtil.readInteger(buf, 2))
                            .setHour(BcdUtil.readInteger(buf, 2))
                            .setMinute(BcdUtil.readInteger(buf, 2))
                            .setSecond(BcdUtil.readInteger(buf, 2));
 
 position.set("end_date", dateFormat.format(dateBuilder1.getDate()));
                 System.out.println("end_date"+dateFormat.format(dateBuilder1.getDate()));

                
                vd.setBusiness_device_id(position.getBusiness_device_id());
                                                System.out.println("=================>"+position.getBusiness_device_id());

                vd.setStart_time(dateBuilder.getDate());
                vd.setEnd_time(dateBuilder1.getDate());
                 System.out.println("===========>"+dateBuilder.getDate());
                 System.out.println("===========>"+dateBuilder1.getDate());

                     buf.skipBytes(8); 
                     position.set("video_type", buf.readUnsignedByte());
                     position.set("stream_type", buf.readUnsignedByte());
                     position.set("memory_type", buf.readUnsignedByte());
                     position.set("file_size",  buf.readUnsignedInt());
                     
                     Context.getDataManager().AddVideoData(vd);

          
            	
                  }
        }

        return null;
    }

//    private Position decodeLocation(DeviceSession deviceSession, ByteBuf buf) {
//
//        Position position = new Position();
//        position.setProtocol(getProtocolName());
//        position.setDeviceId(deviceSession.getDeviceId());
//
//        position.set(Position.KEY_ALARM, decodeAlarm(buf.readUnsignedInt()));
//
//        int flags = buf.readInt();
//
//        position.set(Position.KEY_IGNITION, BitUtil.check(flags, 0));
//
//        position.setValid(BitUtil.check(flags, 1));
//
//        double lat = buf.readUnsignedInt() * 0.000001;
//        double lon = buf.readUnsignedInt() * 0.000001;
//
//        if (BitUtil.check(flags, 2)) {
//            position.setLatitude(-lat);
//        } else {
//            position.setLatitude(lat);
//        }
//
//        if (BitUtil.check(flags, 3)) {
//            position.setLongitude(-lon);
//        } else {
//            position.setLongitude(lon);
//        }
//
//        position.setAltitude(buf.readShort());
//        position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedShort() * 0.1));
//        position.setCourse(buf.readUnsignedShort());
//
//        DateBuilder dateBuilder = new DateBuilder(deviceSession.getTimeZone())
//                .setYear(BcdUtil.readInteger(buf, 2))
//                .setMonth(BcdUtil.readInteger(buf, 2))
//                .setDay(BcdUtil.readInteger(buf, 2))
//                .setHour(BcdUtil.readInteger(buf, 2))
//                .setMinute(BcdUtil.readInteger(buf, 2))
//                .setSecond(BcdUtil.readInteger(buf, 2));
//        position.setTime(dateBuilder.getDate());
//
//        // additional information
//
//        return position;
//    }

//    private List<Position> decodeLocationBatch(DeviceSession deviceSession, ByteBuf buf) throws ClassNotFoundException, SQLException {
//                  
//                
//
//           
//        
//
//    }
 
      private Position getLastPosition(long deviceId) {
            if(Context.getConnectionManager() != null ){
                return Context.getConnectionManager().getLastPosition(deviceId);
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
                    System.out.println(".........______total Mileage_____........"+total_mileage);

            position.setMileage(total_mileage);

            }else{
                   position.setMileage(0);
            }
            
                 
            return position ;
        }

}
