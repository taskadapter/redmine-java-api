package org.redmine.ta.internal;

import java.text.SimpleDateFormat;

final class LocalDateFormat extends ThreadLocal<SimpleDateFormat> {
    private final String format;

    LocalDateFormat(String format) {
        this.format = format;
    }

    protected SimpleDateFormat initialValue() {
        return new SimpleDateFormat(format);
    }
}