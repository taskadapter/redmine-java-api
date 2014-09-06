package com.taskadapter.redmineapi.bean;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class UserTest {
    @Test
    public void customFieldWithDuplicateIDReplacesTheOldOne() {
        User user = new User();
        CustomField field = new CustomField(5, "name1", "value1");
        CustomField duplicateField = new CustomField(5, "name1", "value1");
        assertThat(user.getNumberOfCustomFields()).isEqualTo(0);
        user.addCustomField(field);
        user.addCustomField(duplicateField);
        assertThat(user.getNumberOfCustomFields()).isEqualTo(1);
    }
}
