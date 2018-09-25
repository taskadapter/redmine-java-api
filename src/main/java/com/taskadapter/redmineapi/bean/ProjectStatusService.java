package com.taskadapter.redmineapi.bean;

import java.util.HashMap;
import java.util.Map;

/***
 * <pr>
 *  <ur>Used to manage project status
 *      <li>Use the 3.1 version of the configuration by default</li>
 *      <li>Add new implementation of the version in a static code block</li>
 *  </ur>
 *
 * </pr>
 *
 *@author JackLei
 *@Date 下午 6:24 2018/9/21
 ***/
public class ProjectStatusService {
    private static Map<String ,IProjectStatus> projectStatusMap = new HashMap<>();
    private static final IProjectStatus DEFAULT_STATUS = new ProjectStatusForV31();
    private static String currentVersion="";

    static{
        projectStatusMap.put("3.1.1.stable",new ProjectStatusForV31());
    }


    /***
     * set redmine version
     * @param version
     */
    public static void setRedmineVersion(String version){
        ProjectStatusService.currentVersion = version;
    }

    /***
     *
     * @return the project open status value
     */
    public static int getOpenStatusVal(){
        IProjectStatus projectStatus = projectStatusMap.get(currentVersion);
        if(projectStatus == null){
            projectStatus = DEFAULT_STATUS;
        }
        return projectStatus.openVal();
    }

    /***
     *
     * @return the project close status value
     */
    public static int getCloseStatusVal(){
        IProjectStatus projectStatus = projectStatusMap.get(currentVersion);
        if(projectStatus == null){
            projectStatus = DEFAULT_STATUS;
        }
        return projectStatus.closeVal();
    }
}
