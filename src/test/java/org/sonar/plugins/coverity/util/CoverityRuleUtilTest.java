/*
 * Coverity Sonar Plugin
 * Copyright (c) 2021 Synopsys, Inc
 * support@coverity.com
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonar.plugins.coverity.util;

import org.junit.Test;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.DefaultActiveRules;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.rule.RuleKey;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CoverityRuleUtilTest {

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

    private void verifyFindActiveRule(String checkerName, String domain, String repoKey, String key, String subcategory, String lang) throws Exception {
        final SensorContextTester sensorContextTester = SensorContextTester.create(new File("src"));

        final ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
        final RuleKey ruleKey = RuleKey.of(repoKey, domain + "_" + key);
        final NewActiveRule activeRule = rulesBuilder.create(ruleKey);
        sensorContextTester
                .setActiveRules(new DefaultActiveRules(Arrays.asList(activeRule)));

        ActiveRule rule = CoverityRuleUtil.findActiveRule(sensorContextTester, domain, checkerName, subcategory, lang);
        if (rule != null) {
            assertEquals(domain + "_" + key, rule.ruleKey().rule());
        } else {
            fail("Rule cannot be null. CheckerName: " + checkerName + " Domain: " + domain + " RepoKey: " + repoKey
                    + " Subcategory: " + subcategory + " Language: " + lang);
        }

    }

}
