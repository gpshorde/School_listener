package org.traccar.common;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTimeZone;

import com.vividsolutions.jts.io.ParseException;

public class DateSync {
	
	public static Timestamp getUTCTime() throws java.text.ParseException{
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.S");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));

		//Local time zone   
		SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.S");

		//Time in GMT
		return new Timestamp(dateFormatLocal.parse( dateFormatGmt.format(new Date())).getTime());
	}
}


