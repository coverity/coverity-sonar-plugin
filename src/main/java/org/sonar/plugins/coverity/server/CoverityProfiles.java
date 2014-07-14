/*
 * Coverity Sonar Plugin
 * Copyright (c) 2014 Coverity, Inc
 * support@coverity.com
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonar.plugins.coverity.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ExtensionProvider;
import org.sonar.api.ServerExtension;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Language;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.coverity.CoverityPlugin;
import org.sonar.plugins.coverity.util.FileGenerator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.sonar.plugins.coverity.util.FileGenerator.main;

public class CoverityProfiles extends ExtensionProvider implements ServerExtension {
    private static final Logger LOG = LoggerFactory.getLogger(CoverityProfiles.class);
    List<String> languages = new ArrayList<String>();

    public CoverityProfiles() {
        languages.add("java");
        languages.add("cpp");
        languages.add("cs");
        languages.add("c++");
        languages.add("c");
    }

    @Override
    public List<CoverityProfile> provide() {
        List<CoverityProfile> list = new ArrayList<CoverityProfile>();
        for(String language : languages) {
            list.add(new CoverityProfile(language));
        }
        return list;
    }

    class CoverityProfile extends ProfileDefinition {
        String language;

        public CoverityProfile(String language) {
            this.language = language;
        }

        @Override
        public RulesProfile createProfile(ValidationMessages validation) {
            final RulesProfile profile = RulesProfile.create("Coverity(" + language + ")", language);

            for(Object rule1 : CoverityRules.mapOfRuleLists.get(language)){
                CoverityRules.InternalRule rule = (CoverityRules.InternalRule) rule1;
                profile.activateRule(Rule.create("coverity-" + language, rule.key), RulePriority.valueOf(rule.severity) );
            }

            return profile;
        }

        @Override
        public String toString() {
            return "Coverity(" + language + ")";
        }
    }

    @Override
    public String toString() {
        return "Coverity";
    }
}
