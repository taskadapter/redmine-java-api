package org.redmine.ta.internal;

import org.junit.Test;
import org.redmine.ta.beans.Issue;

import static org.junit.Assert.assertTrue;

public class RedmineXMLGeneratorTest {
    /**
     * Regression test for http://code.google.com/p/redmine-java-api/issues/detail?id=98
     */
    @Test
    public void priorityIdIsAddedToXMLIfProvided() {
        Issue issue = new Issue();
        issue.setPriorityId(1);
        String generatedXML = RedmineXMLGenerator.toXML("some_project_key", issue);
        assertTrue(generatedXML.contains("<priority_id>1</priority_id>"));
    }
}
