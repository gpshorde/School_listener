package org.traccar.common;

import java.io.BufferedInputStream;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class File2Hex{
   
	
   public static StringBuilder getHexOfAudio(File file) throws FileNotFoundException, IOException{
	   StringBuilder sb = new StringBuilder();
	    try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file))) {
	    	//sb.append("TK,").append('\n');
	        for (int b; (b = is.read()) != -1;) {
	            String s = Integer.toHexString(b).toUpperCase();
	            if (s.length() == 1) {
	               sb.append('0');
	            	 
	                
	            }
	            //sb.append("0x").append(s).append(',').append('\n');
	            sb.append("0x").append(s);
	        }
	    }
	   return sb;
   }
   
   
   
   
   final protected static char[] hexArray = "0123456789abcdef".toCharArray();
   public static String bytesToHex(byte[] bytes) {
       char[] hexChars = new char[bytes.length * 2];
       for ( int j = 0; j < bytes.length; j++ ){
           int v = bytes[j] & 0xFF;
           hexChars[j * 2] = hexArray[v >>> 4];
           hexChars[j * 2 + 1] = hexArray[v & 0x0F];
       }
       return new String(hexChars);
   }   
   
   private final static String[] hexSymbols = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

   public final static int BITS_PER_HEX_DIGIT = 4;
   public static String toHexFromBytes(final byte[] bytes)
   {
       if(bytes == null || bytes.length == 0)
       {
    	   return "";
       }
       // there are 2 hex digits per byte
       StringBuilder hexBuffer = new StringBuilder(bytes.length * 2);
       // for each byte, convert it to hex and append it to the buffer
       for(int i = 0; i < bytes.length; i++)
       {
           hexBuffer.append(toHexFromByte(bytes[i]));
       }
       return (hexBuffer.toString());
   }
   public static String toHexFromByte(final byte b)
   {
       byte leftSymbol = (byte)((b >>> BITS_PER_HEX_DIGIT) & 0x0f);
       byte rightSymbol = (byte)(b & 0x0f);
       return (hexSymbols[leftSymbol] + hexSymbols[rightSymbol]);
   }
   
   
}
