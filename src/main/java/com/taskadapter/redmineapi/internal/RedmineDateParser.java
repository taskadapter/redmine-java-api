package com.taskadapter.redmineapi.internal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// side note... can you PLEASE stop changing date formats already? please?
// I submitted a Redmine feature request to read current date format from the server: http://www.redmine.org/issues/19354
public final class RedmineDateParser {
    /**
     * Full date format.
     */
    public static final LocalDateFormat FULL_DATE_FORMAT = new LocalDateFormat("yyyy/MM/dd HH:mm:ss Z");

    /**
     * Full date format used by Redmine 2.
     * <p>Example: 2015-01-23T00:15:24Z
     */
    public static final LocalDateFormat FULL_DATE_FORMAT_V2 = new LocalDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

    /**
     * Full date format used by Redmine 3.
     * <p>Example from redmine 3.0.0: 2015-01-29T10:06:19.000Z
     */
    public static final LocalDateFormat FULL_DATE_FORMAT_V3 = new LocalDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");

    private static final String SHORT_DATE_PATTERN_V1 = "yyyy/MM/dd";
    private static final String SHORT_DATE_PATTERN_V2 = "yyyy-MM-dd";

    /**
     * Short date format for Redmine v. 1.x.
     */
    public static final LocalDateFormat SHORT_DATE_FORMAT = new LocalDateFormat(SHORT_DATE_PATTERN_V1);

    /**
     * Short date format for Redmine v. 2.x.
     */
    public static final LocalDateFormat SHORT_DATE_FORMAT_V2 = new LocalDateFormat(SHORT_DATE_PATTERN_V2);

    private static final int SHORT_DATE_FORMAT_MAX_LENGTH = Math.max(SHORT_DATE_PATTERN_V1.length(), SHORT_DATE_PATTERN_V2.length());

    static Date parse(String dateStr) throws ParseException {
        if (dateStr.length() > SHORT_DATE_FORMAT_MAX_LENGTH) {
            return parseLongFormat(dateStr);
        }
        return parseShortFormat(dateStr);
    }

    private static Date parseShortFormat(String dateStr) throws ParseException {
        final SimpleDateFormat format;
        if (dateStr.length() >= 5 && dateStr.charAt(4) == '/') {
            format = RedmineDateParser.SHORT_DATE_FORMAT.get();
        } else {
            format = RedmineDateParser.SHORT_DATE_FORMAT_V2.get();
        }
        return format.parse(dateStr);
    }

    private static Date parseLongFormat(String dateStr) throws ParseException {
        final SimpleDateFormat format;
        if (dateStr.length() >= 5 && dateStr.charAt(4) == '/') {
            format = RedmineDateParser.FULL_DATE_FORMAT.get();
            return format.parse(dateStr);
        }
        dateStr = normalizeTimeZoneInfo(dateStr);

        if (dateStr.indexOf('.') < 0) {
            format = RedmineDateParser.FULL_DATE_FORMAT_V2.get();
        } else {
            format = RedmineDateParser.FULL_DATE_FORMAT_V3.get();
        }
        return format.parse(dateStr);
    }

    private static String normalizeTimeZoneInfo(String dateStr) throws ParseException {
        if (dateStr.endsWith("Z")) {
            dateStr = dateStr.substring(0, dateStr.length() - 1)
                    + "GMT-00:00";
        } else {
            final int inset = 6;
            if (dateStr.length() <= inset) {
                throw new ParseException("Bad date value " + dateStr, inset);
            }
            String s0 = dateStr.substring(0, dateStr.length() - inset);
            String s1 = dateStr.substring(dateStr.length() - inset,
                    dateStr.length());
            dateStr = s0 + "GMT" + s1;
        }
        return dateStr;
    }
}
