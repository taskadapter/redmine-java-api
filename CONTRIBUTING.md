# How to contribute to the project.
* Install Gradle 2.1+.
* To build in command line: run "gradle build".
* To open the project code in IDEA or Eclipse: open "build.gradle" file. Your IDE will create a project from the gradle script.
* Please make sure you add unit and/or integration tests when submitting your changes.
* Don't forget to document required Redmine version and other limitations & requirements. 

# Release process
Note: this can only be done by someone who has both "gpg key" to sign artifacts and password to upload to OSS Sonatype repository.

* Run "gradle build" to make sure all code is compilable and tests pass.
* Run "gradle upload" to upload the artifacts to OSS Sonatype Nexus.
* Open https://oss.sonatype.org/index.html#stagingRepositories (use proper credentials), find "com.taskadapter" group and the required artifact, use "close" button and then "release".
* That's it. Once "released", the artifact will be synced from OSS Sonatype Nexus repository to Maven Central in a few hours.
* Update Release notes page.
