package com.taskadapter.redmineapi.bean;

/***
 * the porject status configuration
 *@author JackLei
 *@Date 下午 6:10 2018/9/21
 ***/
public interface IProjectStatus {

    /***
     *
     * @return the project open status val
     */
    int openVal();

    /***
     *
     * @return the project close status val
     */
    int closeVal();

    /***
     *
     * @return  redmine version
     */
    String version();
}
