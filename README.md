# Redmine/Chiliproject Java API.

* Uses Redmine's REST API (don't forget to enable it in Redmine server settings).
* Does not require any plugins installed on Redmine/Chiliproject server.
* Runs on any standard Java 7+ platform (Android does not have standard Java).
* Supports HTTP proxy
* Supports GZipped responses from servers
* Uses SLF4J for logging. Provide [your own SLF4J binding](http://www.slf4j.org/codes.html#StaticLoggerBinder)
* [Supported Redmine versions] (https://github.com/taskadapter/redmine-java-api/wiki/Redmine-versions-compatibility)
* Available in Maven Central (binary plus sources).

We recommend using a modern build system like Gradle to build this library locally.
Gradle dependency:

    dependencies {
        compile 'com.taskadapter:redmine-java-api:<current-version>'
    }

Check the [latest release version in Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.taskadapter%22%20AND%20a%3A%22redmine-java-api%22)

You can also use some legacy system like Maven if you can't upgrade to Gradle for some reason.

# Sample code.

## Get list of issues

    String uri = "https://www.hostedredmine.com";
    String apiAccessKey = "somekey";
    String projectKey = "taskconnector-test";
    Integer queryId = null; // any

    RedmineManager mgr = RedmineManagerFactory.createWithApiKey(uri, apiAccessKey);
    List<Issue> issues = mgr.getIssueManager().getIssues(projectKey, queryId);
    for (Issue issue : issues) {
        System.out.println(issue.toString());
    }
    
## Get related objects when retrieving issues
    
    issue = issueManager.getIssueById(123, Include.journals, Include.relations, Include.attachments, 
                              Include.changesets, Include.watchers);
    journals = issue.getJournals();


## Create an issue

    Issue issue = IssueFactory.createWithSubject("test123");
	Version ver = VersionFactory.create(512);
	issue.setTargetVersion(ver);
	IssueCategory cat = IssueCategoryFactory.create(673);
	issue.setCategory(cat);
    ProjectManager projectManager = manager.getProjectManager();
    Project projectByKey = projectManager.getProjectByKey("testid");
    issue.setProject(projectByKey);
    manager.getIssueManager().createIssue(issue);

## Get issue by Id
    Issue retrievedIssue = issueManager.getIssueById(123);

## Get all projects

    List<Project> projects = mgr.getProjectManager().getProjects();

## Free-form search for users
    Map<String, String> params = new HashMap<>();
    params.put("name", name);
    List<User> list = userManager.getUsers(params);

## Create a group and add user to it

    Group template = GroupFactory.create("group " + System.currentTimeMillis());
    Group group = userManager.createGroup(template);
    User newUser = userManager.createUser(UserGenerator.generateRandomUser());
    userManager.addUserToGroup(newUser, group);

##  Delete user
    userManager.deleteUser(123);

