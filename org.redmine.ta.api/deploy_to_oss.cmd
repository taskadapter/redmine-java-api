set DEPLOY_CMD=mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2 -DrepositoryId=sonatype-nexus-staging -DpomFile=pom.xml
set JAR=target/redmine-java-api-1.5.1.jar
set SRC=target/redmine-java-api-1.5.1-sources.jar
set JAVADOC=target/redmine-java-api-1.5.1-javadoc.jar

call %DEPLOY_CMD% -Dfile=%JAR%
call %DEPLOY_CMD% -Dfile=%SRC% -Dclassifier=sources
call %DEPLOY_CMD% -Dfile=%JAVADOC% -Dclassifier=javadoc


