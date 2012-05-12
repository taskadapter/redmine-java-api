package org.redmine.ta.internal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Redmine date conversion utils.
 *
 * @author maxkar
 */
public final class RedmineDateUtils {
    private static final String REDMINE_START_DATE_FORMAT = "yyyy-MM-dd";
    private static final ThreadLocal<SimpleDateFormat> sdf = new LocalDateFormat(REDMINE_START_DATE_FORMAT);

    private static SimpleDateFormat getShortFormat() {
        SimpleDateFormat guess = sdf.get();
        if (guess != null) {
            return guess;
        }
        guess = new SimpleDateFormat(REDMINE_START_DATE_FORMAT);
        sdf.set(guess);
        return guess;
    }

    public static String formatShortDate(Date date) {
        return getShortFormat().format(date);
    }

    public static Date parseShortDate(String date) throws ParseException {
        return getShortFormat().parse(date);
    }

	/**
	 * Full date format.
	 */
	public static final LocalDateFormat FULL_DATE_FORMAT = new LocalDateFormat(
			"yyyy/MM/dd HH:mm:ss Z");

	/**
	 * Short date format.
	 */
	public static final LocalDateFormat SHORT_DATE_FORMAT = new LocalDateFormat(
			"yyyy/MM/dd");
}
