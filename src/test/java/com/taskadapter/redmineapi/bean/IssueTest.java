package com.taskadapter.redmineapi.bean;

import java.util.Date;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;
import org.junit.Test;


public class IssueTest {
    @Test
    public void customFieldWithDuplicateIDReplacesTheOldOne() {
        Issue issue = new Issue();
        CustomField field = CustomFieldFactory.create(5, "name1", "value1");
        CustomField duplicateField = CustomFieldFactory.create(5, "name1", "value1");
        assertThat(issue.getCustomFields().size()).isEqualTo(0);
        issue.addCustomField(field);
        issue.addCustomField(duplicateField);
        assertThat(issue.getCustomFields().size()).isEqualTo(1);
    }
    
    @Test
    public void updateTracking() { 
        // Make sure, alle tracked fields reflect setting them
        Issue issue = new Issue();
        
        assertEquals(0, issue.updated.size());
        
        issue.setSubject("Demo Subject");
        
        assertEquals(1, issue.updated.size());
        assertThat(!issue.wasUpdated(Issue.PROP_PROJECT));
        
        issue.setParentId(1);
        
        assertThat(issue.wasUpdated(Issue.PROP_PROJECT));
        assertEquals(2, issue.updated.size());
        
        issue.setEstimatedHours(2.0f);
        
        assertEquals(3, issue.updated.size());
        
        issue.setSpentHours(1.0f);
        
        assertEquals(4, issue.updated.size());
        
        issue.setAssignee(new User(1));
        
        assertEquals(5, issue.updated.size());
        
        issue.setPriorityId(1);
        
        assertEquals(6, issue.updated.size());
        
        issue.setDoneRatio(1);
        
        assertEquals(7, issue.updated.size());
        
        issue.setProject(new Project(1));
        
        assertEquals(8, issue.updated.size());
        
        issue.setAuthor(new User(2));
        
        assertEquals(9, issue.updated.size());
        
        issue.setStartDate(new Date());
        
        assertEquals(10, issue.updated.size());
        
        issue.setDueDate(new Date());
        
        assertEquals(11, issue.updated.size());
        
        issue.setTracker(new Tracker(1));
        
        assertEquals(12, issue.updated.size());
        
        issue.setDescription("Description");
        
        assertEquals(13, issue.updated.size());
        
        issue.setCreatedOn(new Date());
        
        assertEquals(14, issue.updated.size());
        
        issue.setUpdatedOn(new Date());
        
        assertEquals(15, issue.updated.size());
        assertThat(! issue.wasUpdated(Issue.PROP_STATUS_ID));
        
        issue.setStatusId(1);
        
        assertThat(issue.wasUpdated(Issue.PROP_STATUS_ID));
        assertEquals(16, issue.updated.size());
        
        issue.setTargetVersion(new Version(1));
        
        assertEquals(17, issue.updated.size());
        
        issue.setCategory(new IssueCategory(1));
        
        // Each change should be accounted for
        assertEquals(18, issue.updated.size());
        
        // After (re)setting  update tracking the list should be empty
        issue.setUpdateTracking(true);
        
        assertEquals(0, issue.updated.size());
    }
}
