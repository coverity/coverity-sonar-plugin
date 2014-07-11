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
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Language;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.api.utils.ValidationMessages;
//import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This class no longer serves the propose of an actual repository (as in versions < 4.2)
// Instead it stores maps that are used in other parts of the code.
public class CoverityRulesRepositories implements ServerExtension {
    public static final Map<String, String> languageDomains = new HashMap<String, String>();
    static {
        languageDomains.put("java", "STATIC_JAVA");
        languageDomains.put("cpp", "STATIC_C");
        languageDomains.put("c++", "STATIC_C");
        languageDomains.put("cs", "STATIC_CS");
    }

    private static final Logger LOG = LoggerFactory.getLogger(CoverityRulesRepositories.class);
    Settings settings;
    Language[] languages;

    public CoverityRulesRepositories(Language[] languages, Settings settings) {
        this.settings = settings;
        this.languages = languages;
    }
}



