package org.alskor.redmine.internal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * it's easier to create separate classes for short and long dates than create
 * workarounds for crappy CastorXML library
 * 
 * @author ask
 * 
 */
public class RedmineShortDateHandler extends RedmineDateHandler {

	// XXX there's bug in Castor: http://jira.codehaus.org/browse/CASTOR-1878
	private static final String FORMAT = "yyyy-MM-dd";
	SimpleDateFormat formatter = new SimpleDateFormat(FORMAT);

	@Override
	public Date getDate(String str) throws ParseException {
		return formatter.parse(str);
	}

	@Override
	public String getString(Date date) {
		return formatter.format(date);
	}

}
