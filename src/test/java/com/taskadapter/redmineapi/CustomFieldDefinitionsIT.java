package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.CustomFieldDefinition;
import com.taskadapter.redmineapi.internal.RedmineJSONParser;
import com.taskadapter.redmineapi.internal.json.JsonInput;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import org.json.JSONException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The tests expect some manual (one-time) setup in the Redmine server because
 * there is no remote API to create custom fields definitions (as of January 2015).
 * <p>
 * Exactly 3 custom fields must be configured:
 *   - id: 1, customized_type: issue, name: my_custom_1, type: string
 *   - id: 2, customized_type: issue, name: custom_boolean_1, type: bool
 *   - id: 3, customized_type: issue, name: custom_multi_list, type: list, 
 *            multiple: true, possible_values: V1, V2, V3, default: V2
 */
public class CustomFieldDefinitionsIT {
    private static final String CUSTOM_FIELDS_FILE = "custom_fields_redmine_2.3.json";
    private static CustomFieldManager customFieldManager;

    @BeforeClass
    public static void oneTimeSetup() {
        RedmineManager mgr = IntegrationTestHelper.createRedmineManager();
        customFieldManager = mgr.getCustomFieldManager();
    }

    @Test
    public void testGetCustomFields() throws RedmineException {
        final List<CustomFieldDefinition> definitions = customFieldManager.getCustomFieldDefinitions();
        assertThat(definitions.size()).isGreaterThanOrEqualTo(3);

        final CustomFieldDefinition myCustom1 = getCustomFieldDefinitionByName(definitions, "my_custom_1");
        assertThat(myCustom1.getCustomizedType()).isEqualTo("issue");
        assertThat(myCustom1.getFieldFormat()).isEqualTo("string");

        final CustomFieldDefinition customBoolean1 = getCustomFieldDefinitionByName(definitions, "custom_boolean_1");
        assertThat(customBoolean1.getCustomizedType()).isEqualTo("issue");
        assertThat(customBoolean1.getFieldFormat()).isEqualTo("bool");

        final CustomFieldDefinition customMultiList = getCustomFieldDefinitionByName(definitions, "custom_multi_list");
        assertThat(customMultiList.getCustomizedType()).isEqualTo("issue");
        assertThat(customMultiList.getFieldFormat()).isEqualTo("list");
        assertThat(customMultiList.getDefaultValue()).isEqualTo("V2");
        assertThat(customMultiList.getPossibleValues()).containsExactly("V1", "V2", "V3");
        assertThat(customMultiList.isMultiple()).isTrue();
    }

    private static CustomFieldDefinition getCustomFieldDefinitionByName(List<CustomFieldDefinition> definitions, String name) {
        for (CustomFieldDefinition definition : definitions) {
            if (name.equals(definition.getName())) {
                return definition;
            }
        }
        throw new RuntimeException("Custom Field Definition not found: " + name);
    }

    @Test
    public void savedJSonResponseFromRedmine23CanBeParsed() throws IOException, JSONException {
        String str = MyIOUtils.getResourceAsString(CUSTOM_FIELDS_FILE);
        List<CustomFieldDefinition> definitions = JsonInput.getListOrEmpty(
					RedmineJSONParser.getResponse(str), "custom_fields",
					RedmineJSONParser::parseCustomFieldDefinition);
        CustomFieldDefinition field0 = definitions.get(0);
        assertThat(field0.getId()).isEqualTo(1);
        assertThat(field0.getName()).isEqualTo("my_custom_1");
        assertThat(field0.getCustomizedType()).isEqualTo("issue");
        assertThat(field0.getFieldFormat()).isEqualTo("string");
        assertThat(field0.getRegexp()).isEqualTo("some.*");
        assertThat(field0.getMinLength()).isEqualTo((Integer) 5);
        assertThat(field0.getMaxLength()).isEqualTo((Integer) 80);
        assertThat(field0.isFilter()).isEqualTo(true);
        assertThat(field0.isSearchable()).isEqualTo(true);
        assertThat(field0.isMultiple()).isEqualTo(false);
        assertThat(field0.isVisible()).isEqualTo(true);
        assertThat(field0.isRequired()).isEqualTo(false);
        assertThat(field0.getDefaultValue()).isEqualTo("");
        assertThat(field0.getPossibleValues().size()).isEqualTo(0);
        assertThat(field0.getTrackers().get(0).getId()).isEqualTo((Integer) 1);
        assertThat(field0.getTrackers().get(1).getId()).isEqualTo((Integer) 2);
        assertThat(field0.getTrackers().get(2).getId()).isEqualTo((Integer) 3);
        assertThat(field0.getRoles().size()).isEqualTo(0);

        CustomFieldDefinition field1 = definitions.get(1);
        assertThat(field1.getId()).isEqualTo((Integer) 2);
        assertThat(field1.getName()).isEqualTo("custom_boolean_1");
        assertThat(field1.getCustomizedType()).isEqualTo("issue");
        assertThat(field1.getFieldFormat()).isEqualTo("bool");
        assertThat(field1.getRegexp()).isEqualTo("");
        assertThat(field1.getMinLength()).isEqualTo(null);
        assertThat(field1.getMaxLength()).isEqualTo(null);
        assertThat(field1.isFilter()).isEqualTo(false);
        assertThat(field1.isSearchable()).isEqualTo(false);
        assertThat(field1.isMultiple()).isEqualTo(false);
        assertThat(field1.isVisible()).isEqualTo(true);
        assertThat(field1.isRequired()).isEqualTo(false);
        assertThat(field1.getDefaultValue()).isEqualTo("");
        assertThat(field1.getPossibleValues().get(0)).isEqualTo("1");
        assertThat(field1.getPossibleValues().get(1)).isEqualTo("0");
        assertThat(field1.getTrackers().size()).isEqualTo(3);
        assertThat(field1.getRoles().size()).isEqualTo(0);

        CustomFieldDefinition field2 = definitions.get(2);
        assertThat(field2.getId()).isEqualTo((Integer) 3);
        assertThat(field2.getName()).isEqualTo("Test");
        assertThat(field2.getCustomizedType()).isEqualTo("issue");
        assertThat(field2.getFieldFormat()).isEqualTo("bool");
        assertThat(field2.getRegexp()).isEqualTo("");
        assertThat(field2.getMinLength()).isEqualTo(null);
        assertThat(field2.getMaxLength()).isEqualTo(null);
        assertThat(field2.isFilter()).isEqualTo(false);
        assertThat(field2.isSearchable()).isEqualTo(false);
        assertThat(field2.isMultiple()).isEqualTo(false);
        assertThat(field2.isVisible()).isEqualTo(false);
        assertThat(field2.isRequired()).isEqualTo(true);
        assertThat(field2.getDefaultValue()).isEqualTo("1");
        assertThat(field2.getPossibleValues().get(0)).isEqualTo("1");
        assertThat(field2.getPossibleValues().get(1)).isEqualTo("0");
        assertThat(field2.getTrackers().size()).isEqualTo(0);
        assertThat(field2.getRoles().get(0).getId()).isEqualTo((Integer) 4);
    }
}
