package com.taskadapter.redmineapi.internal;

import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.CustomFieldFactory;
import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.Version;
import org.junit.Test;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertTrue;

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
		Version version = new Version().setId(1);
		CustomField field = CustomFieldFactory.create(2, "myName", "myValue");
		version.addCustomFields(Collections.singletonList(field));

		final String json = RedmineJSONBuilder.toSimpleJSON("dummy", version, RedmineJSONBuilder::writeVersion);
		assertTrue(json.contains("\"custom_field_values\":{\"2\":\"myValue\"}"));
	}

	@Test
	public void fieldsExplicitlySetToNullAreAddedToIssueJSonAsNull() {
		Issue issue = new Issue().setId(null)
				.setSubject("subj1")
				.setDescription(null)
				.setDoneRatio(null)
				.setParentId(null)
				.setAssigneeId(null)
				.setEstimatedHours(null)
				.setSpentHours(null)
				.setNotes(null);
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
	public void actualStartAndDueDatesAreSetAndAreAddedToIssueJson() {
		Issue issue = new Issue()
				.setActualStartDate(new GregorianCalendar(2021, Calendar.APRIL, 23).getTime())
				.setActualDueDate(new GregorianCalendar(2021, Calendar.APRIL, 25).getTime());
		final String generatedJson = RedmineJSONBuilder.toSimpleJSON("some_project_key", issue, RedmineJSONBuilder::writeIssue);
		assertThat(generatedJson).contains("\"actual_start_date\":\"2021-04-23\"");
		assertThat(generatedJson).contains("\"actual_due_date\":\"2021-04-25\"");
	}

	@Test
	public void onlyExplicitlySetFieldsAreAddedToUserJSon() {
		User user = new User(null)
				.setLogin("login1")
				.setMail(null)
				.setStatus(null);
		final String generatedJSON = RedmineJSONBuilder.toSimpleJSON("some_project_key", user, RedmineJSONBuilder::writeUser);
		assertThat(generatedJSON).contains("\"login\":\"login1\",");
		assertThat(generatedJSON).contains("\"mail\":null");
		assertThat(generatedJSON).contains("\"status\":null");
		assertThat(generatedJSON).doesNotContain("\"id\"");
	}

	@Test
	public void onlyExplicitlySetFieldsAreAddedToGroupJSon() {
		Group groupWithoutName = new Group(null).setId(4);
		final String generatedJSON = RedmineJSONBuilder.toSimpleJSON("some_project_key", groupWithoutName, RedmineJSONBuilder::writeGroup);
		assertThat(generatedJSON).doesNotContain("\"name\"");

		Group groupWithName = new Group(null).setId(4);
		groupWithName.setName("some name");
		final String generatedJSONWithName = RedmineJSONBuilder.toSimpleJSON("some_project_key", groupWithName, RedmineJSONBuilder::writeGroup);
		assertThat(generatedJSONWithName).contains("\"name\":\"some name\"");
	}

}
