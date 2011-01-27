package org.alskor.redmine.internal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RedmineLongDateHandler extends RedmineDateHandler {

	/**
	 * sample: Wed Apr 14 13:56:30 -0700 2010
	 */
//	private static final String FORMAT_REDMINE_1_0 = "EEE MMM dd HH:mm:ss Z yyyy";
	
	/**
	 * sample: 
	 * <p>2011-01-20T18:33:29-08:00
	 * <p>see Redmine's bug: http://www.redmine.org/issues/7394
	 */
	private static final String FORMAT_REDMINE_1_1 = "yyyy-mm-dd'T'HH:mm:ssZ";
	
	private static SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_REDMINE_1_1);
	private static final int SHIFT = 3;

	@Override
	public Date getDate(String str) throws ParseException {
		// convert to RFC 822 format
		String converted = convertToRFC822Format(str);
		Date date = formatter.parse(converted);
		return date;
	}
	
	private String convertToRFC822Format(String str) {
		StringBuffer b = new StringBuffer();
		b.append(str.substring(0, str.length() - SHIFT));
		b.append(str.substring(str.length()-SHIFT+1));
		return b.toString();
	}
	
	private String convertToRedmine11Format(String str) {
		StringBuffer b = new StringBuffer();
		b.append(str.substring(0, str.length() - SHIFT+1));
		b.append(":");
		b.append(str.substring(str.length()-SHIFT+1));
		return b.toString();
	}
	
	@Override
	public String getString(Date date) {
		String rfcFormat = formatter.format(date);
		String redmineFormat = convertToRedmine11Format(rfcFormat);
		return redmineFormat;
	}

	public static void main(String[] args) throws ParseException {
//		String dateStr = "2011-01-20T18:33:29-01:00";
		Date d = new Date();
		RedmineLongDateHandler h = new RedmineLongDateHandler();
//		System.out.println(h.getDate(dateStr));
		System.out.println(h.getString(d));
	}
}
