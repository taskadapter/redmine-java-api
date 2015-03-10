package com.taskadapter.redmineapi.internal;

import java.text.ParseException;
import java.util.Date;
import javax.xml.bind.DatatypeConverter;

/**
 * Redmine date conversion utils.
 * 
 * @author maxkar
 * @author Matthias Bläsing
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
        
        private static final LocalDateFormat[] dateTimeFormats = new LocalDateFormat[] {
            FULL_DATE_FORMAT,
            FULL_DATE_FORMAT_V2
        };
        
        // SHORT_DATE_FORMAT_V2 is covered by DatatypeConverter
        private static final LocalDateFormat[] dateFormats = new LocalDateFormat[] {
            SHORT_DATE_FORMAT
        };
        
        /**
         * Try to parse supplied string as a datetime/date and return resulting
         * date.
         * 
         * @param inputString
         * @return parsed date
         * @throws IllegalArgumentException in case of parse fail
         * @throws NullPointerException if inputString is null
         */
        public static Date parseDate(String inputString) {
            // The idea:
            // - Try to parse supplied string as a datetime using JAXB DatatypeConverter
            // - If that fails fall back to old datetime formats - known to be used
            // - Try to parse supplied string as a date using JAXB DatatypeConverter
            // - If that fails fall back to old short format
            // If everything fails it is not a date...
            if(inputString == null) {
                throw new NullPointerException("Input must not be null");
            }
            Date result = null;
            try {
                result = DatatypeConverter.parseDateTime(inputString).getTime();
            } catch (IllegalArgumentException ex) {
                // Ok - not a valid xsd:datetime
            }
            if (result != null) {
                return result;
            }
            for(LocalDateFormat ldf: dateTimeFormats) {
                try {
                    result = ldf.get().parse(inputString);
                    break;
                } catch (ParseException ex) {
                    // Ok - try next
                }
            }
            if(result != null) {
                return result;
            }
            try {
                result = DatatypeConverter.parseDate(inputString).getTime();
            } catch (IllegalArgumentException ex) {
                // Ok - not a valid xsd:date
            }
            if(result != null) {
                return result;
            }
            for(LocalDateFormat ldf: dateFormats) {
                try {
                    result = ldf.get().parse(inputString);
                    break;
                } catch (ParseException ex) {
                    // Ok - try next
                }
            }
            if(result == null) {
                throw new IllegalArgumentException("Failed to parse: " + inputString);
            }
            return result;
        }
}
