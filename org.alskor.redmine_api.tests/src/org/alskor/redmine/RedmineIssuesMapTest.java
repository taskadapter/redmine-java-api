package org.alskor.redmine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class RedmineIssuesMapTest {

	private static final String FILE_1_ISSUES_XML_FILE_NAME = "issues.xml";
	private static final int FILE_1_REQUIRED_ISSUES_NUMBER = 12;
	private List<Issue> issuesList;
	private HashMap<Integer, Issue> issuesMap;

	@Before
	 // Is executed before each test method
	public void setup() throws Exception {
		String str = MyIOUtils.getResourceAsString(FILE_1_ISSUES_XML_FILE_NAME);
		this.issuesList = RedmineManager.parseIssuesFromXML(str);
		
		RedmineIssuesMap loader = new RedmineIssuesMap(issuesList);
		issuesMap = loader.getIssuesMap();
	}
	
	@Test
	public void testAllIssuesAreLoadedToList() {
		assertEquals("There must be " + FILE_1_REQUIRED_ISSUES_NUMBER + " issues loaded to the list.", FILE_1_REQUIRED_ISSUES_NUMBER, issuesList.size());
	}

	@Test
	public void testMapHasSameSizeAsList() {
		assertEquals("There must be equal number of issues in map and list", issuesList.size(), issuesMap.size());
	}
	
	@Test
	public void testListHasIssueWithID18155() {
		Integer id = 18155;
		Iterator<Issue> it = issuesList.iterator();
		boolean found = false;
		while (it.hasNext()) {
			Issue issue = it.next();
			if (issue.getId().equals(id)){
				found = true;
				break;
			}
		}
		assertTrue("The list does not contain issue with ID " + id, found);
	}

	@Test
	public void testMapHasIssueWithID18155() {
		Integer id = 18155;
		assertNotNull("The map must contain issue with ID " + id, issuesMap.get(id));
	}
	
	@Test
	public void testParentId(){
		Integer childId = 18156;
		Integer parentIdExpected = 18154;
		Issue child = issuesMap.get(childId);
		assertNotNull("Issue " + childId + " must be in map", child);
		assertNotNull("Issue " + childId + " must have a parent", child.getParentId());
		
		// XXX getParent() looks very ugly. need to change the XSD file to generate a better set of java classes
		Integer actualParentId = child.getParentId();
		assertEquals("Issue " + childId + " must have parent ID " + parentIdExpected, parentIdExpected, actualParentId);
		
		Issue parent = issuesMap.get(actualParentId);
		assertNotNull("Parent Issue " + parentIdExpected + " must be present in map", parent);
		
		assertEquals("Parent Issue id " + parentIdExpected + " must be equal to the value provided by getId()", parentIdExpected, parent.getId());
	}

	@Test
	public void testNullParentId(){
		Integer childId = 17318;
		Issue child = issuesMap.get(childId);
		assertEquals("Issue " + childId + " must have NULL parent ID ", null, child.getParentId());
	}
	
	@Test
	public void testPriorityTextIsLoaded(){
		Issue issue = issuesMap.get(18156);
		assertEquals("Comparing 'priority' field.", "Normal", issue.getPriorityText());
	}
	
	@Test
	public void testDoneRatioIsLoaded(){
		Issue issue17319 = issuesMap.get(17319);
		Integer expectedDoneRatio17319 = 60;
		assertEquals("Comparing 'done_ratio' field.", expectedDoneRatio17319, issue17319.getDoneRatio());
		
		Issue issue17318 = issuesMap.get(17318);
		Integer expectedDoneRatio17318 = 33;
		assertEquals("Comparing 'done_ratio' field.", expectedDoneRatio17318, issue17318.getDoneRatio());

		Issue issue17481 = issuesMap.get(17481);
		Integer expectedDoneRatio17481 = 0;
		assertEquals("Comparing 'done_ratio' field.", expectedDoneRatio17481, issue17481.getDoneRatio());
	}

	@Test
	public void testProject1(){
		Issue issue17611 = issuesMap.get(17611);
		Integer expectedProjectId = 9579;
		String expectedProjectName = "ace";
		Project actualProect = issue17611.getProject();
		assertNotNull("Project must be not null", actualProect);
		assertEquals("Comparing project ID", expectedProjectId, actualProect.getId());
		assertEquals("Comparing project name", expectedProjectName, actualProect.getName());
	}
	
	@Test
	public void testAssignee(){
		Issue issue = issuesMap.get(18156);
		String assigneeNameExpected = "Alexey Skor";
		Integer assigneeIdExpected = 19640;
		
		User assignee = issue.getAssignee();
		assertNotNull("Checking assignee info", assignee);
		assertEquals("Checking assignee info: name", assigneeNameExpected, assignee.getFullName());
		assertEquals("Checking assignee info: id", assigneeIdExpected, assignee.getId());
		
	}

}
