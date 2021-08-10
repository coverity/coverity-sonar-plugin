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

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.coverity.CoverityPlugin;

public class CoverityRuleUtil {

    public static RuleKey getRuleKey(String language, String key) {
        return RuleKey.of(CoverityPlugin.REPOSITORY_KEY + "-" + language, key);
    }

    public static ActiveRule findActiveRule(SensorContext context, String domain, String checkerName, String subCategory, String lang) {
        String key = domain + "_" + checkerName;
        RuleKey rk = getRuleKey(lang, key + "_" + subCategory);

        ActiveRule ar = context.activeRules().find(rk);

        if(ar == null && !subCategory.equals("none")){
            rk = getRuleKey(lang, key + "_" + "none");
            ar = context.activeRules().find(rk);
        }

        if (ar == null) {
            if (domain.equals("STATIC_C")) {
                if (ar == null && checkerName.startsWith("MISRA C")) {
                    rk = getRuleKey(lang, "STATIC_C_MISRA.*");
                    ar = context.activeRules().find(rk);
                } else if (ar == null && checkerName.startsWith("PW.")) {
                    rk = getRuleKey(lang, "STATIC_C_PW.*");
                    ar = context.activeRules().find(rk);
                } else if (ar == null && checkerName.startsWith("SW.")) {
                    rk = getRuleKey(lang, "STATIC_C_SW.*");
                    ar = context.activeRules().find(rk);
                } else if (ar == null && checkerName.startsWith("RW.")) {
                    rk = getRuleKey(lang, "STATIC_C_RW.*");
                    ar = context.activeRules().find(rk);
                } else {
                    rk = getRuleKey(lang, "STATIC_C_coverity-cpp");
                    ar = context.activeRules().find(rk);
                }
            } else if (domain.equals("STATIC_CS")) {
                if ( ar == null && checkerName.startsWith("MSVSCA")) {
                    rk = getRuleKey(lang, "STATIC_CS_MSVSCA.*");
                    ar = context.activeRules().find(rk);
                } else {
                    rk = getRuleKey(lang, "STATIC_CS_coverity-cs");
                    ar = context.activeRules().find(rk);
                }
            } else if (domain.equals("STATIC_JAVA")) {
                rk = getRuleKey(lang, "STATIC_JAVA_coverity-java");
                ar = context.activeRules().find(rk);
            } else if (domain.equals("OTHER") && lang.equals("js")) {
                if ( ar == null && checkerName.startsWith("JSHINT")) {
                    rk = getRuleKey(lang, "OTHER_JSHINT.*");
                    ar = context.activeRules().find(rk);
                } else {
                    rk = getRuleKey(lang, "OTHER_coverity-js");
                    ar = context.activeRules().find(rk);
                }
            } else if (domain.equals("OTHER") && lang.equals("py")) {
                rk = getRuleKey(lang, "OTHER_coverity-py");
                ar = context.activeRules().find(rk);
            } else if (domain.equals("OTHER") && lang.equals("php")) {
                rk = getRuleKey(lang, "OTHER_coverity-php");
                ar = context.activeRules().find(rk);
            }
        }

        return ar;
    }

}
