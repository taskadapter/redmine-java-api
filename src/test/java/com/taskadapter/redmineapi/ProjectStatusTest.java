package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.ProjectStatusService;
import org.junit.Test;

import java.util.List;

/*** project status test
 *@author JackLei
 *@Date 下午 5:51 2018/9/21
 ***/
public class ProjectStatusTest {

    @Test
    public void testProjectStatus() throws RedmineException {
        RedmineManager mgr = IntegrationTestHelper.createRedmineManagerWithAPIKey();

        ProjectManager projectManager = mgr.getProjectManager();
        List<Project> projects = projectManager.getProjects();

        ProjectStatusService.setRedmineVersion("3.1.1.stable");

        System.out.println("=== set status before ===");
        for(Project project : projects){
            System.out.println("project status:" +project.isOpenStatus());
        }
        System.out.println("=== set status after ===");
        for(Project project : projects){
            project.toCloseStatus();
            //projectManager.update(project);
        }

        for(Project project : projects){
            System.out.println("project status:" +project.isOpenStatus());
        }
    }
}
