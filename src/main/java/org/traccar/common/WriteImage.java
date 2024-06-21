//package org.traccar.common;
//
//import java.awt.image.BufferedImage;
//import java.io.BufferedOutputStream;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.math.BigInteger;
//import java.nio.charset.StandardCharsets;
//import java.sql.SQLException;
//import java.util.Scanner;
//
//import javax.imageio.ImageIO;
//import javax.xml.bind.DatatypeConverter;
//import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
//import org.traccar.Context;
//import org.traccar.PrintOut;
//import org.traccar.model.PictureObservation;
//
//
//public class WriteImage {
//
//	public static void write(String s, long device_id){
//
//		try {
//
//			byte[] byteStr = hexStringToByteArray(s);
//
//
//			BufferedImage bImageFromConvert = ImageIO.read( new ByteArrayInputStream(byteStr));
//                      
//			String photo_name=System.currentTimeMillis()+System.currentTimeMillis()+".jpg";
//
//			if(bImageFromConvert!=null){
//				String outputRecoverdFileName = Config.getIMAGE_PATH()+photo_name;
//				ImageIO.write(bImageFromConvert, "jpg", new File(outputRecoverdFileName));
//
//				/*------====================---------database insert--------=====================*/
//
//				PictureObservation p=new PictureObservation();
//				p.setDeviceId(device_id);
//				try {
//					p.setBusiness_device_id(Context.getDataManager().getBusinessDeviceId((int)device_id));
//					p.setPhoto_name(photo_name);
//					Context.getDataManager().addPictureObservation(p);
//				} catch (ClassNotFoundException e1) {
//					e1.printStackTrace();
//				} catch (SQLException e1) {
//					e1.printStackTrace();
//				}
//			}else{
//				
//                                PrintOut.PrintOutString("null image null");
//                                
//			}
//		} catch (IOException e) {
//			System.out.println(e.getMessage());
//		}
//	}
//
//	public static byte[] hexStringToByteArray(String hex) {
//		int l = hex.length();
//		byte[] data = new byte[l/2];
//		for (int i = 0; i < l; i += 2) {
//			data[i/2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
//					+ Character.digit(hex.charAt(i+1), 16));
//		}
//                             //           System.out.println("in loop hexString-------->"+data.hashCode());
//
//		return data;
//
//	}
//
//        
////        public static byte[] HexStringToByteArray(String hexStr) {
////        byte bArray[] = new byte[hexStr.length()/2];  
////         for(int i=0; i<(hexStr.length()/2); i++){
////              byte firstNibble  = Byte.parseByte(hexStr.substring(2*i,2*i+1),16); // [x,y)
////              byte secondNibble = Byte.parseByte(hexStr.substring(2*i+1,2*i+2),16);
////              int finalByte = (secondNibble) | (firstNibble << 4 ); // bit-operations only with numbers, not bytes.
////              bArray[i] = (byte) finalByte;
////         }
////         return bArray;
////    }
//
//	public static byte[] hexToBytes(String hexString) {
//		HexBinaryAdapter adapter = new HexBinaryAdapter();
//		byte[] bytes = adapter.unmarshal(hexString);
//		return bytes;
//	}
//
//	public static String toHex(String arg) {
//		return String.format("%040x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
//	}
//
//	public static byte[] hexStringToByteArray1(String s) {
//		byte[] b = new byte[s.length() / 2];
//		for (int i = 0; i < b.length; i++) {
//			int index = i * 2;
//			int v = Integer.parseInt(s.substring(index, index + 2), 16);
//			b[i] = (byte) v;
//		}
//		return b;
//	}
//
//	public static void writeAudio(String Str) throws IOException {
//		
//		
//                PrintOut.PrintOutString("Write audio is call in sending time");
//		
//		byte[] byteStr = hexStringToByteArray1(Str);
//		try
//		{
//		    File file2 = new File("/home/vishal/java/"+"mahesh_vitthani.amr");
//		    FileOutputStream os = new FileOutputStream(file2, true);
//		    os.write(byteStr);
//		    os.close();
//		}
//		catch (Exception e)
//		{
//		    e.printStackTrace();
//		}
//		
//		
//	}
//
//	
//	
//}
