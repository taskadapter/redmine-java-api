package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.CustomFieldDefinition;
import com.taskadapter.redmineapi.internal.RedmineJSONParser;
import com.taskadapter.redmineapi.internal.json.JsonInput;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import org.json.JSONException;

import static org.junit.Assert.*;

/**
 * Test CustomFeildDefinitons API - the API is only read-only (as of 2.6.0),
 * so the tests asume a preprogrammed configuration.
 * 
 * Exactly two custom fields are defined (from IssueManagerTest#testCustomFields:
 *   - id: 1, customized_type: issue, name: my_custom_1, type: string
 *   - id: 2, customized_type: issue, name: custom_boolean_1, type: bool
 */
public class CustomFieldDefinitionsTest {
    private static final String CUSTOM_FIELDS_FILE = "custom_fields_redmine_2.3.json";
    private static RedmineManager mgr;
    private static CustomFieldManager customFieldManager;

    @BeforeClass
    public static void oneTimeSetup() {
        mgr = IntegrationTestHelper.createRedmineManager();
        customFieldManager = mgr.getCustomFieldManager();
    }

    @Test
    public void testGetCustomFields() throws RedmineException {
        List<CustomFieldDefinition> definitions = customFieldManager.getCustomFieldDefinitions();
        assertEquals(definitions.size(), 2);
        for(CustomFieldDefinition cfd: definitions) {
            if(cfd.getId() == 1) {
                assertEquals(cfd.getCustomizedType(), "issue");
                assertEquals(cfd.getName(), "my_custom_1");
                assertEquals(cfd.getFieldFormat(), "string");
            } else if (cfd.getId() == 2) {
                assertEquals(cfd.getCustomizedType(), "issue");
                assertEquals(cfd.getName(), "custom_boolean_1");
                assertEquals(cfd.getFieldFormat(), "bool");
            }
        }
    }
    
    @Test
    public void testDataFormat() throws IOException, JSONException {
        String str = MyIOUtils.getResourceAsString(CUSTOM_FIELDS_FILE);
        List<CustomFieldDefinition> definitions = JsonInput.getListOrEmpty(
					RedmineJSONParser.getResponse(str),
                                        "custom_fields",
					RedmineJSONParser.CUSTOM_FIELD_DEFINITION_PARSER);
        assertEquals(definitions.get(0).getId(), (Integer) 1);
        assertEquals(definitions.get(0).getName(), "my_custom_1");
        assertEquals(definitions.get(0).getCustomizedType(), "issue");
        assertEquals(definitions.get(0).getFieldFormat(), "string");
        assertEquals(definitions.get(0).getRegexp(), "some.*");
        assertEquals(definitions.get(0).getMinLength(), (Integer) 5);
        assertEquals(definitions.get(0).getMaxLength(), (Integer) 80);
        assertEquals(definitions.get(0).isFilter(), true);
        assertEquals(definitions.get(0).isSearchable(), true);
        assertEquals(definitions.get(0).isMultiple(), false);
        assertEquals(definitions.get(0).isVisible(), true);
        assertEquals(definitions.get(0).isRequired(), false);
        assertEquals(definitions.get(0).getDefaultValue(), "");
        assertEquals(definitions.get(0).getPossibleValues().size(), 0);
        assertEquals(definitions.get(0).getTrackers().get(0).getId(), (Integer)1);
        assertEquals(definitions.get(0).getTrackers().get(1).getId(), (Integer)2);
        assertEquals(definitions.get(0).getTrackers().get(2).getId(), (Integer)3);
        assertEquals(definitions.get(0).getRoles().size(), 0);
        
        assertEquals(definitions.get(1).getId(), (Integer) 2);
        assertEquals(definitions.get(1).getName(), "custom_boolean_1");
        assertEquals(definitions.get(1).getCustomizedType(), "issue");
        assertEquals(definitions.get(1).getFieldFormat(), "bool");
        assertEquals(definitions.get(1).getRegexp(), "");
        assertEquals(definitions.get(1).getMinLength(), null);
        assertEquals(definitions.get(1).getMaxLength(), null);
        assertEquals(definitions.get(1).isFilter(), false);
        assertEquals(definitions.get(1).isSearchable(), false);
        assertEquals(definitions.get(1).isMultiple(), false);
        assertEquals(definitions.get(1).isVisible(), true);
        assertEquals(definitions.get(1).isRequired(), false);
        assertEquals(definitions.get(1).getDefaultValue(), "");
        assertEquals(definitions.get(1).getPossibleValues().get(0), "1");
        assertEquals(definitions.get(1).getPossibleValues().get(1), "0");
        assertEquals(definitions.get(1).getTrackers().size(), 3);
        assertEquals(definitions.get(1).getRoles().size(), 0);
        
        assertEquals(definitions.get(2).getId(), (Integer) 3);
        assertEquals(definitions.get(2).getName(), "Test");
        assertEquals(definitions.get(2).getCustomizedType(), "issue");
        assertEquals(definitions.get(2).getFieldFormat(), "bool");
        assertEquals(definitions.get(2).getRegexp(), "");
        assertEquals(definitions.get(2).getMinLength(), null);
        assertEquals(definitions.get(2).getMaxLength(), null);
        assertEquals(definitions.get(2).isFilter(), false);
        assertEquals(definitions.get(2).isSearchable(), false);
        assertEquals(definitions.get(2).isMultiple(), false);
        assertEquals(definitions.get(2).isVisible(), false);
        assertEquals(definitions.get(2).isRequired(), true);
        assertEquals(definitions.get(2).getDefaultValue(), "1");
        assertEquals(definitions.get(2).getPossibleValues().get(0), "1");
        assertEquals(definitions.get(2).getPossibleValues().get(1), "0");
        assertEquals(definitions.get(2).getTrackers().size(), 0);
        assertEquals(definitions.get(2).getRoles().get(0).getId(), (Integer) 4);
    }

}
