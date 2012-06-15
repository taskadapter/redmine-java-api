package org.redmine.ta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;
import org.junit.Assert;
import org.redmine.ta.beans.Issue;
import org.redmine.ta.beans.Project;
import org.redmine.ta.beans.User;
import org.redmine.ta.internal.RedmineJSONParser;
import org.redmine.ta.internal.json.JsonInput;
import org.junit.Before;
import org.junit.Test;

public class RedmineIssuesMapTest {

    private static final int FILE_1_REQUIRED_ISSUES_NUMBER = 26;

    private List<Issue> issuesList;
    private HashMap<Integer, Issue> issuesMap;

    @Before
    // Is executed before each test method
    public void setup() throws Exception {
		String str = MyIOUtils.getResourceAsString("redmine_issues.json");
		final JSONObject object = RedmineJSONParser.getResponse(str);
		this.issuesList = JsonInput.getListNotNull(object, "issues",
				RedmineJSONParser.ISSUE_PARSER);

        RedmineIssuesMap loader = new RedmineIssuesMap(issuesList);
        issuesMap = loader.getIssuesMap();
    }

    @Test
    public void testAllIssuesAreLoadedToList() {
        Assert.assertEquals("There must be " + FILE_1_REQUIRED_ISSUES_NUMBER + " issues loaded to the list.", FILE_1_REQUIRED_ISSUES_NUMBER, issuesList.size());
    }

    @Test
    public void testMapHasSameSizeAsList() {
        Assert.assertEquals("There must be equal number of issues in map and list", issuesList.size(), issuesMap.size());
    }

    @Test
    public void testListHasIssueWithID39() {
        Integer id = 39;
        Iterator<Issue> it = issuesList.iterator();
        boolean found = false;
        while (it.hasNext()) {
            Issue issue = it.next();
            if (issue.getId().equals(id)) {
                found = true;
                break;
            }
        }
        Assert.assertTrue("The list does not contain issue with ID " + id, found);
    }

    @Test
    public void testMapHasIssueWithID39() {
        Integer id = 39;
        Assert.assertNotNull("The map must contain issue with ID " + id, issuesMap.get(id));
    }

    @Test
    public void testParentId() {
        Integer childId = 67;
        Integer parentIdExpected = 66;
        Issue child = issuesMap.get(childId);
        Assert.assertNotNull("Issue " + childId + " must be in map", child);
        Assert.assertNotNull("Issue " + childId + " must have a parent", child.getParentId());

        // XXX getParent() looks very ugly. need to change the XSD file to generate a better set of java classes
        Integer actualParentId = child.getParentId();
        Assert.assertEquals("Issue " + childId + " must have parent ID " + parentIdExpected, parentIdExpected, actualParentId);

        Issue parent = issuesMap.get(actualParentId);
        Assert.assertNotNull("Parent Issue " + parentIdExpected + " must be present in map", parent);

        Assert.assertEquals("Parent Issue id " + parentIdExpected + " must be equal to the value provided by getId()", parentIdExpected, parent.getId());
    }

    @Test
    public void testNullParentId() {
        Integer childId = 66;
        Issue child = issuesMap.get(childId);
        Assert.assertEquals("Issue " + childId + " must have NULL parent ID ", null, child.getParentId());
    }

    @Test
    public void testPriorityTextIsLoaded() {
        Issue issue = issuesMap.get(39);
        Assert.assertEquals("Comparing 'priority' field.", "High", issue.getPriorityText());
    }

    @Test
    public void testDoneRatioIsLoaded() {
        Issue issue67 = issuesMap.get(67);
        Integer expectedDoneRatio67 = 20;
        Assert.assertEquals("Comparing 'done_ratio' field.", expectedDoneRatio67, issue67.getDoneRatio());

        Issue issue65 = issuesMap.get(65);
        Integer expectedDoneRatio65 = 80;
        Assert.assertEquals("Comparing 'done_ratio' field.", expectedDoneRatio65, issue65.getDoneRatio());

        Issue issue55 = issuesMap.get(55);
        Integer expectedDoneRatio55 = 0;
        Assert.assertEquals("Comparing 'done_ratio' field.", expectedDoneRatio55, issue55.getDoneRatio());
    }

    @Test
    public void testProject1() {
        Issue issue67 = issuesMap.get(67);
        Integer expectedProjectId = 25;
        String expectedProjectName = "test project";
        Project actualProect = issue67.getProject();
        Assert.assertNotNull("Project must be not null", actualProect);
        Assert.assertEquals("Comparing project ID", expectedProjectId, actualProect.getId());
        Assert.assertEquals("Comparing project name", expectedProjectName, actualProect.getName());
    }

    @Test
    public void testAssignee() {
        Issue issue = issuesMap.get(68);
//		String assigneeFullNameExpected = "Redmine Admin";
        Integer assigneeIdExpected = 1;

        User assignee = issue.getAssignee();
        Assert.assertNotNull("Checking assignee info", assignee);
//		assertEquals("Checking assignee info: login", assigneeFullNameExpected, assignee.getFullName());
        Assert.assertEquals("Checking assignee info: id", assigneeIdExpected, assignee.getId());

    }

}
