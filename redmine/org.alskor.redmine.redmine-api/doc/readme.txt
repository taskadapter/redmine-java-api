Redmine Java API.
http://taskadapter.com/redmine_java_api

Author: Alexey Skorokhodov (alskor@gmail.gom)

USAGE.
Redmine Java API can be used in both OSGI and regular Java environment. 

1.==== Use as OSGI bundle ====
  Use the following two OSGI bundles:
  redmine-api-<version>.jar
  redmine-deps-<version>.jar

  ==== Use as a regular JAR file ====
  Add the Redmine API Jar (redmine-api-1.0.3.qualifier.jar) and all jars from 
  the subfolders in "jars" folder to the classpath of your Java application.
  There's no need to add redmine-deps-1.0.0-SNAPSHOT.jar to the classpath in this case. 

2. See sample code in the "samples" folder.  