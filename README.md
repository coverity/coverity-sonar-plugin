# Coverity Sonar Plugin

The Coverity Sonar Plugin automatically import issues from Coverity Connect into SonarQube.

## Notes

* This project depends on javax.xml.crypto:xmldsig.jar . This artifact is not in maven central, so you may need to add it to your local repository manually. Here's how to do that:

  * Download the xmldsig jar. (It can be found in several places online)
  * Run this command:
  ```
mvn install:install-file -Dfile=xmldsig-1.0.jar -DgroupId=javax.xml.crypto -DartifactId=xmldsig -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
  ```
  Where "-Dfile=xmldsig-1.0.jar" indicates the path to the downloaded jar file.
  
  Note that this is only required to build the project, not to run it.
  
## Changelog

* __1.2.0__
  * Upgrade to Sonarqube 4.3.2.
  * Added the ability to remove a prefix on a defect's path in order to perform Coverity's analysis and Sonar's analysis on different directories.
* __1.2.0__
  * Added description to the widget (BZ 65058)
  * Defects now have the right 'severity' which can be seen under Quality Profiles (BZ 62490)
  * Fixed url link to Cim page (BZ 62066)
  * Fixed problems when starting up the server (BZ 65055)
  * Added support for c++ plugins, both community and license version. Also, a profile with rules for C have been added. (BZ 62892)



