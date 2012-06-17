package com.taskadapter.redmineapi.internal;

/**
 * Redmine date conversion utils.
 * 
 * @author maxkar
 */
public final class RedmineDateUtils {
	/**
	 * Full date format.
	 */
	public static final LocalDateFormat FULL_DATE_FORMAT = new LocalDateFormat(
			"yyyy/MM/dd HH:mm:ss Z");

	/**
	 * Full date format.
	 */
	public static final LocalDateFormat FULL_DATE_FORMAT_V2 = new LocalDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssz");
	/**
	 * Short date format.
	 */
	public static final LocalDateFormat SHORT_DATE_FORMAT = new LocalDateFormat(
			"yyyy/MM/dd");

	/**
	 * Short date format.
	 */
	public static final LocalDateFormat SHORT_DATE_FORMAT_V2 = new LocalDateFormat(
			"yyyy-MM-dd");
}
