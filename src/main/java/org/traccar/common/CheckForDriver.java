//package org.traccar.common;
//
//import com.vividsolutions.jts.geom.Coordinate;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.text.DecimalFormat;
//import java.util.Date;
//import org.traccar.model.Alert;
//import com.vividsolutions.jts.geom.Geometry;
//import com.vividsolutions.jts.io.ParseException;
//import com.vividsolutions.jts.io.WKTReader;
//import org.traccar.Context;
//
//
//
//public class CheckForDriver {
//
////	static String url=StaticVariable.DBURL;
////	static String user=StaticVariable.DBUSER;
////	static String password=StaticVariable.DBPASSWORD;
//
//
//	public static int checkGeo(String gps_location, int device_id, Alert alert, PreparedStatement statement,Connection connection) throws SQLException {
//
//		int result=0;
//             
//		Geometry geom = wktToGeometry(gps_location);
//		if(geom.getCoordinate().x==0.0 && geom.getCoordinate().y==0){
//			return result;
//		}
//
//		//Connection connection=null;
//		//PreparedStatement preparedStatement=null;
//		try{
//			
//			//connection = DriverManager.getConnection(url, user, password);
//			System.out.println("---------latitude----------------"+geom);
//			System.out.println("---------logintude----------------"+geom.getCoordinate().y);
////			System.out.println("---------bussiness_device_id----------------"+device_id);
//                        
//
//                         String qry = "select cb.co_booking_tripmaster_id,st_dwithin(cb.route,\n" +
//                        "ST_GeogFromText('SRID=4326;POINT("+geom.getCoordinate().y+" "+geom.getCoordinate().x+")'),\n" +
//                        "100) as nearMyPoint from web.co_booking_trip_master cb\n" +
//                        "inner join web.vehicle v ON v.vehicle_id = cb.vehicle_vehicle_id\n" +
//                        " inner join web.business_device bd ON bd.business_device_id = v.business_device_business_device_id and bd.is_active =true\n" +
//                        " and bd.business_device_id = ?\n" +
//                        " where cb.is_active =true and  cb.check_trip = 1";
//			
//                         statement=connection.prepareStatement(qry);
//			statement.setInt(1, device_id);
//			ResultSet rs=statement.executeQuery();
//			while(rs.next()){
//
//				int booking_id=rs.getInt("co_booking_tripmaster_id");
//			
//                           
//                           String nearMyPoint =rs.getString("nearMyPoint");
//                      System.out.println("Radius  is---"+nearMyPoint);
//                       System.out.println("Radbooking id---"+booking_id);
//
//
//                           if(nearMyPoint.equals("t"))
//                           {
// 
//                              	result=36;
//				makeSingleEntryForGeofence(result, device_id, alert.getObservationid(), alert.getGps_time(),gps_location,booking_id,connection);	
//						
//					
//                           }
//                            if(nearMyPoint.equals("f"))
//                           {
//                            
//                               result=37;
//				 makeSingleEntryForGeofence(result, device_id, alert.getObservationid(), alert.getGps_time(),gps_location,booking_id,connection);	
//	
//                           }
//
//				
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//		}finally {
//			if (connection!=null) {
//				//statement.close();
//				//connection.close();
////				System.out.println("connection was closed now.");
//			}
//		}
//		
//
//
//		return result;
//
//	}
//        
//       
//
//	private static void makeSingleEntryForGeofence(int result, int device_id, int observationid, Date gps_time,String gps_location,int booking_id,Connection connection) throws SQLException {
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
//				preparedStatement.setInt(2, 36);
//				preparedStatement.setInt(3, 37);
//				preparedStatement.setInt(4, booking_id);
//				ResultSet rs1=preparedStatement.executeQuery();
//				if(rs1!=null && rs1.next()){
////					System.out.println("message is--------------------------------------------"+rs1.getInt("altype"));
////					System.out.println("message is--------------------from method------------------------"+result);
//					if(rs1.getInt("altype")==result){
////						System.out.println("Record not inserted for geofence............... alert");
//						//break;
//					}
//					else{
//						alert.setKey_id(booking_id);
//						alert.setBusiness_device_id(device_id);
//						alert.setObservationid(observationid);
//						alert.setGps_time(gps_time);
//						alert.setGps_location(gps_location);
//                                                alert.setAl_type(result);
//						Context.getDataManager().addAlerts(alert);
////						System.out.println("Record inserted............... alert");
//					}
//				}else{
////					System.out.println("---------First-Time---------Insert--------------come into else part---"+geofence_id+"-"+device_id+"-"+gps_time+"-"+result+"-"+observationid);
//					alert.setKey_id(booking_id);
//					alert.setBusiness_device_id(device_id);
//					alert.setObservationid(observationid);
//					alert.setGps_time(gps_time);
//                                        alert.setGps_location(gps_location);
//                                        alert.setAl_type(result);
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
//
//	public static Geometry wktToGeometry(String wktPoint)
//	{
//		WKTReader fromText = new WKTReader();
//		Geometry geometry = null;
//		try
//		{geometry = fromText.read(wktPoint);}
//		catch (ParseException e)
//		{throw new RuntimeException("Not a WKT string." + wktPoint);}
//		return geometry;
//	}
//
//	private static boolean pointInPolygon(Coordinate[] coordinates, double x, double y) {
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
//	public static double distFromTwoPoint(double circle_x, double circle_y, double x, double y) {
//		double earthRadius = 6371000; //meters
//		double dLat = Math.toRadians(x-circle_x);
//		double dLng = Math.toRadians(y-circle_y);
//		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
//				Math.cos(Math.toRadians(circle_x)) * Math.cos(Math.toRadians(x)) *
//				Math.sin(dLng/2) * Math.sin(dLng/2);
//		
//		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//		float dist = (float) (earthRadius * c);
//
//		return dist;
//	}	
//}
