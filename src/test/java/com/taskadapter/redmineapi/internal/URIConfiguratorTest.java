package com.taskadapter.redmineapi.internal;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class URIConfiguratorTest {

    private static RequestParam param1;
    private static RequestParam nullParam;
    private static RequestParam paramName3;
    private static RequestParam similarName3;

    @BeforeClass
    public static void init() {
        param1 = new RequestParam("name1", "param1");
        nullParam = null;
        paramName3 = new RequestParam("name3", "paramName3");
        similarName3 = new RequestParam("name3", "similarName3");
    }

    @Test
    public void testDistinctWithNullParam() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<RequestParam> params = Arrays.asList(param1, nullParam, paramName3);
        assertCollectionEquals(distinct(params), Arrays.asList(param1, paramName3));
    }

    @Test
    public void testDistinctWithDuplicateParams() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<RequestParam> params = Arrays.asList(param1, paramName3, paramName3);
        assertCollectionEquals(distinct(params), Arrays.asList(param1, paramName3));
    }

    @Test
    public void testDistinctWithSimilarNames() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<RequestParam> params = Arrays.asList(paramName3, similarName3);
        assertCollectionEquals(distinct(params), Collections.singletonList(paramName3));
    }

    @Test
    public void testToNameValue() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Collection<NameValuePair> expected = new HashSet<>();
        expected.add(new BasicNameValuePair("name1", "param1"));
        Assert.assertEquals(toNameValue(Collections.singletonList(param1)), expected);
    }

    private static Collection<? extends NameValuePair> toNameValue(Collection<? extends RequestParam> origParams) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = URIConfigurator.class.getDeclaredMethod("toNameValue", Collection.class);
        method.setAccessible(true);
        return (Collection<? extends NameValuePair>) method.invoke(URIConfigurator.class, origParams);
    }

    private static Collection<? extends RequestParam> distinct(Collection<? extends RequestParam> origParams) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = URIConfigurator.class.getDeclaredMethod("distinct", Collection.class);
        method.setAccessible(true);
        return (Collection<? extends RequestParam>) method.invoke(URIConfigurator.class, origParams);
    }

    private static void assertCollectionEquals(Collection<? extends RequestParam> actual, Collection<? extends RequestParam> expected) {
        Collection<? extends RequestParam> actualSorted =
                actual
                        .stream()
                        .sorted(Comparator.comparing(RequestParam::getName))
                        .collect(Collectors.toList());
        Assert.assertEquals(actualSorted, expected);
    }
}
