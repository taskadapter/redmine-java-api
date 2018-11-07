package com.taskadapter.redmineapi.internal;

import com.taskadapter.redmineapi.RedmineException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class DirectObjectsSearcher {

    public static <T> ResultsWrapper<T> getObjectsListNoPaging(Transport transport, Map<String, String> map, Class<T> classRef) throws RedmineException {
        return transport.getObjectsListNoPaging(classRef, toNameValuePairs(map));
    }

    public static <T> ResultsWrapper<T> getChildObjectsListNoPaging(Transport transport, Map<String, String> map,
                                                                    Class<?> parentClass, String parentId, Class<T> classRef) throws RedmineException {
        return transport.getChildObjectsListNoPaging(parentClass, parentId, classRef, toNameValuePairs(map));
    }

    private static Set<NameValuePair> toNameValuePairs(Map<String, String> map) {
        return map.entrySet()
                .stream()
                .map(param -> new BasicNameValuePair(param.getKey(), param.getValue()))
                .collect(Collectors.toSet());
    }
}
