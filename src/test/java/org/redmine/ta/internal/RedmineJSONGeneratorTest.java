package org.redmine.ta.internal;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.redmine.ta.beans.Issue;

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
				"some_project_key", issue, RedmineJSONBuilder.ISSUE_WRITER);
		assertTrue(generatedJSON.contains("\"priority_id\":1,"));
	}

}
