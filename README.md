# Redmine/Chiliproject Java API.

* Uses Redmine's REST API (don't forget to enable it in Redmine server settings).
* Does not require any plugins installed on Redmine/Chiliproject server.
* Requires JVM 11 or newer.
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

# Obtain `transport` required for fluent-style calls

The new fluent-style API (v. 4.x) requires a `transport` instance for most calls. 
First, create an instance of RedmineManager and then obtain its transport:

    RedmineManager mgr = RedmineManagerFactory.createWithApiKey(uri, apiAccessKey);
    Transport transport = mgr.getTransport();

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
	Version ver = new Version().setId(512);
	IssueCategory cat = new IssueCategory(transport).setId(673);
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

## Create project

	Project project = new Project(transport)
 				.setName("Upload project")
   				.setIdentifier("uploadtmpproject")
   				.create();

## Get all projects

    List<Project> projects = mgr.getProjectManager().getProjects();

## Free-form search for users
    Map<String, String> params = new HashMap<>();
    params.put("name", name);
    List<User> list = userManager.getUsers(params);

## Create a group and add user to it

    Group group = new Group(transport)
        .setName("group")
        .create();
        
    User user = new User(transport).set(...)
        .create();
        
    userManager.addUserToGroup(user, group);

##  Delete user
    
    user.delete();

## Get time entries
    TimeEntryManager timeEntryManager = redmineManager.getTimeEntryManager();
    final Map<String, String> params = new HashMap<>();
    params.put("project_id", projectId);
    params.put("activity_id", activityId);
    final List<TimeEntry> elements = timeEntryManager.getTimeEntries(params);

## Using a custom (e.g. self-signed) SSL certificate
Supposing you have:
* caTrustStore: a Collection\<KeyStore\> object that has the custom CAs to use

Then the following function will return a RedmineManager object that uses those TrustStores
to connect to Redmine:

    public RedmineManager connectToRedmine(String apiAccessKey, String url) throws IOException {
        try {
            ClientConnectionManaget connectionManager =
                    RedmineManagerFactory.createConnectionManagerWithExtraTrust(caTrustStore);
            HttpClient client = RedmineManagerFactory.getNewHttpClient(url, connectionManager);
            return RedmineManagerFactory.createWithApiKey(url, apiAccessKey, client);
        } catch (Exception e) {
            System.out.println("Could not connect to Redmine");
            throw new IOException(e.getMessage());
        }
    }

For another example, see IntegrationTestHelper class:

    Optional<KeyStore> builtInExtension = getExtensionKeystore();
    if (builtInExtension.isPresent()) {
        return RedmineManagerFactory.createConnectionManagerWithExtraTrust(
                Collections.singletonList(builtInExtension.get()));
    }

## Using a custom (e.g. self-signed) SSL certificate with Client Certificate Authentication
Supposing you have: 
* clientKeyStore: a KeyStore object that has your Client Certificate and Private Key loaded 
* caTrustStore: a Collection\<KeyStore\> object that has the custom CAs to use
* password: A string representing clientKeyStore's password

Then the following function will return a RedmineManager object that uses those KeyStores to
connect to Redmine:

    public RedmineManager connectToRedmine(String apiAccessKey, String url) throws IOException {
        try {
            ClientConnectionManaget connectionManager = 
                    RedmineManagerFactory.createConnectionManagerWithClientCertificate(
                    clientKeyStore, password, caTrustStore);  
            HttpClient client = RedmineManagerFactory.getNewHttpClient(url, connectionManager);
            return RedmineManagerFactory.createWithApiKey(url, apiAccessKey, client);
        } catch (Exception e) {
            System.out.println("Could not connect to Redmine");
            throw new IOException(e.getMessage());
        } 
    }

For another example, see IntegrationTestHelper class:

    Optional<KeyStore> builtInExtension = getExtensionKeystore();
    Optional<KeyStore> builtInClient = getClientKeystore();
    if (builtInExtension.isPresent() && builtInClient.isPresent()) {
        return RedmineManagerFactory.createConnectionManagerWithClientCertificate(builtInClient.get(), 
                "keystore-password", Collections.singletonList(builtInExtension.get()));
    }
