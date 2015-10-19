package com.taskadapter.redmineapi.internal;

import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import static com.taskadapter.redmineapi.internal.RedmineDateParser.parse;
import static org.assertj.core.api.Assertions.assertThat;

public class RedmineDateParserTest {
    private static final LocalDateFormat FULL_DATE_FORMAT_V3 = new LocalDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");

    @Test
    public void allRedmineDateFormatsAreParsed() throws ParseException {
        // Redmine 1.x short format
        dateIsParsedTo("2015/03/11", new Date(115, 02, 11));
        // Redmine 1.x long format
        dateIsParsedTo("2015/03/11 17:22:37 -0700", "2015-03-11T17:22:37.000PDT");

        // Redmine 2.x short format
        dateIsParsedTo("2015-03-11", new Date(115, 02, 11));
        // Redmine 2.x long format
        dateIsParsedTo("2015-03-12T00:22:37Z", "2015-03-11T17:22:37.000PDT");

        // Redmine 3.0 long format
        dateIsParsedTo("2015-03-12T00:22:37.123Z", "2015-03-11T17:22:37.123PDT");
    }

    private void dateIsParsedTo(String originalDateString, String expectedDateString) throws ParseException {
        assertThat(parse(originalDateString)).isEqualTo(getDate(expectedDateString));
    }

    private void dateIsParsedTo(String originalDateString, Date expectedDate) throws ParseException {
        assertThat(parse(originalDateString)).isEqualTo(expectedDate);
    }

    private static Date getDate(String str) {
        try {
            return FULL_DATE_FORMAT_V3.get().parse(str);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}