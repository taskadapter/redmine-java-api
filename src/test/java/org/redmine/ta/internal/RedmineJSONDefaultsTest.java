package org.redmine.ta.internal;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.redmine.ta.beans.Project;
import org.redmine.ta.internal.json.JsonObjectParser;

/**
 * Tests for default values for redmine parser.
 * 
 * @author maxkar
 * 
 */
public class RedmineJSONDefaultsTest {
	@Test
	public void testProjectDefaults() throws JSONException {
		final String MINIMAL_PROJECT = "{\"project\":{\"created_on\":\"2012/05/16 01:08:56 -0700\",\"identifier\":\"test1337155905537\",\"homepage\":\"\",\"updated_on\":\"2012/05/16 01:08:56 -0700\",\"trackers\":[{\"name\":\"Bug\",\"id\":1},{\"name\":\"Feature\",\"id\":2},{\"name\":\"Support\",\"id\":3}],\"name\":\"Test name\",\"id\":1060}}";
		final Project project = parse(MINIMAL_PROJECT, "project",
				RedmineJSONParser.PROJECT_PARSER);
		Assert.assertEquals("", project.getDescription());
		Assert.assertEquals("", project.getHomepage());
	}

	private static <T> T parse(String text, String tag,
			JsonObjectParser<T> parser) throws JSONException {
		final JSONObject content = RedmineJSONParser.getResponceSingleObject(
				text, tag);
		return parser.parse(content);
	}
}
