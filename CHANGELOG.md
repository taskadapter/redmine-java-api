# 4.0.0.rc4 (2021-04-05)
* support Redmine 4 "delete object" responses. Redmine 4 returns an empty response for at least some "Delete" calls,
and the library did not know how to handle those. I added null entity handling in TransportDecoder.

* support Redmine 4 new "issue relation" format.
Redmine 4 has a backward incompatible change in its Issue Relations REST API: it now requires "issue_to_id" parameter
to be a comma-separated string when creating issue relations (instead of previous single number).
this change adds support for the new format. I tested this with both Redmine 3.4.5 and 4.2.0.

# 4.0.0.rc3 (2021-04-04)
* added support for downloading files (#358)
* fixed a bug in Transport class constructor: the provided client was not used

# 4.0.0.rc2
* Version 4.x is now built with Java 11 target. Java 8 target is no longer supported for 4.x releases. You can now use Java 11 syntax in the code.

# 4.0.0.rc1
* Fluent-style API. see README for details

# 3.1.3 (2020-01-22)
* fixed a bug in URI creation that happens with Apache httpclient 4.5.6 or newer.

# 3.1.2 (2019-02-13)
* Issue 312: exposed "getTransport" in RedmineManager class to allow custom requests
* Issue 313: added some missing params for user creation
* Issue 315: support for wiki attachments
* fluent API for User, WikiPage, WikiPageDetails classes  

# 3.1.1 (2018-09-21)
* integrated project membership methods from MembershipManager into ProjectManager
(MembershipManager is now deprecated)
* use newer version of apache httpclient: 4.5.1 -> 4.5.6
* use newer version of slf4j-api: 1.7.12 -> 1.7.25
* use slf4j 1.7.12 instead of 1.7.1

# Version 3.1.0 (2017-12-18)

* new method in Transport class to expose some Json results
* Added support for nonProxy host (using standard java.net.ProxySelector) 
* Explicitly specify content length when uploading an attachment

# Version 3.0.2 (2017-12-15)

* Support custom fields for TimeEntry class

# Version 3.0.1 (July 7, 2017)

* Issue #275 getAuthorName returns firstname + NULL when last name is null bug
* Issue #273 Allow private notes
 
# Version 3.0.0 (August 15, 2016)

No functional changes since 3.0.0-RC2. Just a small test fix.
See changes list for v. 3.0.0-RC1 and 3.0.0-RC2.

# Version 3.0.0-RC2 (August 13, 2016)

* Issue #262 Support 'inherit_members' when creating projects
* Issue #260 Wrap direct search results in ResultsWrapper to allow access to paging ginfo
* some minor bugfixes

# Version 3.0.0-RC1 (August 10, 2016)

* Code compiled for Java 8. From now on this library requires Java 8.
* Issue #61. Support for removing fields: internal storage in "beans" is completely rewritten to support https://github.com/taskadapter/redmine-java-api/issues/61
* Support for "is public" field for Projects https://github.com/taskadapter/redmine-java-api/pull/246
* Bug fix: download attachment using Access Key https://github.com/taskadapter/redmine-java-api/pull/202
* Support for creating issues with multiple attachments. https://github.com/taskadapter/redmine-java-api/issues/194
* Support for attachment uploads with explicitly set length. https://github.com/taskadapter/redmine-java-api/issues/78
* Flattened dependency graphs for User, Issue, etc. (e.g. User assignee instance is replaced with assigneeId and assigneeName) 
* Issue #121. Bug fixed: download attachments using API access key
* Support for deleting attachments. https://github.com/taskadapter/redmine-java-api/issues/106

See full list here: https://github.com/taskadapter/redmine-java-api/milestone/27 

#### Migration guide for version 3.0.0:

Issue class:
* Method deleted:    public String getCustomField(String fieldName).
 use the new getCustomFieldByName() method instead

IssueFactory class:
* Method deleted: public static Issue createWithSubject(String subject).
 Use create(int projectId, String subject) instead.

IssueManager class:
* Method deleted: createIssue(String projectKey, Issue issue).
 Use createIssue(Issue issue) instead. The old method had ambiguity in it: not clear which project key value
 to use when it is also set in the Issue itself.

A new TimeEntryManager is introduced. Its methods were extacted from IssueManager.
 
# Version 2.6.0 (November 11, 2015)
  
## ! This version has breaking API changes to address issue #222!
  
Migration for RedmineManagerFactory:

* createShortTermConfig(connectionManager) -> getNewHttpClient(connectionManager)
* createUnauthenticated(String uri, TransportConfiguration config) -> createUnauthenticated(String uri, HttpClient httpClient)
* similarly for other createXX() methods that required TransportConfiguration - they now work with HttpClient
  directly.
  
Also changes to address threading issues:

* deleted Pooling connections. createConnectionManagerWithExtraTrust(), createDefaultConnectionManager()
 and all similar methods now return ClientConnectionManager instead of PoolingClientConnectionManager.
* no more shutdown() method in RedmineManager. if you want to close HttpClient connections, do it directly
on HttpClient you create.
* no more createShortTermConfig() and createLongTermConfiguration() methods. create your own HttpClient
instances and pass them to RedmineManagerFactory if you want to control connection pooling, evictions, etc.

Other changes in 2.6.0:

* Apache HttpClient dependency is updated from 4.2 to v. 4.5.1
* Bug fixed: #229 "Unsupported class class Attachment" when calling attachmentManager.getAttachmentById() bug 

Internal changes in 2.6.0 (should not affect end-users):

* Integration tests are switched from HTTP to HTTPS to make sure HTTPS is supported alright.
* Switched from JUnit 4.10 to 4.12 (test scope dependency - will not affect consumers)
* Switched testing library from Fest-assert to AssertJ  (test scope dependency - will not affect consumers)


# Version 2.5.0 (October 17, 2015)

* Issue 214. new feature: support custom fields for Versions.
* Issue 219. new feature: support for additional keystores to enable SSL connections to servers
 with certificates issued by lesser-known authorities.
* Issue 220. minor improvement: added a method to GroupFactory to create Groups. 

# Version 2.4.0 (August 9, 2015)

* Issue 209 support free-form search for users
* when an object with NULL id is given to update() method in Transport class, it fails with NPE 
* add create(id) method to TrackerFactory class enhancement 
* Fix reading group members for memberships and fix failing unittest enhancement 

# Version 2.3.0 (July 11, 2015)

* Issue #201 Save custom fields for projects.
* Issue #195 Change in RedmineInternalError: it was derived from Error, now it extends RuntimeException
* Issue #114 #188 BREAKING change! getIssues(params map) does NOT handle paging anymore, you can/must use your own limit&offset http params.
note: same for all other object lists - Issue, User, Group, Project, ....
See detailed explanation for this change here: https://github.com/taskadapter/redmine-java-api/issues/114 .
The original API usage model was "load all objects at once" and fetching individual pages was never a goal.
OLD BEHAVIOR: You could previously use setObjectsPerPage() method on RedmineManager, but this only affected the internal page size used for loading issues. clients would only see the final result with all objects anyway.
Backward compatibility is important, of course. But in this case this method is totally misleading as couple people complained.
I believe fixing this bug is more important than maintaining compatibility.
Git commit 82d29bb17c236b87efc834f7dda15b6d0df68da4

# Version 2.2.0 (March 18, 2015)

* Feature #183 Support Redmine 3.0.x date formats. Redmine 3.0.x has date formats that are different from v. 2.6.x
* Feature #184 New methods: ProjectManager:getProjectById() and MembershipManager:getMemberships(int projectId)
* Updates in javadoc

# Version 2.1.0 (January 25, 2015)

* Feature #120. Support "on behalf of user" operations  
* Feature #124. Allow lock user (support user status field)
* Feature #165. Implement retrieving custom field definitions  
* Feature #169. added new method: create issues using project database ID
* Issue #174. Workaround for bug in Redmine 2.6.0 (it returns invalid structure for empty custom field)
* Bug #164. Fix setting project on existing issue  

# Version 2.0.0 (September 30, 2014)

* Back to proper Semantic versioning for the library (http://semver.org/)
* Significant refactoring to split RedmineManager class into many smaller *managers.
* Many deprecated methods are removed, mostly in RedmineManager class.
* Issue 149. hide internal implementation details in User, Issue and other beans. make IDs immutable.

* Issue 90. add watchers to issue during issue serialization.
* Issue 146. Wiki support (read pages only, no create/update).
* Issue 142. adding auth_source_id support.
* Issue 46. Add attachments to issues.

# Version 1.25 (August 10, 2014)

* Issue 140. Rest_Versions add parameter "sharing"  
* Issue 134. support transport injection (custom SSLSocketFactory, evictor, etc)  
* Issue 139, Issue 132. added property is_public to the project bean  
* Issue 135, Issue 136. Support for own SSLSocketFactory
* Issue 134, Issue 132.	Needed a attribute to create a project with non public access  enhancement
* Issue 60. Provide a way to set HTTP Credentials  enhancement

# Version 1.24 (June 20, 2014)

* Issue #131 Support custom fields for Version objects.
* Issue #100 Support custom fields for Project objects.

# Version 1.23 (November 27, 2013)

* Issue #105 change equals() method to use id only (all bean classes: User, Version, ...)

# Version 1.22 (November 6, 2013)

New features:
* Issue #102 Users Groups retrieval
* Issue #99 added field apiKey to the User bean
* Issue #97 Update due date format in writeVersion

Bug fixed:
* Issue #95 fixed a bug in equals() in CustomField.java

# Version 1.21 (May 9, 2013)

* Bug fixed. Issue 88: "limit" parameter is ignored when calling getChildEntries()
* Build system: switched from Maven to Gradle. Maven's pom.xml file will be eventually deleted.

# Version 1.20 (April 17, 2013)

* New feature: Get journal details
* CRUD operations for user groups
* javadoc fixed

Tested with Redmine 2.2.0 and 2.3.0

# Version 1.19 (April 9, 2013)

* Support for date format used by Redmine 2.3.
* Add/Remove operations for Issue Watchers
* a few other bugfixes. see https://github.com/taskadapter/redmine-java-api/issues?milestone=10

# Version 1.18

Bug fixed:

* Authentication issue in some cases. https://github.com/taskadapter/redmine-java-api/pull/77

# Version 1.17

New Features:
* Issue #2 : load priorities list - requires Redmine 2.2.0+

# Version 1.16

New Features:
* Issue #53 , #36 add users To groups (Redmine 2.1.0+)

Bugs fixed:
* #64 Wrong attribute name in time entry serialization: "comment" instead of "comments"
* #54 JSONBuilder does not write all the values of a custom field when the field is a list.
* #55 testAttachmentUploads test fails with Redmine 2.1.0

Minor improvements:
* #63 Tracker doesn't implement Identifiable and Serializable

# Version 1.15
* No new functionality. Support for newer JSon library. See https://github.com/taskadapter/redmine-java-api/issues/47 for details.

# Version 1.14
* Support for invalid SSL certificates. Before this, the API would refuse to work with servers using self-signed certificates (which seems to be the absolute majority of all the SSL certificates in the world).

# Version 1.13
* Using SLF4J as a logging facility. You need to add some SLF4J binding to your project to control Redmine Java API logging. See http://www.slf4j.org/codes.html#StaticLoggerBinder

# Version 1.12.0.
* Support for Redmine 2.0.2 (it's using a different date format from the one in Redmine 1.4.x)
* Package names were changed: "org.redmine.ta.*" -> "com.taskadapter.redmineapi". also "org.redmine.ta.beans" -> "com.taskadapter.redmineapi.bean". the old package names didn't make any sense. this project is not a part of "official" Redmine development.
* All changes in 1.12.0: https://github.com/taskadapter/redmine-java-api/issues?milestone=3&state=closed

# Version 1.11.0.
* Replace "com.googlecode" Maven group Id with "com.taskadapter" in your Maven pom.xml file (if you use Maven to get Redmine Java API dependency).
```
    <dependency>
         <groupId>com.taskadapter</groupId>
         <artifactId>redmine-java-api</artifactId>
         <version>1.11.0</version>
     </dependency>
```

# Version 1.10.0
* Replace AuthenticationException with RedmineSecurityException or its subclasses RedmineAuthenticationException, NotAuthorizedException
