package com.taskadapter.redmineapi.internal;

import java.text.ParseException;
import static org.hamcrest.core.Is.is;
import org.junit.Test;

import org.json.JSONException;
import org.junit.Assert;

/**
 * Redmine JSON date parser tests.
 *
 * @author Matthias Bläsing
 *
 */
public class RedmineDateUtilsTest {

    @Test
    public void testWorkingDates() {
        String[] datesSeenInTheWild = {
            "2015/02/26 20:54:12Z",
            "2015/02/26 20:54:12 +0100",
            "2015/02/26 20:54:12 GMT+01:00",
            "2015-02-26T20:54:12TOT",
            "2015-02-26T20:54:12+01:00",
            "2015/02/26+02:00",
            "2015/02/26",
            "2015-02-26"
        };
        
        int parsed = 0;
        for(String date: datesSeenInTheWild) {
            try {
                RedmineDateUtils.parseDate(date);
                parsed++;
            } catch (IllegalArgumentException ex) {
            }
        }
        
        Assert.assertThat(parsed, is(datesSeenInTheWild.length));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNonWorkingDate() {
        RedmineDateUtils.parseDate("2014-13-01");
    }
}
