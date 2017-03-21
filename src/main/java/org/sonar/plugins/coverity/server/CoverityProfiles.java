/*
 * Coverity Sonar Plugin
 * Copyright (c) 2017 Coverity, Inc
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
import org.sonar.api.ExtensionPoint;
import org.sonar.api.ExtensionProvider;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.ValidationMessages;

import java.util.ArrayList;
import java.util.List;

@ServerSide
@ExtensionPoint
public class CoverityProfiles extends ExtensionProvider {
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

            for(Object rule1 : CoverityRules.mapOfRuleMaps.get(language).values()){
                Rule rule = (Rule) rule1;
                //Fix Bug 80500
                profile.activateRule(Rule.create("coverity-" + language, rule.getKey()), rule.getSeverity() );
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
