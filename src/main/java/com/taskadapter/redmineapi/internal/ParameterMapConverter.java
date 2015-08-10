package com.taskadapter.redmineapi.internal;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ParameterMapConverter {
    public static Set<NameValuePair> getNameValuePairs(Map<String, String> pParameters) {
        final Set<NameValuePair> params = new HashSet<NameValuePair>();

        for (final Map.Entry<String, String> param : pParameters.entrySet()) {
            params.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }
        return params;
    }
}
