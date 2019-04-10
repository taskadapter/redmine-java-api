package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.internal.RedmineJSONParser;
import com.taskadapter.redmineapi.internal.json.JsonInput;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectsTest {

    private List<Project> projectsList;

    @Before
    // Is executed before each test method
    public void setup() throws Exception {
		String text = MyIOUtils.getResourceAsString("project/redmine_projects.json");
		final JSONObject object = RedmineJSONParser.getResponse(text);
		projectsList = JsonInput.getListNotNull(object, "projects", RedmineJSONParser::parseProject);
    }

    @Test
    public void testProjectsNumber() {
        assertThat(projectsList.size()).isEqualTo(12);
    }

    @Test
    public void testProjects1Info() {
        Project aceProject = new Project(null).setId(15);
        aceProject.setIdentifier("test1295577237142");
        aceProject.setName("test project 15");

        Project projectFromList = findProjectInList(aceProject.getId());
        assertThat(projectFromList).isNotNull();

        // could use project.equals later when it's implemented in the class
        assertThat(projectFromList.getId()).isEqualTo(aceProject.getId());
        assertThat(projectFromList.getName()).isEqualTo(aceProject.getName());
        assertThat(projectFromList.getIdentifier()).isEqualTo(aceProject.getIdentifier());
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
