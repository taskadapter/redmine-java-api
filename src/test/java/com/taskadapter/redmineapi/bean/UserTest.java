package com.taskadapter.redmineapi.bean;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {
    @Test
    public void customFieldWithDuplicateIDReplacesTheOldOne() {
        User user = UserFactory.create();
        CustomField field = CustomFieldFactory.create(5, "name1", "value1");
        CustomField duplicateField = CustomFieldFactory.create(5, "name1", "value1");
        assertThat(user.getCustomFields().size()).isEqualTo(0);
        user.addCustomField(field);
        user.addCustomField(duplicateField);
        assertThat(user.getCustomFields().size()).isEqualTo(1);
    }
}
