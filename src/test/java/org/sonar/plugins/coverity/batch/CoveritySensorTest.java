/*
 * Coverity Sonar Plugin
 * Copyright (c) 2017 Synopsys, Inc
 * support@coverity.com
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonar.plugins.coverity.batch;

import com.coverity.ws.v9.DefectInstanceDataObj;
import com.coverity.ws.v9.EventDataObj;
import com.coverity.ws.v9.MergedDefectDataObj;
import com.coverity.ws.v9.ProjectDataObj;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.DefaultActiveRules;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.coverity.CoverityPlugin;
import org.sonar.plugins.coverity.base.CoverityPluginMetrics;
import org.sonar.plugins.coverity.server.CppLanguage;
import org.sonar.plugins.coverity.ws.CIMClient;
import org.sonar.plugins.coverity.ws.CIMClientFactory;
import org.sonar.plugins.coverity.ws.TestCIMClient;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CoveritySensorTest {
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
        assertEquals(Arrays.asList(CoverityPlugin.COVERITY_PROJECT), descriptor.properties());
    }

    @Test
    public void testExecute_savesIssue() throws Exception {

        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));
        final String filePath = "src/Foo.java";
        final DefaultInputFile inputFile = new DefaultInputFile("myProjectKey", filePath)
                .setLanguage("java")
                .initMetadata("public class Foo {\n}");
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

        testCimClient.setupDefect(domain, checkerName, filePath, streamName);

        sensor.execute(sensorContextTester);

        final Collection<Issue> issues = sensorContextTester.allIssues();
        assertNotNull(issues);
        assertEquals(1, issues.size());
        final Issue issue = issues.iterator().next();
        assertEquals(ruleKey, issue.ruleKey());
        assertEquals(inputFile, issue.primaryLocation().inputComponent());
    }

    @Test
    public void testExecute_savesNoIssue_NoInputFileLanguage() throws Exception {

        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));
        final String filePath = "src/ruby.rb";
        final DefaultInputFile inputFile = new DefaultInputFile("myProjectKey", filePath)
                .setLanguage(null)
                .initMetadata("def test(val)\n" +
                        "  z() if ~(s == 0)  # A CONSTANT_EXPRESSION_RESULT here. '!(s == 0)' is intended.\n" +
                        "end");
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

        testCimClient.setupDefect(domain, checkerName, filePath, streamName);

        sensor.execute(sensorContextTester);

        final Collection<Issue> issues = sensorContextTester.allIssues();
        assertNotNull(issues);
        assertEquals(0, issues.size());
    }

    @Test
    public void testGetDefectURL() throws Exception {
        CIMClient instance = mock(CIMClient.class);
        ProjectDataObj projectObj = mock(ProjectDataObj.class);
        MergedDefectDataObj mddo = mock(MergedDefectDataObj.class);

        String target = "http://&&HOST&&:999999/sourcebrowser.htm?projectId=888888&mergedDefectId=777777";

        when(instance.getHost()).thenReturn("&&HOST&&");
        when(instance.getPort()).thenReturn(999999);
        when(projectObj.getProjectKey()).thenReturn(888888L);
        when(mddo.getCid()).thenReturn(777777L);
        String url = sensor.getDefectURL(instance, projectObj, mddo);

        assertEquals(target, url);
    }

    @Test
    public void testGetMainEvent() throws Exception {
        DefectInstanceDataObj dido = new DefectInstanceDataObj();

        EventDataObj em = new EventDataObj();
        em.setMain(true);

        dido.getEvents().add(em);

        int n = 10;
        for(int i = 0; i < n; i++) {
            dido.getEvents().add(new EventDataObj());
        }

        Collections.swap(dido.getEvents(), 0, n / 2);

        EventDataObj result = sensor.getMainEvent(dido);
        assertEquals("Found wrong event", em, result);
    }

    @Test
    public void testExecute_SetsCoverityLogoMeasures() throws Exception {

        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));
        final DefaultInputFile inputFile = new DefaultInputFile("myProjectKey", "src/Foo.java")
                .setLanguage("java")
                .initMetadata("public class Foo {\n}");
        sensorContextTester
                .fileSystem()
                .add(inputFile);

        final String projectName = "my-cov-project";
        testCimClient.setupProject("first-project");
        testCimClient.setupProject(projectName);

        final HashMap<String, String> properties = new HashMap<>();
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
        final long projectId = project.getProjectKey();
        expectedUrl = expectedUrl + "reports.htm#p" + projectId;
        measure = sensorContextTester.measure("projectKey", CoverityPluginMetrics.COVERITY_PROJECT_URL);

        assertEquals(expectedUrl, measure.value());
    }

    @Test
    public void testExecute_CoverityDisabled() throws Exception {

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
    public void testFindActiveRule() throws Exception {
        // Java
        verifyFindActiveRule("TEST_CHECKER", "STATIC_JAVA", "coverity-java", "coverity-java", "", "java");
        verifyFindActiveRule("TEST_CHECKER", "STATIC_JAVA", "coverity-java", "TEST_CHECKER_none", "testSubcategory", "java");

        // C#
        verifyFindActiveRule("MSVSCA.TestParseWarning", "STATIC_CS", "coverity-cs", "MSVSCA.*", "", "cs");
        verifyFindActiveRule("TEST_CHECKER", "STATIC_CS", "coverity-cs", "coverity-cs", "", "cs");

        // C++ (MISRA, ParseWarning, SemanticWarning, RecoveryWarning)
        verifyFindActiveRule("MISRA C RULE 10", "STATIC_C", "coverity-cpp", "MISRA.*", "", "cpp");
        verifyFindActiveRule("PW.TestParseWarning", "STATIC_C", "coverity-cpp", "PW.*", "", "cpp");
        verifyFindActiveRule("SW.TestParseWarning", "STATIC_C", "coverity-cpp", "SW.*", "", "cpp");
        verifyFindActiveRule("RW.TestParseWarning", "STATIC_C", "coverity-cpp", "RW.*", "", "cpp");
        verifyFindActiveRule("TEST_CHECKER", "STATIC_C", "coverity-cpp", "coverity-cpp", "", "cpp");

        // JavaScript, Python, PHP
        verifyFindActiveRule("JSHINT.TestParseWarning", "OTHER", "coverity-js", "JSHINT.*", "", "js");
        verifyFindActiveRule("TEST_CHECKER", "OTHER", "coverity-js", "coverity-js", "", "js");
        verifyFindActiveRule("TEST_CHECKER", "OTHER", "coverity-py", "coverity-py", "", "py");
        verifyFindActiveRule("TEST_CHECKER", "OTHER", "coverity-php", "coverity-php", "", "php");
    }

    @Test
    public void testExecute_CoverityProjectNotSpecified() throws Exception {

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
    public void testExecute_CoverityProjectNotExist() throws Exception {

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

    private void verifyFindActiveRule(String checkerName, String domain, String repoKey, String key, String subcategory, String lang) throws Exception {
        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));

        final ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
        final RuleKey ruleKey = RuleKey.of(repoKey, domain + "_" + key);
        final NewActiveRule activeRule = rulesBuilder.create(ruleKey);
        sensorContextTester
                .setActiveRules(new DefaultActiveRules(Arrays.asList(activeRule)));

        ActiveRule rule = sensor.findActiveRule(sensorContextTester, domain, checkerName, subcategory, lang);
        if (rule != null) {
            assertEquals(domain + "_" + key, rule.ruleKey().rule());
        } else {
            fail("Rule cannot be null. CheckerName: " + checkerName + " Domain: " + domain + " RepoKey: " + repoKey
                + " Subcategory: " + subcategory + " Language: " + lang);
        }

    }
}
