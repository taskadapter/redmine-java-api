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
