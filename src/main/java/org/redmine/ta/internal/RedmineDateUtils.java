package org.redmine.ta.internal;

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
	 * Short date format.
	 */
	public static final LocalDateFormat SHORT_DATE_FORMAT = new LocalDateFormat(
			"yyyy/MM/dd");
}
