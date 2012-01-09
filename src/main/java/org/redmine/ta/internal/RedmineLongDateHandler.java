/*
   Copyright 2010-2011 Alexey Skorokhodov.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.redmine.ta.internal;

import org.redmine.ta.internal.logging.Logger;
import org.redmine.ta.internal.logging.LoggerFactory;

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
    private static final String FORMAT_REDMINE_1_1 = "yyyy-MM-dd'T'HH:mm:ssZ";

    private static SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_REDMINE_1_1);
    private static final int SHIFT = 3;

    private static Logger logger = LoggerFactory.getLogger(RedmineLongDateHandler.class);

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
        b.append(str.substring(str.length() - SHIFT + 1));
        return b.toString();
    }

    private String convertToRedmine11Format(String str) {
        StringBuffer b = new StringBuffer();
        b.append(str.substring(0, str.length() - SHIFT + 1));
        b.append(":");
        b.append(str.substring(str.length() - SHIFT + 1));
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
        logger.debug(h.getString(d));
    }
}
