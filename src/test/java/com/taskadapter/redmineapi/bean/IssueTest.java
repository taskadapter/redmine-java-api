package com.taskadapter.redmineapi.bean;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

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
}
