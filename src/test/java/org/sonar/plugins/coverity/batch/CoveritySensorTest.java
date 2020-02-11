/*
 * Coverity Sonar Plugin
 * Copyright (c) 2020 Synopsys, Inc
 * support@coverity.com
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonar.plugins.coverity.batch;

import com.coverity.ws.v9.*;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.internal.DefaultIndexedFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.Metadata;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.DefaultActiveRules;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.coverity.CoverityPlugin;
import org.sonar.plugins.coverity.metrics.CoverityPluginMetrics;
import org.sonar.plugins.coverity.server.CppLanguage;
import org.sonar.plugins.coverity.ws.CIMClientFactory;
import org.sonar.plugins.coverity.ws.TestCIMClient;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CoveritySensorTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private CoveritySensor sensor;
    private TestCIMClient testCimClient;

    @Before
    public void setUp() throws Exception {
        CIMClientFactory mockClientFactory = mock(CIMClientFactory.class);
        testCimClient = new TestCIMClient();
        when(mockClientFactory.create(any())).thenReturn(testCimClient);
        sensor = new CoveritySensor(mockClientFactory);
    }

    @Test
    public void testDescribe_setsName_repositories_properties() throws Exception {
        final DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();

        sensor.describe(descriptor);

        assertEquals(sensor.toString(), descriptor.name());
        final List<String> expectedRepositories = Arrays.asList(CoverityPlugin.REPOSITORY_KEY + "-java",
                CoverityPlugin.REPOSITORY_KEY + "-cs",
                CoverityPlugin.REPOSITORY_KEY + "-js",
                CoverityPlugin.REPOSITORY_KEY + "-py",
                CoverityPlugin.REPOSITORY_KEY + "-php",
                CoverityPlugin.REPOSITORY_KEY + "-" + CppLanguage.KEY);
        assertEquals(expectedRepositories, descriptor.ruleRepositories());
    }

    @Test
    public void testExecute_savesIssue_FromProject() {
        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));
        final String filePath = "src/Foo.java";
        String content = "public class Foo {\n}";

        sensorContextTester.fileSystem().baseDir().getAbsolutePath();

        final Metadata metadata = new Metadata(1, 1, "", new int[1], 0);
        final DefaultIndexedFile indexedFile = new DefaultIndexedFile(
                StringUtils.EMPTY,
                sensorContextTester.fileSystem().baseDirPath(),
                filePath,
                "java");
        DefaultInputFile inputFile = new DefaultInputFile(indexedFile, f -> f.setMetadata(metadata), content);

        sensorContextTester
                .fileSystem()
                .add(inputFile);
        final HashMap<String, String> properties = new HashMap<>();

        final String projectName = "my-cov-project";
        final String streamName = "my-cov-stream";
        testCimClient.setupProject(projectName);

        properties.put(CoverityPlugin.COVERITY_PROJECT, projectName);
        properties.put(CoverityPlugin.COVERITY_ENABLE, "true");
        properties.put("sonar.sources", "src");
        sensorContextTester
                .settings()
                .addProperties(properties);

        final String checkerName = "TEST_CHECKER";
        final String domain = "STATIC_JAVA";
        final String subcategory = "none";

        final ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
        final RuleKey ruleKey = RuleKey.of("coverity-java", domain + "_" + checkerName + "_" + subcategory);
        final NewActiveRule javaTestChecker = rulesBuilder.create(ruleKey);
        sensorContextTester
                .setActiveRules(new DefaultActiveRules(Arrays.asList(javaTestChecker)));
        final String expectedIssueMessage =
                "[TEST_CHECKER(type)] Event Tag: Event Description ( CID 1 : https://test-host:8443/query/defects.htm?projectId=0&mergeKey=MK_1 )";

        testCimClient.setupDefect(domain, checkerName, streamName, Arrays.asList(filePath));

        sensor.execute(sensorContextTester);

        final Collection<Issue> issues = sensorContextTester.allIssues();
        assertNotNull(issues);
        assertEquals(1, issues.size());
        final Issue issue = issues.iterator().next();
        assertEquals(ruleKey, issue.ruleKey());
        assertEquals(inputFile, issue.primaryLocation().inputComponent());
        assertEquals(expectedIssueMessage, issue.primaryLocation().message());
    }

    @Test
    public void testExecute_savesIssue_FromStream() {
        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));
        final String filePath = "src/Foo.java";
        String content = "public class Foo {\n}";

        final Metadata metadata = new Metadata(1, 1, "", new int[1], 0);
        final DefaultIndexedFile indexedFile = new DefaultIndexedFile(StringUtils.EMPTY,
                sensorContextTester.fileSystem().baseDirPath(), filePath, "java");
        final DefaultInputFile inputFile = new DefaultInputFile(indexedFile, f -> f.setMetadata(metadata), content);

        sensorContextTester
                .fileSystem()
                .add(inputFile);
        final HashMap<String, String> properties = new HashMap<>();

        final String streamName = "my-cov-stream";
        testCimClient.setupStream(streamName);

        properties.put(CoverityPlugin.COVERITY_STREAM, streamName);
        properties.put(CoverityPlugin.COVERITY_ENABLE, "true");
        properties.put("sonar.sources", "src");
        sensorContextTester
                .settings()
                .addProperties(properties);

        final String checkerName = "TEST_CHECKER";
        final String domain = "STATIC_JAVA";
        final String subcategory = "none";

        final ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
        final RuleKey ruleKey = RuleKey.of("coverity-java", domain + "_" + checkerName + "_" + subcategory);
        final NewActiveRule javaTestChecker = rulesBuilder.create(ruleKey);
        sensorContextTester
                .setActiveRules(new DefaultActiveRules(Arrays.asList(javaTestChecker)));
        final String expectedIssueMessage =
                "[TEST_CHECKER(type)] Event Tag: Event Description ( CID 1 : https://test-host:8443/query/defects.htm?stream=my-cov-stream&mergeKey=MK_1 )";

        testCimClient.setupDefect(domain, checkerName, streamName, Arrays.asList(filePath));

        sensor.execute(sensorContextTester);

        final Collection<Issue> issues = sensorContextTester.allIssues();
        assertNotNull(issues);
        assertEquals(1, issues.size());
        final Issue issue = issues.iterator().next();
        assertEquals(ruleKey, issue.ruleKey());
        assertEquals(inputFile, issue.primaryLocation().inputComponent());
        assertEquals(expectedIssueMessage, issue.primaryLocation().message());

        Measure measure = sensorContextTester.measure(":" + filePath, CoreMetrics.NCLOC);
        assertNotNull(measure);
        assertEquals(inputFile.lines(), measure.value());
    }

    @Test
    public void testExecute_savesNoIssue_NoInputFileLanguage() {
        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));
        final String filePath = "src/ruby.rb";
        String content = "def test(val)\n" +
                "  z() if ~(s == 0)  # A CONSTANT_EXPRESSION_RESULT here. '!(s == 0)' is intended.\n" +
                "end";

        final Metadata metadata = new Metadata(1, 1, "", new int[1], 0);
        final DefaultIndexedFile indexedFile = new DefaultIndexedFile(StringUtils.EMPTY,
                sensorContextTester.fileSystem().baseDirPath(), filePath, "ruby");
        final DefaultInputFile inputFile = new DefaultInputFile(indexedFile, f -> f.setMetadata(metadata), content);

        sensorContextTester
                .fileSystem()
                .add(inputFile);
        final HashMap<String, String> properties = new HashMap<>();

        final String projectName = "my-ruby-project";
        final String streamName = "my-ruby-stream";
        testCimClient.setupProject(projectName);

        properties.put(CoverityPlugin.COVERITY_PROJECT, projectName);
        properties.put(CoverityPlugin.COVERITY_ENABLE, "true");
        properties.put("sonar.sources", "src");
        sensorContextTester
                .settings()
                .addProperties(properties);

        final String checkerName = "TEST_CHECKER";
        final String domain = "OTHER";

        testCimClient.setupDefect(domain, checkerName, streamName, Arrays.asList((new File(inputFile.uri()).getAbsolutePath())));

        sensor.execute(sensorContextTester);

        final Collection<Issue> issues = sensorContextTester.allIssues();
        assertNotNull(issues);
        assertEquals(0, issues.size());
    }

    @Test
    public void testExecute_SetsCoverityLogoMeasures() throws Exception {
        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));
        final String filePath = "src/Foo.java";
        String content = "public class Foo {\n}";

        final Metadata metadata = new Metadata(1, 1, "", new int[1], 0);
        final DefaultIndexedFile indexedFile = new DefaultIndexedFile(StringUtils.EMPTY,
                sensorContextTester.fileSystem().baseDirPath(), filePath, "java");
        final DefaultInputFile inputFile = new DefaultInputFile(indexedFile, f -> f.setMetadata(metadata), content);

        sensorContextTester
                .fileSystem()
                .add(inputFile);
        final HashMap<String, String> properties = new HashMap<>();

        final String projectName = "my-cov-project";
        testCimClient.setupProject("first-project");
        testCimClient.setupProject(projectName);

        properties.put(CoverityPlugin.COVERITY_PROJECT, projectName);
        properties.put(CoverityPlugin.COVERITY_ENABLE, "true");
        sensorContextTester
                .settings()
                .addProperties(properties);

        sensor.execute(sensorContextTester);

        String expectedUrl = String.format("%s://%s:%d/", testCimClient.isUseSSL() ? "https" : "http", testCimClient.getHost(), testCimClient.getPort());
        Measure measure = sensorContextTester.measure("projectKey", CoverityPluginMetrics.COVERITY_URL_CIM_METRIC);

        assertEquals(expectedUrl, measure.value());

        final ProjectDataObj project = testCimClient.getProject(projectName);
        assertNotNull(project);

        assertEquals(expectedUrl, measure.value());
    }

    @Test
    public void testExecute_CoverityDisabled() {

        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));
        final HashMap<String, String> properties = new HashMap<>();

        properties.put(CoverityPlugin.COVERITY_ENABLE, "false");
        sensorContextTester
                .settings()
                .addProperties(properties);

        sensor.execute(sensorContextTester);

        final Collection<Issue> issues = sensorContextTester.allIssues();
        assertEquals(0, issues.size());
    }

    @Test
    public void testExecute_CoverityProjectNotSpecified() {

        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));
        final HashMap<String, String> properties = new HashMap<>();

        properties.put(CoverityPlugin.COVERITY_ENABLE, "true");
        sensorContextTester
                .settings()
                .addProperties(properties);

        sensor.execute(sensorContextTester);

        final Collection<Issue> issues = sensorContextTester.allIssues();
        assertEquals(0, issues.size());
    }

    @Test
    public void testExecute_CoverityProjectNotExist() {

        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));
        final HashMap<String, String> properties = new HashMap<>();

        properties.put(CoverityPlugin.COVERITY_ENABLE, "true");
        properties.put(CoverityPlugin.COVERITY_PROJECT, "test-project");
        sensorContextTester
                .settings()
                .addProperties(properties);

        sensor.execute(sensorContextTester);

        final Collection<Issue> issues = sensorContextTester.allIssues();
        assertEquals(0, issues.size());
    }

    @Test
    public void testGetIssueMessage_WhenMainEventNotExist() throws IOException {
        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));
        final String filePath = "src/Class1.cs";
        String content = "public class Class1 {\n}";

        final Metadata metadata = new Metadata(1, 1, "", new int[1], 0);
        final DefaultIndexedFile indexedFile = new DefaultIndexedFile(StringUtils.EMPTY,
                sensorContextTester.fileSystem().baseDirPath(), filePath, "cs");
        final DefaultInputFile inputFile = new DefaultInputFile(indexedFile, f -> f.setMetadata(metadata), content);

        sensorContextTester
                .fileSystem()
                .add(inputFile);
        final HashMap<String, String> properties = new HashMap<>();

        final String projectName = "my-cov-project";
        final String streamName = "my-cov-stream";
        testCimClient.setupProject(projectName);

        properties.put(CoverityPlugin.COVERITY_PROJECT, projectName);
        properties.put(CoverityPlugin.COVERITY_ENABLE, "true");
        properties.put("sonar.sources", "src");
        sensorContextTester
                .settings()
                .addProperties(properties);

        final String checkerName = "TEST_CHECKER";
        final String domain = "STATIC_CS";
        final String subcategory = "none";

        final ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
        final RuleKey ruleKey = RuleKey.of("coverity-cs", domain + "_" + checkerName + "_" + subcategory);
        final NewActiveRule csTestChecker = rulesBuilder.create(ruleKey);
        sensorContextTester
                .setActiveRules(new DefaultActiveRules(Arrays.asList(csTestChecker)));
        final String expectedIssueMessage =
                "[TEST_CHECKER(type)] Defect Long Description ( CID 1 : https://test-host:8443/query/defects.htm?projectId=0&mergeKey=MK_1 )";

        testCimClient.setupDefect(domain, checkerName, streamName, Arrays.asList(filePath));
        testCimClient.configureMainEvent(StringUtils.EMPTY, StringUtils.EMPTY);

        sensor.execute(sensorContextTester);

        final Collection<Issue> issues = sensorContextTester.allIssues();
        assertNotNull(issues);
        assertEquals(1, issues.size());
        final Issue issue = issues.iterator().next();
        assertEquals(ruleKey, issue.ruleKey());
        assertEquals(inputFile, issue.primaryLocation().inputComponent());
        assertEquals(expectedIssueMessage, issue.primaryLocation().message());
    }

    @Test
    public void testExecute_savesIssue_WithMultiOccurrence_WithDifferentFilePaths() throws Exception {
        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));

        final String filePath1 = "Foo1.java";
        String content1 = "public class Foo1 {\n public void createDefect(){\n}}";

        final Metadata metadata1 = new Metadata(3, 1, "", new int[] {0,1,0}, 0);
        final DefaultIndexedFile indexedFile1 = new DefaultIndexedFile(StringUtils.EMPTY,
                sensorContextTester.fileSystem().baseDirPath(), filePath1, "java");
        final DefaultInputFile inputFile1 = new DefaultInputFile(indexedFile1, f -> f.setMetadata(metadata1), content1);

        sensorContextTester
                .fileSystem()
                .add(inputFile1);

        final String filePath2 = "Foo2.java";
        String content2 = "public class Foo2 {\n}";

        final Metadata metadata2 = new Metadata(2, 2, "", new int[2], 0);
        final DefaultIndexedFile indexedFile2 = new DefaultIndexedFile(StringUtils.EMPTY,
                sensorContextTester.fileSystem().baseDirPath(), filePath2, "java");
        final DefaultInputFile inputFile2 = new DefaultInputFile(indexedFile2, f -> f.setMetadata(metadata2), content2);

        sensorContextTester
                .fileSystem()
                .add(inputFile2);

        final HashMap<String, String> properties = new HashMap<>();

        final String projectName = "my-cov-project";
        final String streamName = "my-cov-stream";
        testCimClient.setupProject(projectName);

        properties.put(CoverityPlugin.COVERITY_PROJECT, projectName);
        properties.put(CoverityPlugin.COVERITY_ENABLE, "true");
        properties.put("sonar.sources", "src");
        sensorContextTester
                .settings()
                .addProperties(properties);

        final String checkerName = "TEST_CHECKER";
        final String domain = "STATIC_JAVA";
        final String subcategory = "none";

        final ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
        final RuleKey ruleKey = RuleKey.of("coverity-java", domain + "_" + checkerName + "_" + subcategory);
        final NewActiveRule javaTestChecker = rulesBuilder.create(ruleKey);
        sensorContextTester
                .setActiveRules(new DefaultActiveRules(Arrays.asList(javaTestChecker)));
        final String expectedIssueMessage =
                "[TEST_CHECKER(type)] Event Tag: Event Description ( CID 1 : https://test-host:8443/query/defects.htm?projectId=0&mergeKey=MK_1 )";

        testCimClient.setupDefect(domain, checkerName, streamName, Arrays.asList(filePath1, filePath2, filePath1)
        );

        sensor.execute(sensorContextTester);

        final Collection<Issue> issues = sensorContextTester.allIssues();
        assertNotNull(issues);
        assertEquals(3, issues.size());

        final Iterator<Issue> iterator = issues.iterator();

        Issue issue1 = iterator.next();
        assertEquals(ruleKey, issue1.ruleKey());
        assertEquals(inputFile1, issue1.primaryLocation().inputComponent());
        assertEquals(expectedIssueMessage, issue1.primaryLocation().message());

        Issue issue2 = iterator.next();
        assertEquals(ruleKey, issue2.ruleKey());
        assertEquals(inputFile1, issue2.primaryLocation().inputComponent());
        assertEquals(expectedIssueMessage, issue2.primaryLocation().message());

        Issue issue3 = iterator.next();
        assertEquals(ruleKey, issue3.ruleKey());
        assertEquals(inputFile2, issue3.primaryLocation().inputComponent());
        assertEquals(expectedIssueMessage, issue3.primaryLocation().message());
    }

    @Test
    public void testExecute_savesIssue_WithStripPrefix() throws IOException {
        String originalOsName = System.getProperty("os.name");
        System.setProperty("os.name", "Windows 10");
        String originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", ".");

        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));
        final String filePath = "Foo.java";
        String content = "public class Foo {\n}";

        final Metadata metadata = new Metadata(1, 1, "", new int[1], 0);
        final DefaultIndexedFile indexedFile = new DefaultIndexedFile(StringUtils.EMPTY,
                sensorContextTester.fileSystem().baseDirPath(), filePath, "java");
        final DefaultInputFile inputFile = new DefaultInputFile(indexedFile, f -> f.setMetadata(metadata), content);

        final String stripPath = "stripPath/";

        sensorContextTester
                .fileSystem()
                .add(inputFile);
        final HashMap<String, String> properties = new HashMap<>();

        final String projectName = "my-cov-project";
        final String streamName = "my-cov-stream";
        testCimClient.setupProject(projectName);

        properties.put(CoverityPlugin.COVERITY_PROJECT, projectName);
        properties.put(CoverityPlugin.COVERITY_ENABLE, "true");
        properties.put(CoverityPlugin.COVERITY_PREFIX, stripPath);
        properties.put("sonar.sources", "src");
        sensorContextTester
                .settings()
                .addProperties(properties);

        final String checkerName = "TEST_CHECKER";
        final String domain = "STATIC_JAVA";
        final String subcategory = "none";

        final ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
        final RuleKey ruleKey = RuleKey.of("coverity-java", domain + "_" + checkerName + "_" + subcategory);
        final NewActiveRule javaTestChecker = rulesBuilder.create(ruleKey);
        sensorContextTester
                .setActiveRules(new DefaultActiveRules(Arrays.asList(javaTestChecker)));
        final String expectedIssueMessage =
                "[TEST_CHECKER(type)] Event Tag: Event Description ( CID 1 : https://test-host:8443/query/defects.htm?projectId=0&mergeKey=MK_1 )";

        try{
            testCimClient.setupDefect(domain, checkerName, streamName, Arrays.asList(stripPath + filePath));

            sensor.execute(sensorContextTester);

            final Collection<Issue> issues = sensorContextTester.allIssues();
            assertNotNull(issues);
            assertEquals(1, issues.size());
            final Issue issue = issues.iterator().next();
            assertEquals(ruleKey, issue.ruleKey());
            assertEquals(inputFile, issue.primaryLocation().inputComponent());
            assertEquals(expectedIssueMessage, issue.primaryLocation().message());
        } finally {
            System.setProperty("os.name", originalOsName);
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    public void testExecute_savesIssue_WithNoInputFile() {

        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));
        final String filePath = "src/Foo.java";
        final HashMap<String, String> properties = new HashMap<>();

        final String projectName = "my-cov-project";
        final String streamName = "my-cov-stream";
        testCimClient.setupProject(projectName);

        properties.put(CoverityPlugin.COVERITY_PROJECT, projectName);
        properties.put(CoverityPlugin.COVERITY_ENABLE, "true");
        properties.put("sonar.sources", "src");
        sensorContextTester
                .settings()
                .addProperties(properties);

        final String checkerName = "TEST_CHECKER";
        final String domain = "STATIC_JAVA";
        final String subcategory = "none";

        final ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
        final RuleKey ruleKey = RuleKey.of("coverity-java", domain + "_" + checkerName + "_" + subcategory);
        final NewActiveRule javaTestChecker = rulesBuilder.create(ruleKey);
        sensorContextTester
                .setActiveRules(new DefaultActiveRules(Arrays.asList(javaTestChecker)));
        testCimClient.setupDefect(domain, checkerName, streamName, Arrays.asList(filePath));

        sensor.execute(sensorContextTester);

        final Collection<Issue> issues = sensorContextTester.allIssues();
        assertNotNull(issues);
        assertEquals(0, issues.size());
    }
}
