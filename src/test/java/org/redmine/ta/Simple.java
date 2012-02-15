package org.redmine.ta;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.redmine.ta.RedmineManager.INCLUDE;
import org.redmine.ta.beans.*;
import org.redmine.ta.internal.RedmineXMLGenerator;
import org.redmine.ta.internal.logging.Logger;
import org.redmine.ta.internal.logging.LoggerFactory;

public class Simple {
    private static Logger logger = LoggerFactory.getLogger(Simple.class);
    private static String projectKey = "test";

    private static Integer queryId = null; // any

    public static void main(String[] args) {
        String redmineHost = "http://ta-dev.dyndns.biz:8099/redmine-1.3.1";
        String apiAccessKey = "e665eabdddfa3744e3cbea0f122445d098f2f4b2";
        RedmineManager mgr = new RedmineManager(redmineHost, apiAccessKey);
        try {
//			getIssueWithRelations(mgr);
//			tryCreateIssue(mgr);
//			tryGetIssues(mgr);
//            tryGetAllIssues(mgr);
//			printCurrentUser(mgr);
//			generateXMLForUser();
//			generateXMLForTimeEntry();
//			getSavedQueries(mgr);
//			getProjects(mgr);
//			tryCreateRelation(mgr);
//            tryGetNews(mgr);
//            getProject(mgr);
            changeIssueStatus(mgr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void changeIssueStatus(RedmineManager mgr) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        Issue issue = mgr.getIssueById(1771);
        issue.setSubject("new");
        mgr.update(issue);
    }

    private static void getProject(RedmineManager mgr) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        Project test = mgr.getProjectByKey("test");
        System.out.println(test);
    }

    private static void tryGetNews(RedmineManager mgr) throws IOException, AuthenticationException, RedmineException, NotFoundException {
        List<News> news = mgr.getNews(null);
        for (News aNew : news) {
            System.out.println(aNew);
        }
    }

    private static void tryCreateRelation(RedmineManager mgr) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        IssueRelation r = mgr.createRelation(49, 50, IssueRelation.TYPE.precedes.toString());
        logger.debug("Created relation " + r);
    }

    private static void getProjects(RedmineManager mgr) throws IOException, AuthenticationException, RedmineException {
        List<Project> projects = mgr.getProjects();
        logger.debug("Retrieved projects " + projects);

    }

    private static void getSavedQueries(RedmineManager mgr) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        List<SavedQuery> savedQueries = mgr.getSavedQueries("test");
        logger.debug("Retrieved queries " + savedQueries);

    }

    private static void getIssueWithRelations(RedmineManager mgr) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        Issue issue = mgr.getIssueById(24580, INCLUDE.relations);
        List<IssueRelation> r = issue.getRelations();
        logger.debug("Retrieved relations " + r);

    }

    private static void tryCreateIssue(RedmineManager mgr) throws IOException, AuthenticationException, NotFoundException, RedmineException {
        Issue issue = new Issue();
        issue.setSubject("test123");
        mgr.createIssue(projectKey, issue);
    }

    private static void generateXMLForTimeEntry() {
        TimeEntry o = new TimeEntry();
        o.setId(13);
        o.setIssueId(45);
        o.setActivityId(3);
        o.setProjectId(55);
        o.setUserId(66);
        o.setHours(123f);
        o.setComment("text here");
        o.setSpentOn(new Date());
        String xml = RedmineXMLGenerator.toXML(o);
        logger.debug(xml);
    }

    private static void generateXMLForUser() {
        User u = new User();
        u.setLogin("newlogin");
        String xml = RedmineXMLGenerator.toXML(u);
        logger.debug(xml);

    }

    private static void tryGetIssues(RedmineManager mgr) throws Exception {
        List<Issue> issues = mgr.getIssues(projectKey, queryId);
        for (Issue issue : issues) {
            logger.debug(issue.toString());
        }
    }

    private static void tryGetAllIssues(RedmineManager mgr) throws Exception {
        List<Issue> issues = mgr.getIssues(projectKey, null);
        for (Issue issue : issues) {
            logger.debug(issue.toString());
        }
    }

    private static void printCurrentUser(RedmineManager mgr) throws Exception {
        User currentUser = mgr.getCurrentUser();
        logger.debug("user=" + currentUser.getMail());

        currentUser.setMail("ne@com123.com");
        mgr.update(currentUser);
        logger.debug("updated user");

        User currentUser2 = mgr.getCurrentUser();
        logger.debug("updated user's mail: " + currentUser2.getMail());

    }
}
