package com.dianping.dog.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
    public static final long HOUR_MICROS = 60 * 60 * 1000;
    public static final long DAY_MICROS = 60 * 60 * 1000 * 24;
	public static final long TWO_DAY_MICROS = DAY_MICROS * 2;
	
	public static int getHourOfDay(long timestamp){
		Calendar calendar = Calendar.getInstance(); 
		calendar.setTime(new Date(timestamp));
		return calendar.get(Calendar.HOUR_OF_DAY);
	}
	
	public static int getDayOfWeak(long timestamp){
		Calendar calendar = Calendar.getInstance(); 
		calendar.setTime(new Date(timestamp));
		return calendar.get(Calendar.DAY_OF_WEEK);
	}
	
	public static long getTimeFromString(String day){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		Date date;
      try {
	      date = sdf.parse(day);
      } catch (ParseException e) {
	      return -1;
      }  
		return date.getTime();
	}
	
	public static String formatTime(String format,long timestamp){
		SimpleDateFormat df = null;
		Date date=new Date(timestamp);
		if(format == null){
			df=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		}else{
			df=new SimpleDateFormat(format);
		}
		return df.format(date);
	}
	
	public static String formatTime(String format,Date date){
		SimpleDateFormat df = null;
		if(format == null){
			df=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		}else{
			df=new SimpleDateFormat(format);
		}
		return df.format(date);
	}
	
	public static String getDayNameOfWeak(long timestamp){
		Calendar calendar = Calendar.getInstance(); 
		calendar.setTime(new Date(timestamp));
		int day =  calendar.get(Calendar.DAY_OF_WEEK);
		switch(day){
		case Calendar.MONDAY:
			return "Monday";
		case Calendar.TUESDAY:
			return "Tuesday";
		case Calendar.WEDNESDAY:
			return "Wendesday";
		case Calendar.THURSDAY:
			return "Thursday";
		case Calendar.FRIDAY:
			return "Friday";
		case Calendar.SATURDAY:
			return "Saturday";
		case Calendar.SUNDAY:
			return "Sunday";
		default:
			return "";
		}
	}
}
