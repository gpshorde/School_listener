///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.traccar.common;
//
//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.Geometry;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.Date;
//import org.traccar.Context;
//import static org.traccar.common.CheckForGeoFence.checkForCircle;
//import static org.traccar.common.CheckForGeoFence.distFromTwoPoint;
//import static org.traccar.common.CheckForGeoFence.wktToGeometry;
//import org.traccar.model.Alert;
//
///**
// *
// * @author addon10
// */
//public class CheckForFranchise {
//    
//     public static int getFranchise(String gps_location, int device_id, Alert alert, PreparedStatement statement,Connection connection) throws SQLException{
//       
//            int result=0;
//                try{
////                       System.out.println(".........  Your Business DEvice ID is ............... "  + device_id);
////comment following line advance package 
//                        String query = "select packages_package_id from web.business_device where business_device_id = "+device_id+"";
//                        statement=connection.prepareStatement(query);
//			ResultSet rs=statement.executeQuery();
//                        while(rs.next()){    
////                            System.out.println("");
//                            int aInt = rs.getInt("packages_package_id");
////                             System.out.println("..............  business device _id ............ " + device_id);
////                              System.out.println("..............  packages_package_id ............ " + aInt);
////                            System.out.println("----------------    package is - ---------------------- "  + aInt );
//                            if(aInt!=4){
////                             
//                               return result ;
//                            } 
//                       }
////                            System.out.println("........ GPS LOCATION ......." + gps_location);
//
//
//                            Geometry geom = wktToGeometry(gps_location);
////                            System.out.println(".............      geom.x  ........    " + geom.getCoordinate().x);
////                            System.out.println(".............      geom.y  ........    " + geom.getCoordinate().y);
//                            if(geom.getCoordinate().x==0.0 && geom.getCoordinate().y==0){
//                                    return result;
//                            }
////                            System.out.println("---------bussiness_device_id----------------"+device_id);
//                            String qry="select bd.business_device_id,f.franchiseId,f.circle_distance,f.franchisename,st_astext(f.location) as gps_location from web.franchise f \n" +
//                                       " inner join web.business_device bd ON (bd.business_business_id = f.business_business_id and bd.is_active =true and f.is_active=true) where bd.business_device_id=?"; 
//                            statement=connection.prepareStatement(qry);
//                            statement.setInt(1, device_id);
//                            ResultSet rs1 = statement.executeQuery();
//                            while(rs1.next()){
////				
//				 int franchiseId=rs1.getInt("franchiseId");
//                                 if(rs1.getDouble("circle_distance")!=0){
////					System.out.println("Radius values is-----------------------------"+rs.getDouble("circle_distance"));
//				}
////                               System.out.println("..............  Booking Geofence ID ............ " + franchiseId);
//                                
//                            
////                                	if(rs.getString("gps_location").contains("POINT") && rs.getString("gps_location").startsWith("POINT")){
////					double circle_distance=rs.getDouble("circle_distance");
////					Geometry geometry=wktToGeometry(rs.getString("gps_location"));
////					boolean check_for_circle=checkForCircle(geometry.getCoordinate().y,geometry.getCoordinate().x,geom.getCoordinate().y,geom.getCoordinate().x,circle_distance);
////					//System.out.println("check for the value"+checkForCircle(geometry.getCoordinate().y,geometry.getCoordinate().x,geom.getCoordinate().y,geom.getCoordinate().x,circle_distance));
////					if(check_for_circle==false){
//////						System.out.println("yes geofence exited now circle.");
////						result=35;
////						makeSingleEntryForGeofence(result, device_id, alert.getObservationid(), alert.getGps_time(),franchiseId,connection);
////					}else{
////						result=34;
////						makeSingleEntryForGeofence(result, device_id, alert.getObservationid(), alert.getGps_time(),franchiseId,connection);
////					}
////				} 
////System.out.println("franchiseId----->"+rs1.getInt("franchiseId"));
////System.out.println("gps_location----->"+rs1.getString("gps_location"));
////                                        
//                                        if(rs1.getString("gps_location").contains("POINT") && rs1.getString("gps_location").startsWith("POINT")){
//					double circle_distance=rs1.getDouble("circle_distance");
//					Geometry geometry=wktToGeometry(rs1.getString("gps_location"));
//					boolean check_for_circle=checkForCircle(geometry.getCoordinate().y,geometry.getCoordinate().x,geom.getCoordinate().y,geom.getCoordinate().x,circle_distance);
//					//System.out.println("check for the value"+checkForCircle(geometry.getCoordinate().y,geometry.getCoordinate().x,geom.getCoordinate().y,geom.getCoordinate().x,circle_distance));
//					if(check_for_circle==false){
////						System.out.println("yes geofence exited now circle.");
//						result=35;
//						makeSingleEntryForGeofence(result, device_id, alert.getObservationid(), alert.getGps_time(),franchiseId,connection);
//					}else{
//						result=34;
//						makeSingleEntryForGeofence(result, device_id, alert.getObservationid(), alert.getGps_time(),franchiseId,connection);
//					}
//				}
//				else{
//					Geometry geometry=wktToGeometry(rs1.getString("gps_location"));
//					boolean check_for_poly=pointInPolygon(geometry.getCoordinates(),geom.getCoordinate().x,geom.getCoordinate().y);
//				//	System.out.println("check for the value"+pointInPolygon(geometry.getCoordinates(),geom.getCoordinate().x,geom.getCoordinate().y));
//					if(check_for_poly==false){
////						System.out.println("yes geofence exited now Polygon");
//						result=35;
//						//PushNotification.callPushNotification(device_id,4);
//						//makeSingleEntryForGeofence(result, device_id, alert.getObservationid(), alert.getGps_time(),geofence_id);
//						makeSingleEntryForGeofence(result, device_id, alert.getObservationid(), alert.getGps_time(),franchiseId,connection);	
//						
//					}else{
//						result=34;
//						//makeSingleEntryForGeofence(result, device_id, alert.getObservationid(), alert.getGps_time(),geofence_id);
//						//PushNotification.callPushNotification(device_id,5);
////						System.out.println("yes geofence enter now Polygon");
//						makeSingleEntryForGeofence(result, device_id, alert.getObservationid(), alert.getGps_time(),franchiseId,connection);
//					}
//				}
//			}
//          	}catch(Exception e){
//			e.printStackTrace();
//		  }
//                finally {
//			if (connection!=null) {
//				//statement.close();
//				//connection.close();
////				System.out.println("connection was closed now.");
//			}
//		}
//		
//                return result;
//   }
//     
//     private static void makeSingleEntryForGeofence(int result, int device_id, int observationid, Date gps_time,int franchiseId,Connection connection) throws SQLException {
////		Connection connection=null;
//		PreparedStatement preparedStatement=null;
//		Alert alert=new Alert();
//		//System.out.println("message is--------------------from method------------------------"+result);
//			try{
//				//connection=dataSource.getConnection("track", "trackio");
////				connection = DriverManager.getConnection(url, user, password);
//				String qry1="select * from web.notification where business_device_id=? AND (altype=? OR altype =?) AND key_id=? order by notification_id DESC limit 1" ;
//				preparedStatement=connection.prepareStatement(qry1);
//				preparedStatement.setLong(1, device_id);
//				preparedStatement.setInt(2, 35);
//				preparedStatement.setInt(3, 34);
//				preparedStatement.setInt(4, franchiseId);
//				ResultSet rs1=preparedStatement.executeQuery();
//				if(rs1!=null && rs1.next()){
////					System.out.println("message is--------------------------------------------"+rs1.getInt("altype"));
////					System.out.println("message is--------------------from method------------------------"+result);
//					if(rs1.getInt("altype")==result){
////						System.out.println("Record not inserted for geofence............... alert");
//						//break;
//					}
//					else{
//						alert.setKey_id(franchiseId);
//						alert.setBusiness_device_id(device_id);
//						alert.setObservationid(observationid);
//						alert.setGps_time(gps_time);
//						alert.setAl_type(result);
//						Context.getDataManager().addAlerts(alert);
////						System.out.println("Record inserted............... alert");
//					}
//				}else{
////					System.out.println("---------First-Time---------Insert--------------come into else part---"+geofence_id+"-"+device_id+"-"+gps_time+"-"+result+"-"+observationid);
//					alert.setKey_id(franchiseId);
//					alert.setBusiness_device_id(device_id);
//					alert.setObservationid(observationid);
//					alert.setGps_time(gps_time);
//					alert.setAl_type(result);
//					Context.getDataManager().addAlerts(alert);
////					System.out.println("Record inserted............... alert");
//				}
//			}catch (Exception e) {
//				e.printStackTrace();
//			}finally {
//				if (connection!=null) {
////					connection.close();
////					System.out.println("connection was closed now. in method entry.");
//				}
//			}
//		
//	}
//     
//     	private static boolean pointInPolygon(Coordinate[] coordinates, double x, double y) {
//		int i;
//		int j;
//		boolean result = false;
//		for (i = 0, j = coordinates.length - 1; i < coordinates.length; j = i++) {
//			if ((coordinates[i].y > y) != (coordinates[j].y > y) &&
//					(x < (coordinates[j].x - coordinates[i].x) * (y - coordinates[i].y) / (coordinates[j].y-coordinates[i].y) + coordinates[i].x)) {
//				result = !result;
//			}
//		}
//		return result;
//	}
//
//	public static boolean checkForCircle(double circle_x, double circle_y, double x, double y, double circle_distance){
//
//		double distance=distFromTwoPoint(circle_x, circle_y, x, y);
//		//Double.parseDouble(new DecimalFormat("##.####").format(distance));
////		System.out.println("Calculation--distance--------------------------------------"+distance);
//		if(distance<=circle_distance){
//			return true;
//		}else{
//			return false;
//		}
//	}
//
//}
//
//     
//
//
//
