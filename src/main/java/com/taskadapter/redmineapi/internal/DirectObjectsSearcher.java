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
        final Set<RequestParam> set = map.entrySet()
                .stream()
                .map(param -> new RequestParam(param.getKey(), param.getValue()))
                .collect(Collectors.toSet());
        return transport.getObjectsListNoPaging(classRef, set);
    }
}
