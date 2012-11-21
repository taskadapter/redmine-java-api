package com.taskadapter.redmineapi.internal;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import com.taskadapter.redmineapi.bean.Issue;

public class RedmineJSONGeneratorTest {
    /**
     * Ported regression test for
     * http://code.google.com/p/redmine-java-api/issues/detail?id=98 from
     * RedmineXMLGeneratorTest
     */
    @Test
    public void priorityIdIsAddedToXMLIfProvided() {
        Issue issue = new Issue();
        issue.setPriorityId(1);
        final String generatedJSON = RedmineJSONBuilder.toSimpleJSON(
                "some_project_key", issue, RedmineJSONBuilder.ISSUE_WRITER);
        assertTrue(generatedJSON.contains("\"priority_id\":1"));
    }
    
     /*
     * Covering this bug 
     * https://github.com/taskadapter/redmine-java-api/issues/61    
     */
   @Test
    public void nonMandatoryFieldsAreResetIfResettable() {
        Issue issue = new Issue();
        issue.setParentId(null);
        issue.setEstimatedHours(null);
        issue.setDescription(null);
        issue.setResettable(Boolean.TRUE);
        final String generatedJSON = RedmineJSONBuilder.toSimpleJSON(
                "some_project_key", issue, RedmineJSONBuilder.ISSUE_WRITER);
        assertTrue(generatedJSON.contains("\"parent_issue_id\":null"));
        assertTrue(generatedJSON.contains("\"estimated_hours\":null"));
        assertTrue(generatedJSON.contains("\"description\":null"));
    }
   
   /*
    * Covering this bug 
    * https://github.com/taskadapter/redmine-java-api/issues/61    
    */
  @Test
   public void notResetIfNotResettable() {
       Issue issue = new Issue();
       issue.setParentId(null);
       issue.setEstimatedHours(null);
       issue.setDescription(null);
       final String generatedJSON = RedmineJSONBuilder.toSimpleJSON(
               "some_project_key", issue, RedmineJSONBuilder.ISSUE_WRITER);
       assertTrue(!generatedJSON.contains("\"parent_issue_id\":null"));
       assertTrue(!generatedJSON.contains("\"estimated_hours\":null"));
       assertTrue(!generatedJSON.contains("\"description\":null"));
   }

}