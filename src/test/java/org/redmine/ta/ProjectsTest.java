package org.redmine.ta;

import java.util.List;

import org.junit.Assert;
import org.redmine.ta.beans.Project;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ProjectsTest {

    private List<Project> projectsList;

    @Before
    // Is executed before each test method
    public void setup() throws Exception {
		// String text =
		// MyIOUtils.getResourceAsString("redmine_1_1_projects.xml");
		// projectsList = RedmineXMLParser.parseProjectsFromXML(text);
    }

	@Ignore
    @Test
    public void testProjectsNumber() {
        int expectedProjectsNumber = 12;
        Assert.assertEquals("Checking projects number", expectedProjectsNumber, projectsList.size());
    }

	@Ignore
    @Test
    public void testProjects1Info() {
        Project aceProject = new Project();
        aceProject.setId(15);
        aceProject.setIdentifier("test1295577237142");
        aceProject.setName("test project 15");

        Project projectFromList = findProjectInList(aceProject.getId());
        Assert.assertNotNull("Checking project is loaded", projectFromList);

        // could use project.equals later when it's implemented in the class
        Assert.assertEquals("Checking the loaded project info", aceProject.getId(), projectFromList.getId());
        Assert.assertEquals("Checking the loaded project info", aceProject.getName(), projectFromList.getName());
        Assert.assertEquals("Checking the loaded project info", aceProject.getIdentifier(), projectFromList.getIdentifier());
    }

    /*
      * @return NULL, if not found in list
      */
    private Project findProjectInList(int projectDbId) {
        Project result = null;
        for (Project project : projectsList) {
            if (project.getId().equals(projectDbId)) {
                result = project;
                break;
            }
        }
        return result;
    }
}
