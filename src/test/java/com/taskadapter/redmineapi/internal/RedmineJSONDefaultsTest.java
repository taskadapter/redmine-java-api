package com.taskadapter.redmineapi.internal;

import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.internal.json.JsonObjectParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Tests for default values for redmine parser.
 */
public class RedmineJSONDefaultsTest {
	@Test
	public void testProjectDefaults() throws JSONException {
		final String MINIMAL_PROJECT = "{\"project\":{\"created_on\":\"2012/05/16 01:08:56 -0700\",\"identifier\":\"test1337155905537\",\"homepage\":\"\",\"updated_on\":\"2012/05/16 01:08:56 -0700\",\"trackers\":[{\"name\":\"Bug\",\"id\":1},{\"name\":\"Feature\",\"id\":2},{\"name\":\"Support\",\"id\":3}],\"name\":\"Test name\",\"id\":1060}}";
		final Project project = parse(MINIMAL_PROJECT, "project",
				RedmineJSONParser::parseProject);
		assertEquals("", project.getDescription());
		assertEquals("", project.getHomepage());
	}

	@Test
	public void testIssueDefaults() throws JSONException {
		final String MINIMAL_ISSUE = "{\"issue\":{\"status\":{\"name\":\"New\",\"id\":1},\"author\":{\"name\":\"Redmine Admin\",\"id\":1},\"created_on\":\"2012/05/16 01:25:27 -0700\",\"tracker\":{\"name\":\"Bug\",\"id\":1},\"project\":{\"name\":\"test project\",\"id\":1063},\"spent_hours\":0.0,\"updated_on\":\"2012/05/16 01:25:27 -0700\",\"done_ratio\":0,\"subject\":\"This is a subject\",\"id\":1926,\"custom_fields\":[{\"value\":\"\",\"name\":\"my_custom_1\",\"id\":1},{\"value\":\"on\",\"name\":\"custom_boolean_1\",\"id\":2}],\"priority\":{\"name\":\"Normal\",\"id\":4}}}";
		final Issue issue = parse(MINIMAL_ISSUE, "issue", RedmineJSONParser::parseIssue);
		assertThat(issue.getDescription()).isNull();
	}

	@Test
	public void testTimeEntryDefaults() throws JSONException {
		final String MINIMAL_TIME_ENTRY = "{\"time_entry\":{\"issue\":{\"id\":1931},\"created_on\":\"2012/05/16 01:40:29 -0700\",\"project\":{\"name\":\"test project\",\"id\":1072},\"updated_on\":\"2012/05/16 01:40:29 -0700\",\"user\":{\"name\":\"Redmine Admin\",\"id\":1},\"hours\":123.0,\"id\":166,\"spent_on\":\"2012/05/16\",\"activity\":{\"name\":\"Design\",\"id\":8}}}";
		final TimeEntry entry = parse(MINIMAL_TIME_ENTRY, "time_entry", RedmineJSONParser::parseTimeEntry);
		assertEquals("", entry.getComment());
	}

	@Test
	public void testRelationDefaults() throws JSONException {
		final String MINIMAL_RELATION = "{\"relation\":{\"issue_to_id\":1934,\"issue_id\":1933,\"id\":162,\"relation_type\":\"blocks\"}}";
		final IssueRelation relation = parse(MINIMAL_RELATION, "relation", RedmineJSONParser::parseRelation);
		assertEquals(Integer.valueOf(0), relation.getDelay());
	}

	private static <T> T parse(String text, String tag,
			JsonObjectParser<T> parser) throws JSONException {
		final JSONObject content = RedmineJSONParser.getResponseSingleObject(
            text, tag);
		return parser.parse(content);
	}
}
