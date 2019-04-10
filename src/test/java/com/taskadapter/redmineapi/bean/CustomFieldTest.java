package com.taskadapter.redmineapi.bean;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class CustomFieldTest {
    @Test
    public void fieldsAreEqualIfIDIsEqual() {
        assertThat(getField(1, "A"), equalTo(getField(1, "ANOTHER")));
        assertThat(getField(1, "A", "B"), equalTo(getField(1, "SOMETHING", "ANOTHER")));
    }

    @Test
    public void fieldsAreNotEqualIfIDsAreNotEqual() {
        assertThat(getField(1), not(equalTo(getField(2))));
        assertThat(getField(1, "A"), not(equalTo(getField(2, "A"))));
        assertThat(getField(1, "A", "B"), not(equalTo(getField(2, "A", "B"))));
    }

    private CustomField getField(int id, String... values) {
        CustomField field = new CustomField().setId(id);
        field.setValues(Arrays.asList(values));
        return field;
    }
}
