package org.traccar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import org.eclipse.jetty.util.UrlEncoded;
import org.glassfish.jersey.internal.inject.Custom;



public class PushNotification 
{
	
	public static void callPushNotification(int bussiness_device_id, int al_type, int key_id){
		try {

			//URL url = new URL("http://52.41.98.178:8080/device_token/SendPushNotification");
			//URL url = new URL("http://52.43.224.3:8080/device_token/SendPushNotification");

			//// Following is the url to test the FCM and the method developed by Kartik for
		    //// sending the push notifications via FCM
//			                 System.out.println("business_device_id"+bussiness_device_id);
//                                         System.out.println("alert name"+al_type);
//                                        System.out.println("key"+key_id);


			//TT Live


			URL url = new URL("https://app.truetrackgps.in:8444/SendPushNotificationFCM");
			//URL url = new URL("http://192.168.0.166:8443/SendPushNotificationFCM");

						//URL url = new URL("http://192.168.0.166:8080/SendPushNotificationFCM");



                        // tt KARTIK URL FOR FCM (82)
//                             URL url = new URL("http://192.168.1.82:9999/SendPushNotificationFCM");
			// Local server


//			System.out.println(" Business Device_ID is -->" + bussiness_device_id);
//			System.out.println("Alerte type is -->"+ al_type);
//			System.out.println("Key ID  is -->"+ key_id);



	/*		Scanner reader = new Scanner(System.in);  // Reading from System.in
			System.out.println("Enter a number: ");
			int n = reader.nextInt(); // Scans the next token of the input as an int.
			//once finished
			reader.close();*/



			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.addRequestProperty("Content-Type", "application/json;charset=UTF-8");
			String input = "{ \"business_device_id\": \""+bussiness_device_id+"\",\"ignition\": \""+al_type+"\",\"key_id\": \""+key_id+"\"}";
			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(input);
			writer.flush();
			writer.close();
			os.close();
			conn.connect();
			
			int responseCode = conn.getResponseCode();
			System.out.println("Response Code is --->"+ responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String line;
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				while ((line = br.readLine()) != null) {
					System.out.println("=====Response from Back End=======> "+line);
				}
			}else{
//				System.out.println("<=====failed To Send Notification=======>");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	public static void PushNotificationSend(String data) throws InvalidSSLConfig, FileNotFoundException, UnsupportedEncodingException{
//		ApnsService service = null;
//		data=URLEncoder.encode(data,"utf-8");
//		/*service = APNS.newService()
//	     		    .withCert(new FileInputStream("/home/ubuntu/iphone_dev.p12"), "admin")
//	     		    .withProductionDestination()
//	     		    .build();
//	     	String token = "828832355dfbf2531e5b79c412d42cb13edc77254a39430771d76ea5144c865b";
//
//	     	String payload = APNS.newPayload().alertBody("Can't be simpler than this!").build();
//	     	service.push(token, payload);
//	     	service.push(token, payload);
//	     	System.out.println("Send successfully.");*/
//
//		// get the certificate
//		ApnsServiceBuilder serviceBuilder = APNS.newService();
//
//		/*	System.out.println("inside developement");
//			String certPath = PushNotification.class.getResource("AddonTrack_Development.p12").getPath();
//			serviceBuilder.withCert(certPath, "admin")
//			.withSandboxDestination();*/
//
//
//		/*serviceBuilder.withCert(new FileInputStream("/home/mahesh/java/sts/l_workspace/Trackio/src/main/java/com/trackio/servicecall/AddonTrack_Development.p12"), "admin")
//            .withSandboxDestination();*/
//
//		serviceBuilder.withCert(new FileInputStream("/home/mahesh/java/sts/trackio_Workspace/TrackIO/src/main/java/com/track/io/controller/iphone_dev.p12"), "admin")
//		.withProductionDestination();
//
//
//
//
//
//		service = serviceBuilder.build();
//		// or
//		// service = APNS.newService().withCert(certStream,
//		// "your_cert_password").withProductionDestination().build();
//		//service.start();
//		// You have to delete the devices from you list that no longer
//		//have the app installed, see method below
//
//		// read your user list
//
//		try {
//
//			// we had a daily update here, so we need to know how many
//			//days the user hasn't started the app
//			// so that we get the number of updates to display it as the badge.
//			//int days = (int) ((System.currentTimeMillis() - user.getLastUpdate()) / 1000 / 60 / 60 / 24);
//			PayloadBuilder payloadBuilder = APNS.newPayload();
//			payloadBuilder = payloadBuilder.alertBody(data);
//			// check if the message is too long (it won't be sent if it is)
//			//and trim it if it is.
//			if (payloadBuilder.isTooLong()) {
//				payloadBuilder = payloadBuilder.shrinkBody();
//			}
//			payloadBuilder.alertTitle("Addon Track").
//			//alertBody(device_alias+" Device Status")
//			alertBody(data)
//			.badge(1)
//			.sound("default")
//			.customField("AMR", data);
//
//			String payload = payloadBuilder.build();
////			System.out.println("payload:"+payload);
//			service.push("75953556ac4d22be924752a47589810be0860b22f19b40c6df8a90fd3df12032", payload);
////			System.out.println("iPhone notitification has been send successfully");
//
//
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//


	public static void sendRoutePushNotification(int bussiness_device_id, int al_type, String stoppage_name, long stoppage_id){
		try {
			
                        System.out.println("..........         INSIDE PUSH Notification CAll      ............");
//                        URL url = new URL("https://app.truetrackgps.in:8443/sendAlertforStoppage");           		
                        URL url = new URL("https://app.truetrackgps.in:8444/sendAlertforStoppage");           		

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.addRequestProperty("Content-Type", "application/json;charset=UTF-8");
                        System.out.println("inside push notification_den");
			String input = "{ \"business_device_id\": \""+bussiness_device_id+"\",\"stoppage_id\": \""+stoppage_id+"\",\"stoppage_name\": \""+stoppage_name+"\"}";
			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(input);
			writer.flush();
			writer.close();
			os.close();
			conn.connect();
                        
			int responseCode = conn.getResponseCode();
                        
			if (responseCode == HttpURLConnection.HTTP_OK) {
                            System.out.println("notification send for route test");

				String line;
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				while ((line = br.readLine()) != null) {
					System.out.println("=====Response from Back End=======> "+line);
				}
			}else{
//				System.out.println("<=====failed To Send Notification=======>");
			}
			//sendSMS(bussiness_device_id,al_type,stoppage_name,stoppage_id);

		} catch (Exception e) {
			e.printStackTrace();
		}finally {

		}
	}

	private static void sendSMS(int bussiness_device_id, int al_type, String stoppage_name, long stoppage_id) {
		try {
			//URL url = new URL("http://202.131.106.55:8080/device_token/SendRoutePushNotification");
			URL url = new URL("http://52.43.224.3:8080/sendSms");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.addRequestProperty("Content-Type", "application/json;charset=UTF-8");
			String input = "{ \"business_device_id\": \""+bussiness_device_id+"\",\"stoppage_id\": \""+stoppage_id+"\",\"stoppage_name\": \""+stoppage_name+"\"}";
			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(input);
			writer.flush();
			writer.close();
			os.close();
			conn.connect();
			int responseCode = conn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String line;
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				while ((line = br.readLine()) != null) {
//					System.out.println("=====Response from Back End=======> "+line);
				}
			}else{
				System.out.println("<=====failed To Send Notification=======>");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}






