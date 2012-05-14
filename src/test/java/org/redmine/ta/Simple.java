package org.redmine.ta;

import java.util.List;

import org.redmine.ta.RedmineManager.INCLUDE;
import org.redmine.ta.beans.*;
import org.redmine.ta.internal.logging.Logger;
import org.redmine.ta.internal.logging.LoggerFactory;

public class Simple {
    private static final Logger logger = LoggerFactory.getLogger(Simple.class);
	private static final String projectKey = "test1336839552181";

    private static final Integer queryId = null; // any

    public static void main(String[] args) {
        String redmineHost = "http://ta-dev.dyndns.biz:8099/redmine-1.3.1";
        String apiAccessKey = "e665eabdddfa3744e3cbea0f122445d098f2f4b2";
        RedmineManager mgr = new RedmineManager(redmineHost, apiAccessKey);
        try {
			// getIssueWithRelations(mgr);
			tryCreateIssue(mgr);
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
//            changeIssueStatus(mgr);
			// getVersion(mgr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@SuppressWarnings("unused")
    private static void getVersion(RedmineManager mgr) throws RedmineException {
        // see Redmine bug http://www.redmine.org/issues/10241
        Version version = mgr.getVersionById(294);
        System.out.println(version);
    }

	@SuppressWarnings("unused")
	private static void changeIssueStatus(RedmineManager mgr)
			throws RedmineException {
        Issue issue = mgr.getIssueById(1771);
        issue.setSubject("new");
        mgr.update(issue);
    }

	@SuppressWarnings("unused")
    private static void getProject(RedmineManager mgr) throws RedmineException {
        Project test = mgr.getProjectByKey("test");
        System.out.println(test);
    }

	@SuppressWarnings("unused")
    private static void tryGetNews(RedmineManager mgr) throws RedmineException {
        List<News> news = mgr.getNews(null);
        for (News aNew : news) {
            System.out.println(aNew);
        }
    }

	@SuppressWarnings("unused")
    private static void tryCreateRelation(RedmineManager mgr) throws RedmineException {
        IssueRelation r = mgr.createRelation(49, 50, IssueRelation.TYPE.precedes.toString());
        logger.debug("Created relation " + r);
    }

	@SuppressWarnings("unused")
    private static void getProjects(RedmineManager mgr) throws RedmineException {
        List<Project> projects = mgr.getProjects();
        logger.debug("Retrieved projects " + projects);

    }

	@SuppressWarnings("unused")
    private static void getSavedQueries(RedmineManager mgr) throws RedmineException {
        List<SavedQuery> savedQueries = mgr.getSavedQueries("test");
        logger.debug("Retrieved queries " + savedQueries);

    }

	@SuppressWarnings("unused")
    private static void getIssueWithRelations(RedmineManager mgr) throws RedmineException {
		Issue issue = mgr.getIssueById(22751, INCLUDE.relations);
        List<IssueRelation> r = issue.getRelations();
        logger.debug("Retrieved relations " + r);

    }

	@SuppressWarnings("unused")
    private static void tryCreateIssue(RedmineManager mgr) throws RedmineException {
        Issue issue = new Issue();
        issue.setSubject("test123");
		final Version ver = new Version();
		ver.setId(512);
		issue.setTargetVersion(ver);
		final IssueCategory cat = new IssueCategory();
		cat.setId(673);
		issue.setCategory(cat);
        mgr.createIssue(projectKey, issue);
    }


	@SuppressWarnings("unused")
    private static void tryGetIssues(RedmineManager mgr) throws Exception {
        List<Issue> issues = mgr.getIssues(projectKey, queryId);
        for (Issue issue : issues) {
            logger.debug(issue.toString());
        }
    }

	@SuppressWarnings("unused")
    private static void tryGetAllIssues(RedmineManager mgr) throws Exception {
        List<Issue> issues = mgr.getIssues(projectKey, null);
        for (Issue issue : issues) {
            logger.debug(issue.toString());
        }
    }

	@SuppressWarnings("unused")
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
