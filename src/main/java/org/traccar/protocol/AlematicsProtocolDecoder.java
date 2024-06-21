/*
 * Copyright 2017 - 2018 Anton Tananaev (anton@traccar.org)
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

import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import org.traccar.BaseProtocolDecoder;
import org.traccar.DeviceSession;
import org.traccar.Protocol;
import org.traccar.helper.BitUtil;
import org.traccar.helper.Parser;
import org.traccar.helper.PatternBuilder;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.Position;

import java.net.SocketAddress;
import java.util.Date;
import java.util.regex.Pattern;
import org.traccar.Context;

public class AlematicsProtocolDecoder extends BaseProtocolDecoder {

     public AlematicsProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    private static final Pattern PATTERN = new PatternBuilder()
            .text("$T,")
            .number("(d+),")                     // type
            .number("(d+),")                     // index
            .number("(d+),")                     // id
            .number("(dddd)(dd)(dd)")            // gps date
            .number("(dd)(dd)(dd),")             // gps time
            .number("(dddd)(dd)(dd)")            // device date
            .number("(dd)(dd)(dd),")             // device time
            .number("(-?d+.d+),")                // latitude
            .number("(-?d+.d+),")                // longitude
            .number("(d+),")                     // speed
            .number("(d+),")                     // course
            .number("(-?d+),")                   // altitude
            .number("(d+.d),")                   // hdop
            .number("(d+),")                     // satellites
            .number("(d+),")                     // input
            .number("(d+),")                     // output
            .number("(d+.d+),")                  // adc
            .number("(d+.d+),")                  // power
            .number("(d+),")                     // odometer
            .groupBegin()
            .text("0,$S,")
            .expression("(.*)")                  // text message
            .or()
            .number("(d+),")                     // extra mask
            .expression("(.*)")                  // extra data
            .or()
            .any()
            .groupEnd()
            .compile();

    private void decodeExtras(Position position, Parser parser) {
 int mask = parser.nextInt();
        String[] data = parser.next().split(",");


        int index = 0;

    if(mask == 24)
      position.setTemperature(Integer.parseInt(data[index++]));
//     position.set("temprature", Integer.parseInt(data[index++]));

        if (BitUtil.check(mask, 0)) {
            index++; // pulse counter 3

        }

        if (BitUtil.check(mask, 1)) {
            position.set(Position.KEY_POWER, Integer.parseInt(data[index++]));
        }

        if (BitUtil.check(mask, 2)) {
            int battery = Integer.parseInt(data[index++]);
            position.set(Position.KEY_BATTERY, battery/100);
            position.setBattery(battery/100);
        }

        if (BitUtil.check(mask, 3)) {
            position.set(Position.KEY_OBD_SPEED, Integer.parseInt(data[index++]));
        }

        if (BitUtil.check(mask, 4)) {
            position.set(Position.KEY_RPM, Integer.parseInt(data[index++]));
        }

        if (BitUtil.check(mask, 5)) {
            position.set(Position.KEY_RSSI, Integer.parseInt(data[index++]));
        }

        if (BitUtil.check(mask, 6)) {
           position.set("extraData", Integer.parseInt(data[index++]));// index++; // pulse counter 2
        }

        if (BitUtil.check(mask, 7)) {
            position.set("sensor_rpm", Integer.parseInt(data[index++]));//index++; // magnetic rotation sensor rpm
        }

    }
    
     private Position decodeAlarm(int type , int input, int output ,  Position position){
        
       switch(type){
            
 
                   
            case 11:
                  if(input == 1){
                   position.set("gps_antenna", "connect");
                
               } else if(input == 0){
                   position.set("gps_antenna","disconnect");
              
               } 
                  return position;
            
            case 12:
                   position.set("gps_antenna", "short_short");
                   return position;
            
            case 13:
                   position.set("gps_antenna", "re-connect");
                   return position;
                 
            case 20:
                   position.set("power", "on");
                   return position;
                  
           
            case 51:
                   position.set("speed", "overspeed");
                   return position;
           
            case 53:
                   position.set("power", "low");
                   return position;  
         
            case 54:
                   position.set("power", "restore");
                   return position;
//          
            case 55:
                   position.set("battery", "low");
                   return position;  
           
          
            case 50:
               if(input == 1){
                   position.set("ignition_on", "yes");
                   position.setIgnition(1);
                   position.setTrip(1);
               } else if(input == 0){
                   position.set("ignition_off","yes");
                   position.setIgnition(2);
                   position.setTrip(2);
               }
               return position;
           
            case 56:
               if(input == 3){
                     position.set("ac", "on");
                     position.setAc(1); 
                     
                } else if(input == 1){
                   position.set("ac","off");
                   position.setAc(2);
                   
                } else if(input == 2){
                   position.set("ac", "on");
                   position.setAc(1); 
                   
               } else if(input == 0){
                   position.set("ac","off");
                   position.setAc(2);
               }
               return position;
                   
                   
           case 57:
                 if(input == 4){
                    
                     position.set("ac", "on");
                     position.setAc(1); 
                    } else if(input == 0){
                   position.set("ac","off");
                   position.setAc(2);

               }
               return position;
//               
           case 59:
                   position.set("power", "fail");
                   return position;
           
           case 60:
                   position.set("battery", "restore");
                   return position;
                   
           case 61:
                   position.set("power", "remove");
                   return position;
                  
                    
           case 103:
                   position.set("speed", "overspeed");
                   return position;
                   
           case 101:
                   position.set("engine_idle","start");
                   return position;
                   
           case 102:
                    position.set("engine_idle","stop");
                    return position;
          
           case 112:
                   position.set("motion", "detection");
                   return position;
         
           case 113:
                   position.set("hard", "acceleration");
                   return position;
        
           case 114:
                   position.set("break", "braking");
                   return position;
         
           case 115:
                   position.set("alarm", "HARD_CORNERING");
                   return position;
          
           case 117:
                   position.set("impact", "detection");
                   return position;
//                  
           default:
//               System.out.println("..no.. alert");
               return position;
       }

    }

    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
        
        System.out.println((String) msg);
        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }
        
            Position position = new Position();
            int type = parser.nextInt();

         position.set(Position.KEY_TYPE, type);
         position.set(Position.KEY_INDEX, parser.nextInt());
         String id = parser.next();

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress,id);
        if (deviceSession == null) {
            return null;
        }
//        System.out.println("org.traccar.protocol.AlematicsProtocolDecoder.decode()");
        position.setProtocol(getProtocolName());
        System.out.println("AlematicsProtocolDecoder");

           SocketAddress data = channel.localAddress();
              InetSocketAddress address = (InetSocketAddress) data;
               int port = address.getPort();
        System.out.println("port"+port);
             position.setPort(port);
        position.setDeviceId(getDeviceId());
        position.setBusiness_device_id(Context.getDataManager().getBusinessDeviceId((int)getDeviceId()));

        position.setTime(parser.nextDateTime());
        Date deviceTime = parser.nextDateTime();
        
        double latitude = parser.nextDouble(0);
        position.setLatitude(latitude);
         double longtitude = parser.nextDouble(0);
        position.setLongitude(longtitude);

        position.setValid(true);
      
        double speed = UnitsConverter.knotsFromKph(parser.nextInt(0)) * 2;
        position.setSpeed(speed);
//        System.out.println("speed------------>"+speed);
        position.setCourse(parser .nextInt(0));
        position.setAltitude(parser.nextInt(0));

        position.set(Position.KEY_HDOP, parser.nextDouble());
        position.set(Position.KEY_SATELLITES, parser.nextInt());
              int input = parser.nextInt();
//              System.out.println("input--------->"+input);
              int output = parser.nextInt();
             
      //      position.set(Position.KEY_INPUT, parser.nextInt());
      //      position.set(Position.KEY_OUTPUT, parser.nextInt());
              position.set(Position.KEY_INPUT, input);
              position.set(Position.KEY_OUTPUT, output);
            double fuel = parser.nextDouble();
            position.setFuel((int) fuel);
        position.set(Position.PREFIX_ADC + 1, fuel);
        position.set(Position.KEY_POWER, parser.nextDouble());
         double odometer = parser.nextDouble()/1000;
//         System.out.println("odometer--------->"+odometer);
              position.set(Position.KEY_ODOMETER, odometer);
              
              position.setMileage(odometer);

        if (parser.hasNext()) {
            position.set("text", parser.next());
        } else if (parser.hasNext()) {
            decodeExtras(position, parser);
        }
          decodeAlarm(type, input, output, position);

        return position;
    }


}