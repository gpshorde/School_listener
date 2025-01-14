/*
 * Copyright 2013 - 2018 Anton Tananaev (anton@traccar.org)
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
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.net.InetSocketAddress;
import org.traccar.BaseHttpProtocolDecoder;
import org.traccar.DeviceSession;
import org.traccar.Protocol;
import org.traccar.helper.DateUtil;
import org.traccar.model.CellTower;
import org.traccar.model.Network;
import org.traccar.model.Position;
import org.traccar.model.WifiAccessPoint;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.joda.time.format.ISODateTimeFormat;
import org.traccar.Context;


public class OsmAndProtocolDecoder extends BaseHttpProtocolDecoder {

    public OsmAndProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        FullHttpRequest request = (FullHttpRequest) msg;
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = decoder.parameters();
        if (params.isEmpty()) {
            decoder = new QueryStringDecoder(request.content().toString(StandardCharsets.US_ASCII), false);
            params = decoder.parameters();
        }

       Position position = new Position();
        position.setProtocol(getProtocolName());
         SocketAddress data = channel.localAddress();
              InetSocketAddress address = (InetSocketAddress) data;
               int port = address.getPort();
             position.setPort(port);
        position.setValid(true);
        
          String token="";
        Integer user_id=0;

        Network network = new Network();

        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            for (String value : entry.getValue()) {
                
                switch (entry.getKey()) {
                      case "token":
                       
                           token=value;	
//                              System.out.println("token------------>"+token);
                           break;
                     case "user_id":
                       
            	           user_id=Integer.parseInt(value);
//                              System.out.println("user_id------------>"+user_id);
            	           break;
                    case "id":
                    case "deviceid":
//                            System.out.println("imei------------>"+value);
                        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, value);
                        if (deviceSession == null) {
                            sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
                            return null;
                        }
                        position.setDeviceId(deviceSession.getDeviceId());
                        position.setBusiness_device_id(Context.getDataManager().getBusinessDeviceId((int)getDeviceId()));
                        break;
                    case "valid":
                        position.setValid(Boolean.parseBoolean(value) || "1".equals(value));
                        break;
                    case "timestamp":
                        try {
                            long timestamp = Long.parseLong(value);
                            if (timestamp < Integer.MAX_VALUE) {
                                timestamp *= 1000;
                            }
//                             System.out.println("time------->timestamp");
                            position.setTime(new Date(timestamp));
                        } catch (NumberFormatException error) {
                            if (value.contains("T")) {
//                                position.setTime(DateUtil.parseDate(value));
                                   position.setTime(new Date(
                                    ISODateTimeFormat.dateTimeParser().parseMillis(value)));  
                                
                            } else {
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                 
                                position.setTime(dateFormat.parse(value));
                            }
                        }
                        break;
                    case "lat":
                        position.setLatitude(Double.parseDouble(value));
                        break;
                    case "lon":
                        position.setLongitude(Double.parseDouble(value));
                        break;
                    case "location":
                        String[] location = value.split(",");
                        position.setLatitude(Double.parseDouble(location[0]));
                        position.setLongitude(Double.parseDouble(location[1]));
                        break;
                    case "cell":
                        String[] cell = value.split(",");
                        if (cell.length > 4) {
                            network.addCellTower(CellTower.from(
                                    Integer.parseInt(cell[0]), Integer.parseInt(cell[1]),
                                    Integer.parseInt(cell[2]), Integer.parseInt(cell[3]), Integer.parseInt(cell[4])));
                        } else {
                            network.addCellTower(CellTower.from(
                                    Integer.parseInt(cell[0]), Integer.parseInt(cell[1]),
                                    Integer.parseInt(cell[2]), Integer.parseInt(cell[3])));
                        }
                        break;
                    case "wifi":
                        String[] wifi = value.split(",");
                        network.addWifiAccessPoint(WifiAccessPoint.from(
                                wifi[0].replace('-', ':'), Integer.parseInt(wifi[1])));
                        break;
                    case "speed":
//                        position.setSpeed(convertSpeed(Double.parseDouble(value), "kn"));
//                        
                    position.setSpeed(Double.parseDouble(value));
//                     System.out.println("speed------>"+Double.parseDouble(value));
//                          position.setSpeed(Double.parseDouble(value));
                        break;
                    case "bearing":
                    case "heading":
                        position.setCourse(Double.parseDouble(value));
                        break;
                    case "altitude":
                        
                        position.setAltitude(Double.parseDouble(value));
                        break;
                    case "accuracy":
                         position.set("accuracy", Double.parseDouble(value));
                        position.setAccuracy(Double.parseDouble(value));
                        break;
                    case "hdop":
                        position.set(Position.KEY_HDOP, Double.parseDouble(value));
                        break;
                    case "batt":
                        position.set(Position.KEY_BATTERY_LEVEL, Double.parseDouble(value));
                         position.setBattery(Double.parseDouble(value));
                        break;
                    case "driverUniqueId":
                        position.set(Position.KEY_DRIVER_UNIQUE_ID, value);
                        break;
                    case "alarm":
                	if(value.equals("lowBattery")){
                		position.set("alarm", Position.ALARM_LOW_BATTERY);
                	}else{
                		position.set("alarm", Position.ALARM_SOS);
//                		System.out.println("Alarm sos pressed");
                	}
                	break;
                    default:
                        try {
                            position.set(entry.getKey(), Double.parseDouble(value));
                        } catch (NumberFormatException e) {
                            switch (value) {
                                case "true":
                                    position.set(entry.getKey(), true);
                                    break;
                                case "false":
                                    position.set(entry.getKey(), false);
                                    break;
                                default:
                                    position.set(entry.getKey(), value);
                                    break;
                            }
                        }
                        break;
                }
            }
        }

//        if (position.getFixTime() == null) {
//  
//            position.setTime(new Date());
//        }

        if (network.getCellTowers() != null || network.getWifiAccessPoints() != null) {
            position.setNetwork(network);
        }

        if (position.getLatitude() == 0 && position.getLongitude() == 0) {
            getLastLocation(position, position.getDeviceTime());
        }

        if (position.getDeviceId() != 0) {
            sendResponse(channel, HttpResponseStatus.OK);
           
        } else {
            sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
            return null;
        }
//          System.out.println("token------------>"+token);
//                System.out.println("user_id------------>"+user_id);
        
         if(!checkAuthorization(token, user_id)){
//             System.out.println("token if------------>"+token);
//                System.out.println("user_id if------------>"+user_id);
//        	System.out.println("******* Authentication Faild *******"); // Test. 
        	HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.UNAUTHORIZED);
            channel.write(response).addListener(ChannelFutureListener.CLOSE);
            return null;
            
    	}
        if (channel != null) {
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.OK);
            channel.write(response).addListener(ChannelFutureListener.CLOSE);
        }
//        System.out.println(" ");
//        	System.out.println("******* ****************************** *******");
//                System.out.println(" ");
                   
    return position;
    }

}
