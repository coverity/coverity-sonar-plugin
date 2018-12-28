/*
 * Coverity Sonar Plugin
 * Copyright (c) 2019 Synopsys, Inc
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
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.coverity.CoverityPlugin;

import java.util.ArrayList;
import java.util.List;

@ServerSide
@ExtensionPoint
public class CoverityProfiles implements BuiltInQualityProfilesDefinition  {
    private static final Logger LOG = LoggerFactory.getLogger(CoverityProfiles.class);
//    private ActiveRules activeRules;
//
//    public CoverityProfiles(ActiveRules activeRules) {
//        this.activeRules = activeRules;
//    }

//    private void  List<CoverityProfile> provide() {
//        List<CoverityProfile> list = new ArrayList<CoverityProfile>();
//        for(String language : CoverityPlugin.COVERITY_LANGUAGES) {
//            list.add(new CoverityProfile(language));
//        }
//        return list;
//    }

    @Override
    public void define(Context context) {
        LOG.error("Andrew Cho -> CoverityProfiles.define started");

        for (String language : CoverityPlugin.COVERITY_LANGUAGES){
            NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(
                    "Coverity(" + language + ")", language);

            for (RulesDefinition.NewRule rule : CoverityRules.loadedRules.get(language)){
                profile.activateRule(CoverityPlugin.REPOSITORY_KEY + "-" + language, rule.key());
            }

            profile.done();

//            if (activeRules != null){
//                for(ActiveRule activeRule : activeRules.findByRepository(CoverityPlugin.REPOSITORY_KEY + "-" + language)){
//                    profile.activateRule(CoverityPlugin.REPOSITORY_KEY + "-" + language, activeRule.ruleKey().rule());
//                }
//
//                profile.done();
//            }else{
//                LOG.error("active rules is null");
//            }
        }


    }

//    class CoverityProfile implements BuiltInQualityProfilesDefinition {
//        String language;
//
//        public CoverityProfile(String language) {
//            this.language = language;
//        }

//        @Override
//        public RulesProfile createProfile(ValidationMessages validation) {
//            final RulesProfile profile = RulesProfile.create("Coverity(" + language + ")", language);
//
//            for(Rule rule : ruleFinder.findAll(RuleQuery.create().withRepositoryKey(CoverityPlugin.REPOSITORY_KEY + "-" + language))){
//                profile.activateRule(Rule.create("coverity-" + language, rule.getKey()), rule.getSeverity());
//            }
//
//            return profile;
//        }

//        @Override
//        public String toString() {
//            return "Coverity(" + language + ")";
//        }


//    }

    @Override
    public String toString() {
        return "Coverity";
    }


}
