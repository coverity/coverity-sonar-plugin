/*
 * Coverity Sonar Plugin
 * Copyright (C) 2013 Coverity, Inc.
 * support@coverity.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
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
