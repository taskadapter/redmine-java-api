package com.taskadapter.redmineapi;

import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;

import static org.junit.Assert.assertTrue;

/**
 * Integration tests for RedmineManager.
 */
public class RedmineManagerTest {

    @Test
    public void unknownHostGivesException() throws RedmineException, IOException {
        final RedmineManager mgr1 = RedmineManagerFactory.createUnauthenticated("http://The.unknown.host");
        try {
            mgr1.getProjectManager().getProjects();
        } catch (RedmineTransportException e1) {
            assertTrue(e1.getMessage().startsWith(
                    "Cannot fetch data from http://The.unknown.host/"));
            assertTrue(e1.getCause() instanceof UnknownHostException);
        }
    }
}