Coverity Sonar Plugin
=====================

The Coverity Sonar Plugin automatically import issues from Coverity Connect into SonarQube.

![Coverity Scan Build Status](https://scan.coverity.com/projects/13562/badge.svg "Coverity Scan Build Status")

Coverity® Sonar Plug-in Installation and Configuration Guide
============================================================

Version 1.7.4

This guide is intended to assist you with the installation and
configuration of the Coverity Sonar plug-in. Once completed, you will be
able to view Coverity Analysis issues within the SonarQube environment.

Compatibility
=============

The table below displays the software versions supported by the Coverity
Sonar plug-in.

| **Software**     | **Supported versions** |
|------------------|------------------------|
| SonarQube        | 6.7.5 - 7.9.1          |
| SonarQube Scanner| 3.0 - 4.0              |
| Coverity Connect | 8.0+                   |

Installing the Coverity Sonar Plug-in
=====================================

To install the Coverity Sonar plug-in, complete the following steps.

1.  Ensure that you have SonarQube(v6.7.5 ~ v7.9.1) and SonarQube Scanner(v3.0 ~ v4.0) installed.
    Sonar installation and setup instructions are located at
    <http://docs.sonarqube.org/display/SONAR/Setup+and+Upgrade>.

2.  Download and unzip the Coverity Sonar plug-in to the Sonar plugins
    folder:

    &lt;SonarInstallDirectory&gt;/extensions/plugins

3.  Restart SonarQube.

Note: After upgrading SonarQube, reset the quality profile for the languages which use Coverity
(in **Quality Profiles**, select **Restore Built-in Profiles**, and select the language.)

Configuring the Coverity Sonar Plug-in
======================================

Once installed, you must configure the Coverity Sonar plug-in for
general use.

1.  Log in to SonarQube as an administrator.

2.  Click on **Administration**.

3.  Choose **Configuration &gt; General Settings**.

4.  Choose **Coverity**.

5.  Enter the appropriate information in each of the fields for your
    Coverity Connect instance. Ensure that the **Enable Coverity**
    option is set to “True” to allow the import of Coverity data.

6.  Click **Save Coverity Settings** to complete the
    basic configuration.

Configuring your Project Settings
=================================

After configuring the general plug-in settings, you must select the
correct Coverity Connect project to associate with each of your Sonar
projects.

1.  Log in to SonarQube as an administrator.

2.  Ensure that you have uploaded your project at least once (with
    SonarQube Scanner), and select the project in SonarQube.

3.  Click on **Quality Profiles**.

4.  Change the Quality Profile option for your project to
    *Coverity(&lt;language&gt;)*, and click **Update.**

5.  Choose **Configuration &gt; General Settings**.

6.  Choose **Coverity**.

7.  Ensure that the **Enable Coverity** option is set to “True” to allow
    the import of Coverity data.

8.  Enter the name of the Coverity Connect project that corresponds to
    the current Sonar project.

9.  Click **Save Coverity Settings. **

Once completed, SonarQube will pull the current Coverity Analysis data
whenever you run SonarQube Scanner on the specified project. This
configuration must be completed for each project you wish to link with
Coverity Connect.

Setting Up sonar-project.properties
===================================

For the plug-in to successfully display Coverity defects, the correct
source paths must be entered in the sonar-project.properties file at the
root of the project you are scanning. The sonar.sources variable must
contain the absolute path names of the source files. For example, on a
Linux system, the variable’s setting might look like this:

sonar.sources=/home/gwen/source/ces-tools/src/main/java

On windows it might look like this:

> sonar.sources=C:\\\\Users\\\\gwen\\\\source\\\\ces-tools\\\\src\\\\main\\\\java

See below for a complete example sonar-project.properties file. 

> \# Required metadata
>
> sonar.projectKey=My-Project-Key
>
> sonar.projectName=My-Project-Name
>
> sonar.login=admin
>
> sonar.password=admin
>
> sonar.host.url=http://localhost:9000
>
> sonar.projectVersion=1.5.0
>
> \# Comma-separated paths to directories with sources (required)
>
> sonar.sources=.
>
> \# Encoding of the source files
>
> sonar.sourceEncoding=UTF-8
>
> sonar.coverity.connect.hostname=localhost
>
> sonar.coverity.connect.port=8080
>
> sonar.coverity.connect.username=user
>
> sonar.coverity.connect.password=password
>
> sonar.projectVersion=1.5.0
>
> sonar.coverity.stream=MyStream
>
> sonar.coverity.project=MyProject
>
> sonar.coverity.enable=true
>
> \# sonar.coverity.prefix=MyOptionalPrefix

*Note*: When using the Coverity plug-in, use the language key "cov-cpp" instead of "c", "c++", or "cpp". This language key prevents conflicts with non_Coverity plug-ins.

To specify the language key: 
-   Add "sonar.language=cov-cpp" (or another preferred language) to the properties file.
-   in **Administration &gt; Coverity &gt; Languages**, configure "C/C++ source files suffixes" appropriately.
-   Configure the source file suffixes for the other language plug-ins to avoid conflicts.

*Note*: The "sonar.coverity.prefix" property is used to help locate files when anlyzing with the sonar scanner. The prefix value will be removed from the "File path" value on the Coverity Connect issue.
-   the value must match exactly, if having trouble finding the source files look at the Coverity Connect issues "File" column 
-   when running analysis on windows Coverity Connect returns values with linux path separators
-   by using --strip-path during analysis this property can be avoided

*Note*: Coverity SonarQube Plugin now supports both stream and project. 
-	If **sonar.coverity.stream** is configured, then the plugin will only fetch defects from configured stream, regardless **sonar.coverity.project** is configured. 
-	If **sonar.coverity.stream** is not configured, then the plugin will use **sonar.coverity.project** to fetch defects from.


The Coverity Widget
===================

The Coverity plug-in includes a Coverity widget that displays
Coverity-specific measures. The Coverity widget is available with SonarQube versions before version 6.2.

-   The Coverity logo and the Coverity Project are both clickable links
    that take you to the Coverity Connect instance. There, you can view
    the Coverity project that contributes data to your Sonar project.
-   The Outstanding Issues count is the number of outstanding Coverity
    issues found in the most recent scan.
-   The other three counts are the numbers of issues at each of
    Coverity’s three impact levels.
-   The Coverity widget is no longer supported as of SonarQube v6.2. The metrics that 
    were displayed by the widget are shown in SonarQube under **Measures**.
-   The Coverity widget can be added to the Dashboard by two different routes: as Admin,
go to **Dashboards &gt; Manage dashboards**, or in a Project, go to **Dashboard** and add it there. 

Sonar Scanner with SSL
===================

Coverity SonarQube Plugin provides a connection to Coverity Connect through SSL. The certificates should be imported to the java key chain where Sonar Scanner is running from. 

Sonar Scanner provides its own jre bundle as part of Sonar Scanner. This means that if a user java installed locally, the certificates need to be imported to the jre which is bundled with Sonar Scanner. 

keytool -importcert -keystore <PATH_TO_SONAR_SCANNER>/jre/lib/security/cacerts -storepass changeit -file <CERT_FILE> -alias <ALIAS>

Limitations
===========

The Coverity Sonar plug-in has the following limitations, which may be
addressed in future releases.

-   Cannot modify data in Coverity Connect (such as triage). Data from
    Coverity Connect is read-only in Sonar.

-   A Sonar instance can only work against a single Coverity
    Connect instance.

-   Does not distinguish between Quality, Test Advisor, and
    Security issues.

-   Interacts with Coverity Connect only through web services, meaning
    the plug-in will not interact with build or analysis, and source
    code is separately maintained between Coverity Connect and Sonar.

-   No parsing of source code – the plug-in is language agnostic.

-   No creation of related Coverity Connect projects in Sonar.

-   The file paths must match exactly in Sonar and Coverity Connect;
    otherwise issue data will not be imported. However, because Coverity
    Analysis may not be performed on the same directory as Sonar
    Analysis, you may remove the beginning of the filename to make it
    relative to Sonar’s project root.

> To do so, navigate to the “Configuration -&gt; General Settings -&gt;
> Coverity” menu, and specify the prefix to be removed in the “Coverity
> Files Prefix” field.

-   There are no immediate plans for localization to languages other
    than English.

Support
=======
If you have any questions or issues with the Coverity plugin, contact <coverity-support@synopsys.com>

Changelog
=========

* __1.7.4__
  * Fixed an issue where C/C++ doesn't show up under 'languages' filter in the Projects page. (SQP-134)
  * Fixed an issue where lines of code not reporting consistently for c/c++ in SonarQube. (SQP-135)
* Enhancement Request - addition of Coverity c/c++ rules into SonarQube plugin (SQP-133)
* __1.7.3__
  * Fixed an issue where C/C++ project is displayed as an empty project in the SonarQube after running Coverity SonarQube plugin. (SQP-144)
  * "sonar.coverity.cov-cpp.suffixes" property is declared as multi-value property. (SQP-136)
* __1.7.2__
  * Coverity SonarQube plugin now support importing defects from configured stream. (SQP-130, SQP-131)
  * Coverity SonarQube plugin now support SonarQube 7.9LTS. (SQP-137)
* __1.7.1__
  * Fixed an issue finding the physical source file via sonar.coverity.prefix in the sonar-project.properties file. (SQP-128)
* __1.7.0__
  * Minimum support version of SonarQube has been changed to version 6.7.5. (SQP-121)
* __1.6.2__
  * The Coverity SonarQube plugin handles the multiple occurrences of Coverity defects where each occurrence has different file path. (BZ 108516)


* __1.6.1__
  * Fixed an issue where the SonarQube issue's line number is different compared to the defect's line number from the Coverity Connect. (BZ 105639)
  * SonarQube Coverity plugin creates the Sonarqube issue with similar description, compared to the defect description displayed in the Coverity Connect. (BZ 105640)
  * Added logging to console on the progress of retrieving Coverity defects from Coverity Connect. (BZ 107598)


* __1.6.0__
  * The SonarQube Coverity plugin now uses tags for each rule to provide easy filtering and lookup. (BZ 96223)
  * The SonarQube Coverity plugin now uses the prefix to match the file location in the Windows operating system. (BZ 90691)
  * Updated to support SonarQube version 5.6 and newer. (BZ 90540)
  * Added support for Coverity JavaScript, Python, PHP, and Objective-C/C++ language checkers. (BZ 90023, 90056, 90061, 90188)
  * SonarQube Coverity plugin now imports DC.WEAK_CRYPTO, TOCTOU, and RESOURCE_LEAK defects from CIM and creates SonarQube issues. (BZ 89850)
  * Removed conflict with other C++ plugins for SonarQube by using a unique language (key="cov-cpp") for Coverity C languages. (BZ 88234)
  * The Coverity SonarQube plugin will try to match the any "Parse Warnings" defects from Coverity Connect with the rules the plugin provides upfront to the SonarQube server. If none of the rules match, then it will create a general "Parse Warnings" rule so that there are corresponding SonarQube issues. (BZ 83997)


* __1.5.0__
  * Upgraded web services from v6 to v9.
  * Fixed issue of Dismissed defects being counted.
  * Coverity Metrics are set to have integer values so other plugins can use our information for statistics and other computations.
  * More rules definitions for all supported languages.
  * Fixed bug of C++ headers not being scanned.
  * Fixed bug of Coverity defects with no main event not being counted.
