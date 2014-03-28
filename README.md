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

* __1.0.1__
  * Fix a classloader issue that caused issue import to fail in some environments. (BZ 60318)
  * Remove hardcoded URLs from WSDL files. (BZ 60390)
  * Fix a bug that caused issue import to fail with large amounts of issues. (BZ 60393)
  * Fix issue descriptions that were appearing as null. (BZ 60508)
* __1.0.2__
  * Implement a Coverity widget that shows the count of defects at each Coverity impact level. A link to the Coverity project is included.
