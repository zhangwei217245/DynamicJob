package org.uiuc.cigi.crawler.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * simple date format transformation class
 * @author dawning zhzhang@cnic.cn
 *	2012-11-22 1:25:19
 *
 */
public class DateFormat {
	
	public static String convertDate(Date date,String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format,Locale.ENGLISH);
		return sdf.format(date);
	}
	
	public static Date convertString(String dateStr,String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format,Locale.ENGLISH);
		try {
			return sdf.parse(dateStr);
		} catch (ParseException e) {
			return null;
		}
	}
	
	public static void main(String[] args){
		System.out.println(convertString("Mon, 26 Sep 2011 18:48:21 +0000","EEE, d MMM yyyy HH:mm:ss Z"));
//		System.out.println(convertDate(new Date(),"EEE, d MMM yyyy HH:mm:ss Z"));
	}
}
