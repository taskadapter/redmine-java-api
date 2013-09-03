package com.taskadapter.redmineapi.bean;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CustomFieldTest {
    @Test
    public void valuesAreEqual() {
        assertThat(getField("A"), equalTo(getField("A")));
        assertThat(getField("A", "B"), equalTo(getField("A", "B")));
    }

    @Test
    public void valuesNotEqual() {
        assertThat(getField(), not(equalTo(getField("something"))));
        assertThat(getField("A"), not(equalTo(getField("B"))));
        assertThat(getField("A", "B"), not(equalTo(getField("B", "A"))));
    }

    private CustomField getField(String... values) {
        CustomField field = new CustomField();
        field.setValues(Arrays.asList(values));
        return field;
    }
}
