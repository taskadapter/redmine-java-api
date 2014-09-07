package com.taskadapter.redmineapi.bean;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class IssueTest {
    @Test
    public void customFieldWithDuplicateIDReplacesTheOldOne() {
        Issue issue = new Issue();
        CustomField field = CustomFieldFactory.create(5, "name1", "value1");
        CustomField duplicateField = CustomFieldFactory.create(5, "name1", "value1");
        assertThat(issue.getNumberOfCustomFields()).isEqualTo(0);
        issue.addCustomField(field);
        issue.addCustomField(duplicateField);
        assertThat(issue.getNumberOfCustomFields()).isEqualTo(1);
    }
}
