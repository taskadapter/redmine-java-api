package com.taskadapter.redmineapi.internal;

import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class URIConfiguratorTest {

    private static final RequestParam param1 = new RequestParam("name1", "value1");
    private static final RequestParam param2 = new RequestParam("name3", "value3");
    private static final RequestParam param2WithDifferentValue = new RequestParam("name3", "anotherValue3");
    private static final RequestParam nullParam = null;

    @Test
    public void testDistinctWithNullParam() {
        List<RequestParam> params = Arrays.asList(param1, nullParam, param2);
        assertThat(URIConfigurator.distinct(params)).containsOnly(param1, param2);
    }

    @Test
    public void testDistinctWithDuplicateParams() {
        List<RequestParam> params = Arrays.asList(param1, param2, param2);
        assertThat(URIConfigurator.distinct(params)).containsOnly(param1, param2);
    }

    @Test
    public void testDistinctWithSimilarNames() {
        List<RequestParam> params = Arrays.asList(param2, param2WithDifferentValue);
        assertThat(URIConfigurator.distinct(params)).containsOnly(param2);
    }

    @Test
    public void distinctWithDifferentNamesSameValuesShouldKeepAllItems() {
        assertThat(URIConfigurator.distinct(
                params("one", "value",
                        "two", "value")
        )).containsOnlyElementsOf(
                params("one", "value",
                        "two", "value"));
    }

    @Test
    public void toNameValueConvertsCollection() {
        assertThat(URIConfigurator.toNameValue(Arrays.asList(param1, param2))).containsOnly(
                new BasicNameValuePair(param1.getName(), param1.getValue()),
                new BasicNameValuePair(param2.getName(), param2.getValue()));
    }

    @Test
    public void toNameValueSkipsNullItems() {
        assertThat(URIConfigurator.toNameValue(Arrays.asList(param1, null, param2))).containsOnly(
                new BasicNameValuePair(param1.getName(), param1.getValue()),
                new BasicNameValuePair(param2.getName(), param2.getValue()));
    }

    private static Collection<RequestParam> params(String name, String value) {
        return Arrays.asList(new RequestParam(name, value));
    }

    private static Collection<RequestParam> params(String name1, String value1,
                                             String name2, String value2) {
        return Arrays.asList(new RequestParam(name1, value1),
                new RequestParam(name2, value2));
    }
}
