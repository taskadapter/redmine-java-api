package com.taskadapter.redmineapi.internal;

import com.taskadapter.redmineapi.RedmineException;
import org.apache.http.NameValuePair;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DirectObjectsSearcher {
    public static <T> List<T> getObjectsListNoPaging(Transport transport, Map<String, String> map, Class<T> classRef) throws RedmineException {
        Set<NameValuePair> params = ParameterMapConverter.getNameValuePairs(map);
        final Transport.ResultsWrapper<T> wrapper = transport.getObjectsListNoPaging(classRef, params);
        return wrapper.getResults();
    }
}
