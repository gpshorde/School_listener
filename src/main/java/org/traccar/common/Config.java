package org.traccar.common;

public class Config {


		

	private static String IMAGE_PATH= "/home/ubuntu/tt_images/";//aws server
        
//    private static String IMAGE_PATH= "/home/krishna/TT01_Images/"; // local server
//        private static String IMAGE_PATH= "/home/vishal/image/"; // local server


	
	public  static String getIMAGE_PATH() {
		return IMAGE_PATH;
	}
	
	public static void setIMAGE_PATH(String iMAGE_PATH) {
		IMAGE_PATH = iMAGE_PATH;
	}
	
}
