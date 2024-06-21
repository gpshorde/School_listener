/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.traccar.protocol;

import io.netty.channel.Channel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.traccar.BaseProtocolDecoder;
import org.traccar.Context;
import org.traccar.DeviceSession;
import org.traccar.Protocol;
import org.traccar.helper.DistanceCalculator;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.Position;

/**
 *
 * @author vishal
 */
public class TeltonikaProtocolDecoder extends BaseProtocolDecoder {

    public TeltonikaProtocolDecoder(Protocol protocol) {
        super(protocol);
    }
    
//      private Position getLastPosition(long deviceId) {
//            if(Context.getConnectionManager() != null ){
//                return Context.getConnectionManager().getLastPosition(deviceId);
//            }
//            return null;
//        }
    
    
       private Position decodeAlarm(String status,int input , Position position) {
       // Position last = getLastPosition(position.getDeviceId());

        switch (status) {
            case "WD":
            case "IN":
                   position.set("ignition_on", "yes");
                   position.setIgnition(1);
                   position.setTrip(1);
                   return position;
            case "IF":
                   position.set("ignition_off", "yes");
                   position.setIgnition(2);
                   position.setTrip(2);
                   return position;
   
            case "EA":
                position.set("alarm", Position.ALARM_SOS);
                return position;
            case "BL":
                 position.set("battery", Position.ALARM_LOW_BATTERY);
                return position;
            case "HB":
                 position.set("break", Position.ALARM_BRAKING);
                return position;
            case "HA":
                 position.set("hard", Position.ALARM_ACCELERATION);
                return position;
            case "RT":
                 position.set("alarm", Position.ALARM_CORNERING);
                return position;
            case "OS":
                 position.set("speed", Position.ALARM_OVERSPEED);
                return position;
            case "TA":
                 position.set("temper", Position.ALARM_TAMPERING);
                return position;
            case "BD":
                 position.set("alarm", Position.ALARM_POWER_CUT);
                 return position;
            case "BR":
                 position.set("power", Position.ALARM_POWER_RESTORED);
                 return position;
            case "DT":
                 
                if(input == 12)
                {
                    position.set("ac", "off");
                    position.setAc(2);
                }
            
                 return position;
              case "NR":
              //    if(last != null)
               //   {
                  //   Map<String, Object> attributes = last.getAttributes();
            
                
           // Object get_input = attributes.get("input");
                  //    System.out.println("gps data--->"+get_input);
//                   long i = (Long) get_input;
                //   int in =Integer.parseInt(get_input.toString());
//
//              if(input == 13)
//              {
//                  if(input != in)
//                {
//                    position.set("ac", "on");
//                    position.setAc(1);
//                }
//               }
//                  }
                 return position;
            default:
                return null;
        }
    }
    
   
    @Override
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
    
           Position position=new Position();
		
           String data = (String) msg;
             System.out.println("split-------->"+data);
		
