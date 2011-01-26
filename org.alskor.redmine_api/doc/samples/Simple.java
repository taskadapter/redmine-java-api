import java.io.IOException;
import java.util.List;

import org.alskor.redmine.AuthenticationException;
import org.alskor.redmine.NotFoundException;
import org.alskor.redmine.RedmineManager;
import org.alskor.redmine.beans.Issue;

public class Simple {
	private static String redmineHost = "https://www.hostedredmine.com";
	private static String apiAccessKey = "a3221bfcef5750219bd0a2df69519416dba17fc9";
	private static String projectKey = "taskconnector-test";
	private static Integer queryId = null; // any

	public static void main(String[] args) {
		RedmineManager mgr = new RedmineManager(redmineHost, apiAccessKey);
		try {
			tryGetIssues(mgr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void tryGetIssues(RedmineManager mgr) throws IOException, AuthenticationException, NotFoundException {
		List<Issue> issues = mgr.getIssues(projectKey, queryId);
		for (Issue issue : issues) {
			System.out.println(issue.toString());
		}
	}
}
