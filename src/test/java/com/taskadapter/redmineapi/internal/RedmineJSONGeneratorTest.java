package com.taskadapter.redmineapi.internal;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Version;
import com.taskadapter.redmineapi.bean.VersionFactory;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.CustomFieldFactory;
import java.util.Collections;

public class RedmineJSONGeneratorTest {
	/**
	 * Ported regression test for
	 * http://code.google.com/p/redmine-java-api/issues/detail?id=98 from
	 * RedmineXMLGeneratorTest
	 */
	@Test
	public void priorityIdIsAddedToXMLIfProvided() {
		Issue issue = new Issue();
		issue.setPriorityId(1);
		final String generatedJSON = RedmineJSONBuilder.toSimpleJSON(
                "some_project_key", issue, RedmineJSONBuilder::writeIssue);
		assertTrue(generatedJSON.contains("\"priority_id\":1,"));
	}

	/**
	 * Tests whether custom fields are serialized to the JSON of a {@link Version}
	 */
	@Test
	public void customFieldsAreWrittenToVersionIfProvided() {
		Version version = VersionFactory.create(1);
		CustomField field = CustomFieldFactory.create(2, "myName", "myValue");
		version.addCustomFields(Collections.singletonList(field));

		final String generatedJSON = RedmineJSONBuilder.toSimpleJSON(
				"dummy", version, RedmineJSONBuilder::writeVersion);
		assertTrue(generatedJSON.contains("\"custom_field_values\":{\"2\":\"myValue\"}"));
	}
}
