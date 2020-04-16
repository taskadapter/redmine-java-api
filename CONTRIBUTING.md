# How to contribute to the project.
* To build using command line: run `./gradlew build`.
* To open the project code in IDEA or Eclipse: open `build.gradle` file. Your IDE will create a project from the gradle script.
* Please make sure you add unit and/or integration tests when submitting your changes.
* Always include description of what your pull request is trying to achieve, why the current API is bad or not sufficient.
* Don't forget to document required Redmine version and other limitations & requirements. 

# Release process
Note: this can only be done by someone who has both "gpg key" to sign artifacts and password to upload to OSS Sonatype repository.

* Run `./gradlew build` to make sure all code is compilable and tests pass.
* Run `./gradlew upload` to upload the artifacts to OSS Sonatype Nexus. This is usually done by a Jenkins "release lib" job.
* Open https://oss.sonatype.org/index.html#stagingRepositories (use proper credentials), find "com.taskadapter" group and the required artifact, use "close" button and then "release".
* That's it. Once "released", the artifact will be synced from OSS Sonatype Nexus repository to Maven Central in a few hours.
* Update CHANGELOG file.
