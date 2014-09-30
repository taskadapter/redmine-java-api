package com.taskadapter.redmineapi;

import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Integration tests for RedmineManager.
 */
public class RedmineManagerTest {

    @Test
    public void unknownHostGivesException() throws RedmineException, IOException {
        final RedmineManager mgr1 = RedmineManagerFactory.createUnauthenticated("http://someunknownhost.com");
        try {
            mgr1.getProjectManager().getProjects();
        } catch (RedmineTransportException e1) {
            assertThat(e1.getMessage()).startsWith("Cannot fetch data from http://someunknownhost.com");
            assertThat(e1.getCause()).isInstanceOf(UnknownHostException.class);
        }
    }
}