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

    @Test
    public void issueCloneIsDeep() {
        final Issue issue = new Issue();
        issue.setSubject("subj1");
        final int initialDoneRatio = 100;
        issue.setDoneRatio(initialDoneRatio);
        Calendar calendar = Calendar.getInstance();
        Date originalStartDate = calendar.getTime();
        issue.setStartDate(originalStartDate);

        issue.setAssigneeId(55);
        issue.setAssigneeName("originalName");
        issue.addCustomField(CustomFieldFactory.create(123, "custom1", "original custom field value"));

        final Issue cloned = issue.cloneDeep();
        issue.setSubject("updated");
        calendar.add(Calendar.DAY_OF_MONTH, 10);
        issue.setStartDate(calendar.getTime());
        issue.setDoneRatio(999);
        issue.setAssigneeId(999);
        issue.setAssigneeName("new name");
        issue.getCustomFieldByName("custom1").setValue("new value");

        assertThat(cloned.getSubject()).isEqualTo("subj1");
        assertThat(cloned.getStartDate()).isEqualTo(originalStartDate);
        assertThat(cloned.getDoneRatio()).isEqualTo(initialDoneRatio);
        assertThat(cloned.getAssigneeId()).isEqualTo(55);
        assertThat(cloned.getAssigneeName()).isEqualTo("originalName");

        final Collection<CustomField> customFields = cloned.getCustomFields();
        assertThat(customFields).hasSize(1);

        final CustomField customFieldById = cloned.getCustomFieldById(123);
        assertThat(customFieldById.getValue()).isEqualTo("original custom field value");

    }

    @Test
    public void customFieldsAreCloned() {
        final Issue issue = new Issue();
        issue.setSubject("subj1");
        issue.addCustomField(CustomFieldFactory.create(123, "custom1", "value1"));

        final Issue cloned = issue.cloneDeep();
        issue.setSubject("updated");
        final Collection<CustomField> customFields = cloned.getCustomFields();
        assertThat(customFields).hasSize(1);

        final CustomField customFieldById = cloned.getCustomFieldById(123);
        assertThat(customFieldById.getValue()).isEqualTo("value1");
    }
}
