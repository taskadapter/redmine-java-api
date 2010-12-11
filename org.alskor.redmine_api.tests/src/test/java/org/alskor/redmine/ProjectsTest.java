package org.alskor.redmine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.alskor.redmine.beans.Project;
import org.junit.Before;
import org.junit.Test;

import com.alskor.taskadapter.util.MyIOUtils;


public class ProjectsTest {

	private List<Project> projectsList;

	@Before
	// Is executed before each test method
	public void setup() throws Exception {
		String text = MyIOUtils.getResourceAsString("projects.xml");
		projectsList = RedmineManager.parseProjectsFromXML(text);
	}

	@Test
	public void testProjectsNumber() {
		int expectedProjectsNumber = 6;
		assertEquals("Checking projects number" , expectedProjectsNumber, projectsList.size());
	}

	@Test
	public void testProjects1Info() {
		Project aceProject = new Project();
		aceProject.setId(7);
		aceProject.setIdentifier("ace");
		aceProject.setName("ace");

		Project projectFromList = findProjectInList(aceProject.getId());
		assertNotNull("Checking project is loaded", projectFromList);
		
		// could use project.equals later when it's implemented in the class
		assertEquals("Checking the loaded project info" , aceProject.getId(), projectFromList.getId());
		assertEquals("Checking the loaded project info" , aceProject.getName(), projectFromList.getName());
		assertEquals("Checking the loaded project info" , aceProject.getIdentifier(), projectFromList.getIdentifier());
	}

	/*
	 * @return NULL, if not found in list
	 */
	private Project findProjectInList(int projectDbId) {
		Project result = null;
		Iterator<Project> it = projectsList.iterator();
		while (it.hasNext()) {
			Project project = it.next();
			if (project.getId().equals(projectDbId)) {
				result = project;
				break;
			}
		}
		return result;
	}
}
