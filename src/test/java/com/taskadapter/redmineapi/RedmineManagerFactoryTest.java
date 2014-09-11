package com.taskadapter.redmineapi;

import org.junit.Test;

public class RedmineManagerFactoryTest {

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testNULLHostParameter() {
        RedmineManagerFactory.createUnauthenticated(null);
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public void testEmptyHostParameter() throws RuntimeException {
        RedmineManagerFactory.createUnauthenticated("");
    }

}
