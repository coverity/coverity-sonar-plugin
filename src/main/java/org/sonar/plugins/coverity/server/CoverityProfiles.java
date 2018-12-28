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
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.coverity.CoverityPlugin;

@ServerSide
@ExtensionPoint
public class CoverityProfiles implements BuiltInQualityProfilesDefinition  {
    private static final Logger LOG = LoggerFactory.getLogger(CoverityProfiles.class);

    @Override
    public void define(Context context) {
        for (String language : CoverityPlugin.COVERITY_LANGUAGES){
            NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(
                    "Coverity(" + language + ")", language);

            for (RulesDefinition.NewRule rule : CoverityRules.LOADED_RULES.get(language)){
                profile.activateRule(CoverityPlugin.REPOSITORY_KEY + "-" + language, rule.key());
            }

            profile.done();
        }
    }

    @Override
    public String toString() {
        return "Coverity";
    }
}