           if(data.startsWith("$TEL123"))
           {
                String[] splitdata=data.split(",");
                System.out.println("split-------->"+splitdata.length);
                    
                if(splitdata.length > 10 && splitdata.length < 13 )
                {
                
                  String Vendor_id = splitdata[1];
                  System.out.println("---Vendor_id----"+Vendor_id);
        
                  String vechicle_number = splitdata[2];
                  System.out.println("---vechicle_number----"+vechicle_number);
            
                  String imei = splitdata[3];
                  System.out.println("---imei----"+imei);
              
                  DeviceSession deviceSession = getDeviceSession(channel, remoteAddress,imei);
                  if (deviceSession == null) {
                    return null;
                  }
               
                  position.setProtocol(getProtocolName());
                  SocketAddress data1 = channel.localAddress();
              InetSocketAddress address = (InetSocketAddress) data1;
               int port = address.getPort();
             position.setPort(port);

	          System.out.println("Protocol name===="+position.getProtocol());
	       //   position.setBusiness_device_id(Context.getDataManager().getBusinessDeviceId((int)getDeviceId()));

                  position.setDeviceId(getDeviceId());
		  System.out.println("Device Imei Number===="+position.getDeviceId());
               
                  String FirmwareVersion = splitdata[4];
                  System.out.println("---FirmwareVersion----"+FirmwareVersion);
               
                  String ProtocolVersion = splitdata[5];
                  System.out.println("---ProtocolVersion----"+ProtocolVersion);
               
                  double latitude =  Double.parseDouble(splitdata[6]);
                  position.setLatitude(latitude);
                  System.out.println("---latitude----"+latitude);
             
                  String latitude_dir =splitdata[7];
                  System.out.println("---latitude_dir----"+latitude_dir);
            
                  double longtude = Double.parseDouble(splitdata[8]);
                  position.setLongitude(longtude);
                  System.out.println("---longtude----"+longtude);
           
                  String longtitude_dir =splitdata[9];
                  System.out.println("---longtitude_dir----"+longtitude_dir);
                  position.setAltitude(0);
               
                }else if((splitdata.length > 50 && splitdata.length < 55))
                { 
                           
                   String Vendor_id = splitdata[1];
                   System.out.println("---Vendor_id----"+Vendor_id);
                     
                   String imei = splitdata[6];
                   System.out.println("---imei----"+imei);
              
                   DeviceSession deviceSession = getDeviceSession(channel, remoteAddress,imei);
                   if (deviceSession == null) {
                     return null;
                   }
               SocketAddress data2 = channel.localAddress();
              InetSocketAddress address = (InetSocketAddress) data2;
               int port = address.getPort();
             position.setPort(port);
                   position.setProtocol(getProtocolName());

	           System.out.println("Protocol name===="+position.getProtocol());
	      //     position.setBusiness_device_id(Context.getDataManager().getBusinessDeviceId((int)getDeviceId()));

                   position.setDeviceId(getDeviceId());
//	
                   
                   String FirmwareVersion = splitdata[2];
                   System.out.println("---FirmwareVersion----"+FirmwareVersion);
               
                   String packet_type = splitdata[3];
                                      System.out.println("---packet_type----"+packet_type);

                                    
                   int message_id = Integer.parseInt(splitdata[4]);
                   System.out.println("---message_id----"+message_id);
               
                   String packet_status = splitdata[5];
                   System.out.println("---packet_status----"+packet_status);
               
                   String vechicle_number = splitdata[7];
                   System.out.println("---vechicle_number----"+vechicle_number);
               
                
                   int gps_fix = Integer.parseInt(splitdata[8]);
                   System.out.println("---gps_fix----"+gps_fix);
               
                   String date =  splitdata[9]+ splitdata[10];
               
//                   DateFormat DF = new SimpleDateFormat("yyMMddHHmmss");
                   DateFormat DF = new SimpleDateFormat("ddMMyyyyHHmmss");
                
                  position.setTime(DF.parse(date));
                  System.out.println("---date----"+DF.parse(date));
          
                double latitude =  Double.parseDouble(splitdata[11]);
                position.setLatitude(latitude);
                System.out.println("---latitude----"+latitude);
         
                String latitude_dir =splitdata[12];
                System.out.println("---latitude_dir----"+latitude_dir);
         
               double longtude = Double.parseDouble(splitdata[13]);
               position.setLongitude(longtude);
                System.out.println("---longtude----"+longtude);
             
                String longtitude_dir =splitdata[14];
                System.out.println("---longtitude_dir----"+longtitude_dir);
                position.setAltitude(0);
            
               double speed =UnitsConverter.knotsFromKph(Double.parseDouble(splitdata[15]) * 2);
               position.setSpeed(speed);
                 System.out.println("---speed----"+speed);
           
                 double course = Double.parseDouble(splitdata[16]);
               position.setCourse(course);
                System.out.println("---course----"+course);
               position.set(Position.KEY_SATELLITES,splitdata[17] );
           
               double altitude = Double.parseDouble(splitdata[18]);
               position.setAltitude(altitude);
           
               double pdop = Double.parseDouble(splitdata[19]);
                 position.set("pdop",pdop );
                    System.out.println("---pdop----"+pdop);
          
                    double hdop = Double.parseDouble(splitdata[20]);
                  position.set("hdop", hdop );
                     System.out.println("---hdop----"+hdop);
          
                     String operator_name =splitdata[21];
                  position.set("operator",operator_name);
                    System.out.println("---operator_name----"+operator_name);
               
                 int ignition = Integer.parseInt(splitdata[22]);
                
                  System.out.println("---ignition----"+ignition);
                   if(ignition == 1){
                   position.set("ignition_on", "yes");
                   position.setIgnition(1);
                   position.setTrip(1);
                  } else if(ignition == 0){
                   position.set("ignition_off","yes");
                   position.setIgnition(2);
                   position.setTrip(2);
                  }
                   
                     int main_power = Integer.parseInt(splitdata[23]);
                  System.out.println("---main_power----"+main_power);
                   if(main_power == 1){
                   position.set("battery", "connected");
             
                  } else if(main_power == 0){
                   position.set("battery","disconnected");
              
                  }
                   
                   
                       double power = Double.parseDouble(splitdata[24]);
                  position.set("power", power );
                     System.out.println("---power----"+power);
                     
                   double battery = Double.parseDouble(splitdata[25]);
                    System.out.println("---battery----"+battery);
                   position.setBattery(battery);
                   
                  int emergencyStatus = Integer.parseInt(splitdata[26]);
                  System.out.println("---emergencyStatus----"+emergencyStatus);
                   if(emergencyStatus == 1){
                   position.set("emergency", "on");
             
                  } else if(emergencyStatus == 0){
                   position.set("emergency","off");
              
                  }
                   
                    String tamper_alert =splitdata[27];
                  position.set("tamper_alert",tamper_alert);
                    System.out.println("---tamper_alert----"+tamper_alert);
                    
                    int gsm_strength = Integer.parseInt(splitdata[28]);
                    System.out.println("---gsm_strength----"+gsm_strength);
                    
                      int MCC = Integer.parseInt(splitdata[29]);
                       position.set("MCC",MCC);
                    System.out.println("---MCC----"+MCC);
                    
                      int MNC = Integer.parseInt(splitdata[30]);
                       position.set("MNC",MNC);
                    System.out.println("---MNC----"+MNC);
                    
                        String LAC =splitdata[31];
                  position.set("LAC",LAC);
                    System.out.println("---LAC----"+LAC);
                    
                     String input_hex =splitdata[45];
                     int input = Integer.parseInt(input_hex,2);
                  position.set("input",input);
                    
                     String output_hex =splitdata[46];
                   int output = Integer.parseInt(output_hex,2);
                  position.set("output",output);
                    System.out.println("---output----"+output);
                    
                      decodeAlarm(packet_type,input, position);
                    
                     double adc1 = Double.parseDouble(splitdata[48]);
                  position.set("adc1", adc1 );
                     System.out.println("---adc1----"+adc1);
          
                       double adc2 = Double.parseDouble(splitdata[49]);
                  position.set("adc2", adc2 );
                     System.out.println("---adc2----"+adc2);
                     
                     
                       double odometer = Double.parseDouble(splitdata[50]);
                       position.set("odometer", odometer);
                      
//                  position.setMileage(odometer);      // update at 14/6 @vishal

//                    double last_mileage = 0.0;
//
//                    Position last = getLastPosition(position.getDeviceId());
//
//                    last_mileage = last.getMileage() + odometer;
//
//                    position.setMileage(last_mileage);
////                     System.out.println("---odometer----"+odometer);


//                 change mileage with distance                  // update at 01/07 @vishal

//                    double last_mileage = 0.0;                  
//
//                    Position last = getLastPosition(position.getDeviceId());
//                    
//                    if(last != null)
//                    {
//                        last_mileage = last.getMileage() + odometer;
////                         System.out.println("mileage last--------->"+last_mileage);
//                        position.setMileage(last_mileage);
//                    }else{
//                         
//                        position.setMileage(odometer);
//                    }
               
                    calculatedmileage(position);
                  return position;
               }else if((splitdata.length > 16 && splitdata.length < 21))
                { 
                     String message_type = splitdata[2];
                       String imei = splitdata[3];
                   System.out.println("---imei----"+imei);
              
                   DeviceSession deviceSession = getDeviceSession(channel, remoteAddress,imei);
                   if (deviceSession == null) {
                     return null;
                   }
               
            SocketAddress data3 = channel.localAddress();
              InetSocketAddress address = (InetSocketAddress) data3;
               int port = address.getPort();
             position.setPort(port);                  
             position.setProtocol(getProtocolName());

	           System.out.println("Protocol name===="+position.getProtocol());
//	           position.setBusiness_device_id(Context.getDataManager().getBusinessDeviceId((int)getDeviceId()));

                   position.setDeviceId(getDeviceId());

                    String status = splitdata[4];
                    
                     String date =  splitdata[5];
               
                    DateFormat DF = new SimpleDateFormat("yyMMddHHmmss");
                
                    position.setTime(DF.parse(date));
                    
                 double latitude =  Double.parseDouble(splitdata[7]);
                position.setLatitude(latitude);
                System.out.println("---latitude----"+latitude);
         
                String latitude_dir =splitdata[8];
                System.out.println("---latitude_dir----"+latitude_dir);
         
               double longtude = Double.parseDouble(splitdata[9]);
               position.setLongitude(longtude);
                System.out.println("---longtude----"+longtude);
             
                String longtitude_dir =splitdata[10];
                System.out.println("---longtitude_dir----"+longtitude_dir);
                position.setAltitude(0);
            
                double speed =Double.parseDouble(splitdata[12]) * 3.6;
                position.setSpeed(speed);
                 System.out.println("---speed----"+speed);
                   
                    
                }
                    
                
                
           }
    return null;

	}

       
     private Position calculatedmileage (Position position) {
            
            double mileage = 0.0;
            double last_mileage = 0.0;
             double total_mileage = 0.0;
            
       //     Position last = getLastPosition(position.getDeviceId());
         //   if(last != null) {

              //  mileage += DistanceCalculator.distance(position.getLatitude(), position.getLongitude(),
                    //    last.getLatitude(), last.getLongitude());
               
//               mileage = BigDecimal.valueOf(mileage).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                mileage = BigDecimal.valueOf(mileage).setScale(2, RoundingMode.FLOOR).doubleValue();
                
                System.out.println(".........______Mileage_____........"+mileage);
             //   System.out.println(".........______last Mileage_____........"+last.getMileage());
                System.out.println(".........______total Mileage first_____........"+last_mileage);

          last_mileage =  mileage * 0.001;
          //        total_mileage  = last.getMileage() + last_mileage;
                    
            position.setMileage(total_mileage);
             System.out.println(".........______total Mileage seconds_____........"+last_mileage);
            position.set("mileage", last_mileage);
              position.set("total_mileage", total_mileage);
                           System.out.println(".........______total Mileage last_____........"+last_mileage);

          //  }else{
         //          position.setMileage(0);
           // }
            
                 
            return position ;
        }


}
