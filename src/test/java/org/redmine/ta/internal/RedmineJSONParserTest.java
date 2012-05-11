package org.redmine.ta.internal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.redmine.ta.RedmineFormatException;
import org.redmine.ta.beans.Project;
import org.redmine.ta.beans.Tracker;

/**
 * Redmine JSON parser tests.
 * 
 * @author maxkar
 * 
 */
public class RedmineJSONParserTest {

	@Test
	public void testParseProject1() throws RedmineFormatException,
			ParseException {
		final String projectString = "{\"project\":{\"created_on\":\"2012/05/11 06:53:21 -0700\",\"updated_on\":\"2012/05/11 06:53:20 -0700\",\"homepage\":\"\",\"trackers\":[{\"name\":\"Bug\",\"id\":1},{\"name\":\"Feature\",\"id\":2},{\"name\":\"Support\",\"id\":3}],\"identifier\":\"test1336744548920\",\"name\":\"test project\",\"id\":6143}}";
		final Project project = RedmineJSONParser.parseProject(projectString);

		final Project template = new Project();
		template.setId(Integer.valueOf(6143));
		template.setIdentifier("test1336744548920");
		template.setName("test project");
		template.setHomepage("");
		template.setCreatedOn(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss Z")
				.parse("11.05.2012 06:53:21 -0700"));
		template.setUpdatedOn(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss Z")
				.parse("11.05.2012 06:53:20 -0700"));
		template.setTrackers(Arrays.asList(new Tracker(1, "Bug"), new Tracker(
				2, "Feature"), new Tracker(3, "Support")));
		Assert.assertEquals(template, project);
	}
}
