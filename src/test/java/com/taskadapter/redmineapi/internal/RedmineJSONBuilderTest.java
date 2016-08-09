package com.taskadapter.redmineapi.internal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertTrue;

import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.GroupFactory;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.UserFactory;
import org.junit.Test;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Version;
import com.taskadapter.redmineapi.bean.VersionFactory;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.CustomFieldFactory;
import java.util.Collections;

public class RedmineJSONBuilderTest {

	@Test
	public void priorityIdIsAddedToJsonIfProvided() {
		Issue issue = new Issue();
		issue.setPriorityId(1);
		final String generatedJSON = RedmineJSONBuilder.toSimpleJSON(
                "some_project_key", issue, RedmineJSONBuilder::writeIssue);
		assertTrue(generatedJSON.contains("\"priority_id\":1"));
	}

	/**
	 * Tests whether custom fields are serialized to the JSON of a {@link Version}
	 */
	@Test
	public void customFieldsAreWrittenToVersionIfProvided() {
		Version version = VersionFactory.create(1);
		CustomField field = CustomFieldFactory.create(2, "myName", "myValue");
		version.addCustomFields(Collections.singletonList(field));

		final String json = RedmineJSONBuilder.toSimpleJSON("dummy", version, RedmineJSONBuilder::writeVersion);
		assertTrue(json.contains("\"custom_field_values\":{\"2\":\"myValue\"}"));
	}

	@Test
	public void fieldsExplicitlySetToNullAreAddedToIssueJSonAsNull() {
		Issue issue = IssueFactory.create(null);
		issue.setSubject("subj1");
		issue.setDescription(null);
		issue.setDoneRatio(null);
		issue.setParentId(null);
		issue.setAssigneeId(null);
		issue.setEstimatedHours(null);
		issue.setSpentHours(null);
		issue.setNotes(null);
		final String generatedJSON = RedmineJSONBuilder.toSimpleJSON("some_project_key", issue, RedmineJSONBuilder::writeIssue);
		assertThat(generatedJSON).contains("\"id\":null");
		assertThat(generatedJSON).contains("\"subject\":\"subj1\"");
		assertThat(generatedJSON).contains("\"description\":null");
		assertThat(generatedJSON).contains("\"done_ratio\":null");
		assertThat(generatedJSON).contains("\"parent_issue_id\":null");
		assertThat(generatedJSON).contains("\"assigned_to_id\":null");
		assertThat(generatedJSON).contains("\"estimated_hours\":null");
		assertThat(generatedJSON).contains("\"spent_hours\":null");
		assertThat(generatedJSON).contains("\"notes\":null");
	}

	@Test
	public void onlyExplicitlySetFieldsAreAddedToUserJSon() {
		User user = UserFactory.create();
		user.setLogin("login1");
		user.setMail(null);
		user.setStatus(null);
		final String generatedJSON = RedmineJSONBuilder.toSimpleJSON("some_project_key", user, RedmineJSONBuilder::writeUser);
		assertThat(generatedJSON).contains("\"login\":\"login1\",");
		assertThat(generatedJSON).contains("\"mail\":null");
		assertThat(generatedJSON).contains("\"status\":null");
		assertThat(generatedJSON).doesNotContain("\"id\"");
	}

	@Test
	public void onlyExplicitlySetFieldsAreAddedToGroupJSon() {
		Group groupWithoutName = GroupFactory.create(4);
		final String generatedJSON = RedmineJSONBuilder.toSimpleJSON("some_project_key", groupWithoutName, RedmineJSONBuilder::writeGroup);
		assertThat(generatedJSON).doesNotContain("\"name\"");

		Group groupWithName = GroupFactory.create(4);
		groupWithName.setName("some name");
		final String generatedJSONWithName = RedmineJSONBuilder.toSimpleJSON("some_project_key", groupWithName, RedmineJSONBuilder::writeGroup);
		assertThat(generatedJSONWithName).contains("\"name\":\"some name\"");
	}

}
