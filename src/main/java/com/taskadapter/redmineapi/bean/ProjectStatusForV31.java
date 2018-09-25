package com.taskadapter.redmineapi.bean;

/***
 * the project status configuration for version 3.1
 *@author JackLei
 *@Date 下午 6:22 2018/9/21
 ***/
public class ProjectStatusForV31 implements IProjectStatus{

    @Override
    public int openVal() {
        return 1;
    }

    @Override
    public int closeVal() {
        return 5;
    }

    @Override
    public String version() {
        return "3.1.1.stable";
    }
}
