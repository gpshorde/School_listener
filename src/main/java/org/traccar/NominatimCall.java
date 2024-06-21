package org.traccar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

public class NominatimCall {
	private final static String USER_AGENT = "Mozilla/5.0";
	
	
	public static String sendGet(double lat, double lng) throws Exception {
        String url = "http://37.27.116.110/reverse.php?format=json&lat=" + lat + "&lon=" + lng + "&zoom=18";

		//String url = "http://nominatim.openstreetmap.org/reverse?format=json&lat="+lat+"&lon="+lng+"&zoom=19&addressdetails=1";
		
		//String url = "http://52.43.20.17:8088/nominatim/reverse.php?format=json&lat="+lat+"&lon="+lng+"&zoom=21&addressdetails=1";
		
//		String url="http://194.147.58.28/reverse.php?format=json&lat="+lat+"&lon="+lng+"&zoom=21&addressdetails=1";
//		String url="http://185.197.250.167/nominatim/reverse.php?format=json&lat="+lat+"&lon="+lng+"&zoom=21&addressdetails=1";
		//String url="http://185.197.250.167/nominatim/reverse.php?format=json&lat="+lat+"&lon="+lng+"&zoom=21&addressdetails=1";

//		String url = "http://213.136.73.98/reverse.php?format=json&lat="+lat+"&lon="+lng+"&zoom=21&addressdetails=1";
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//		con.setConnectTimeout(2);

		// optional default is GET
		con.setRequestMethod("GET");
                
//                 if (con.getResponseCode() != 200) {
//                    return null;
//                }

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

/*		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
*/
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		String response = "";
                String straddress="";
                

		while ((inputLine = in.readLine()) != null) {
			response +=inputLine;
		}
		in.close();
		JSONObject json=new JSONObject(response);
               
                 
                if(json.has("error"))
                {
                    straddress = null;
           
                    return straddress;
                    
                }else
                {
                   straddress = json.getString("display_name");
                  }
                
             return straddress;

	}

}
