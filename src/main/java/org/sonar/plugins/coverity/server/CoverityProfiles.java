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
import org.sonar.api.utils.ValidationMessages;

import java.util.ArrayList;
import java.util.List;

public class CoverityProfiles extends ExtensionProvider implements ServerExtension {
    private static final Logger LOG = LoggerFactory.getLogger(CoverityProfiles.class);
    Language[] languages;
    RuleFinder finder;

    public CoverityProfiles(Language[] languages, RuleFinder finder) {
        this.languages = languages;
        this.finder = finder;
    }

    @Override
    public List<CoverityProfile> provide() {
        List<CoverityProfile> list = new ArrayList<CoverityProfile>();
        for(Language language : languages) {
            list.add(new CoverityProfile(language.getKey()));
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
            RulesProfile profile = RulesProfile.create("Coverity (" + language + ")", language);

            for(Rule r : finder.findAll(RuleQuery.create().withRepositoryKey("coverity-" + language))) {
                profile.activateRule(r, RulePriority.MAJOR);
            }

            return profile;
        }
    }
}
