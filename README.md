# Redmine/Chiliproject Java API.

* Uses Redmine's REST API (don't forget to enable it in Redmine server settings).
* Does not require any plugins installed on Redmine/Chiliproject server.
* Runs on any standard Java 8+ platform (Android does not have standard Java).
* Supports HTTP proxy
* Supports GZipped responses from servers
* Uses SLF4J for logging. Provide [your own SLF4J binding](http://www.slf4j.org/codes.html#StaticLoggerBinder)
* [Supported Redmine versions] (https://github.com/taskadapter/redmine-java-api/wiki/Redmine-versions-compatibility)
* Available in Maven Central (binary, sources, javadocs).

Gradle dependency:

    dependencies {
        compile 'com.taskadapter:redmine-java-api:<current-version>'
    }

Check the [latest release version in Maven Central](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.taskadapter%22%20AND%20a%3A%22redmine-java-api%22)

# Sample code.

## Get list of issues

    String uri = "https://www.hostedredmine.com";
    String apiAccessKey = "somekey";
    String projectKey = "taskconnector-test";
    Integer queryId = null; // any

    RedmineManager mgr = RedmineManagerFactory.createWithApiKey(uri, apiAccessKey);
    // override default page size if needed
    mgr.setObjectsPerPage(100);
    List<Issue> issues = mgr.getIssueManager().getIssues(projectKey, queryId);
    for (Issue issue : issues) {
        System.out.println(issue.toString());
    }

## Multi-values search for issues


    Params params = new Params()
                .add("set_filter", "1")
                .add("f[]", "summary")
                .add("op[summary]", "~")
                .add("v[summary]", "another")
                .add("f[]", "description")
                .add("op[description]", "~")
                .add("v[description][]", "abc");

    result = issueManager.getIssues(params);
    
Redmine searches for "Open" issues by default. You can specify "all" in your Map if you want:

    params.put("status_id", "*");    

## Get related objects when retrieving issues
    
    issue = issueManager.getIssueById(123, Include.journals, Include.relations, Include.attachments, 
                              Include.changesets, Include.watchers);
    journals = issue.getJournals();


## Create an issue

    RedmineManager mgr = RedmineManagerFactory.createWithApiKey(uri, apiAccessKey);	
	Version ver = VersionFactory.create(512);
	IssueCategory cat = IssueCategoryFactory.create(673);
    ProjectManager projectManager = manager.getProjectManager();
    Project projectByKey = projectManager.getProjectByKey("testid");

    Issue issue = new Issue(mgr.getTransport(), projectId)
        .setSubject("test123")
	    .setTargetVersion(ver)
	    .setCategory(cat);
        .setProjectId(projectId)
        .create();
    
## Get issue by Id
    Issue retrievedIssue = issueManager.getIssueById(123);

## Set custom field value on issue 
    Issue issue = ...
    List<CustomFieldDefinition> customFieldDefinitions = mgr.getCustomFieldManager().getCustomFieldDefinitions();
    // sample implementation for getCustomFieldByName() is in CustomFieldResolver (test class).
    // in prod code you would typically know the custom field name or id already 
    CustomFieldDefinition customField1 = getCustomFieldByName(customFieldDefinitions, "my_custom_1");
    String custom1Value = "some value 123";
    issue.addCustomField(CustomFieldFactory.create(customField1.getId(), customField1.getName(), custom1Value));
    issueManager.update(issue);

## Get all projects

    List<Project> projects = mgr.getProjectManager().getProjects();

## Free-form search for users
    Map<String, String> params = new HashMap<>();
    params.put("name", name);
    List<User> list = userManager.getUsers(params);

## Create a group and add user to it

    Group template = new Group().setName("group " + System.currentTimeMillis());
    Group group = userManager.createGroup(template);
    User newUser = userManager.createUser(UserGenerator.generateRandomUser());
    userManager.addUserToGroup(newUser, group);

##  Delete user
    userManager.deleteUser(123);

## Get time entries
    TimeEntryManager timeEntryManager = redmineManager.getTimeEntryManager();
    final Map<String, String> params = new HashMap<>();
    params.put("project_id", projectId);
    params.put("activity_id", activityId);
    final List<TimeEntry> elements = timeEntryManager.getTimeEntries(params);

## Using a custom (e.g. self-signed) SSL certificate
See IntegrationTestHelper class:

    final Optional<KeyStore> builtInExtension = getExtensionKeystore();
    if (builtInExtension.isPresent()) {
        return RedmineManagerFactory.createConnectionManagerWithExtraTrust(
                Collections.singletonList(builtInExtension.get()));
    }
