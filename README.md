# Redmine/Chiliproject Java API.

* Uses Redmine's REST API (don't forget to enable it in Redmine server settings).
* Does not require any plugins installed on Redmine/Chiliproject server.
* Runs on any standard Java 6+ platform.
* Supports HTTP proxy
* Supports GZipped responses from servers
* Uses SLF4J for logging. Provide [your own SLF4J binding](http://www.slf4j.org/codes.html#StaticLoggerBinder)
* [Supported Redmine versions] (https://github.com/taskadapter/redmine-java-api/wiki/Redmine-versions-compatibility)
* Available in Maven Central.

Gradle dependency:

    dependencies {
        compile 'com.taskadapter:redmine-java-api:2.2.0'
    }

Check the [latest release version in Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.taskadapter%22%20AND%20a%3A%22redmine-java-api%22)

# Sample code.

Sample code to retrieve list of Redmine issues:

    String uri = "https://www.hostedredmine.com";
    String apiAccessKey = "a3221bfcef5750219bd0a2df69519416dba17fc9";
    String projectKey = "taskconnector-test";
    Integer queryId = null; // any

    RedmineManager mgr = RedmineManagerFactory.createWithApiKey(uri, apiAccessKey);
    List<Issue> issues = mgr.getIssueManager().getIssues(projectKey, queryId);
    for (Issue issue : issues) {
        System.out.println(issue.toString());
    }
