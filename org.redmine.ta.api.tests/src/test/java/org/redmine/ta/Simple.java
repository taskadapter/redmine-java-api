package org.redmine.ta;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.redmine.ta.RedmineManager.INCLUDE;
import org.redmine.ta.beans.Issue;
import org.redmine.ta.beans.IssueRelation;
import org.redmine.ta.beans.SavedQuery;
import org.redmine.ta.beans.TimeEntry;
import org.redmine.ta.beans.User;
import org.redmine.ta.internal.RedmineXMLGenerator;

public class Simple {
//	private static String redmineHost = "https://www.hostedredmine.com";
//	private static String apiAccessKey = "a3221bfcef5750219bd0a2df69519416dba17fc9";
//	private static String projectKey = "taskconnector-test";
    
//	private static String redmineHost = "http://192.168.0.30:3000";
//    private static String apiAccessKey = "cf6de1494fcca2b5c9206179f6621abeea15c6c7";
	
	private static String redmineHost = "http://192.168.0.64:3000";
    private static String login = "admin";
    private static String password = "admin";
	
	private static String projectKey = "test";

	private static Integer queryId = null; // any

	public static void main(String[] args) {
		RedmineManager mgr = new RedmineManager(redmineHost, login, password);
		try {
//			getIssueWithRelations(mgr);
//			tryCreateIssue(mgr);
//			tryGetIssues(mgr);
//			printCurrentUser(mgr);
//			generateXMLForUser();
//			generateXMLForTimeEntry();
			getSavedQueries(mgr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void getSavedQueries(RedmineManager mgr) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		List<SavedQuery> savedQueries = mgr.getSavedQueries("test");
		System.out.println(savedQueries);
		
	}

	private static void getIssueWithRelations(RedmineManager mgr) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		Issue issue = mgr.getIssueById(24580, INCLUDE.relations);
		List<IssueRelation> r = issue.getRelations();
		System.out.println(r);
		
	}

	private static void tryCreateIssue(RedmineManager mgr) throws IOException, AuthenticationException, NotFoundException, RedmineException {
		Issue issue = new Issue();
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
		System.out.println(xml);
	}

	private static void generateXMLForUser() {
		User u = new User();
		u.setLogin("newlogin");
		String xml = RedmineXMLGenerator.toXML(u);
		System.out.println(xml);
		
	}

	private static void tryGetIssues(RedmineManager mgr) throws Exception {
		List<Issue> issues = mgr.getIssues(projectKey, queryId);
		for (Issue issue : issues) {
			System.out.println(issue.toString());
		}
	}

	private static void printCurrentUser(RedmineManager mgr) throws Exception {
		User currentUser = mgr.getCurrentUser();
		System.out.println("user=" + currentUser.getMail());
		
		currentUser.setMail("ne@com123.com");
		mgr.updateUser(currentUser);
		System.out.println("updated user");

		User currentUser2 = mgr.getCurrentUser();
		System.out.println("updated user's mail: " + currentUser2.getMail());

	}
}
