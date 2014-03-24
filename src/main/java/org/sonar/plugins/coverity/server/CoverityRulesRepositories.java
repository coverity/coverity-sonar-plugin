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
import org.sonar.api.resources.Language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoverityRulesRepositories extends ExtensionProvider implements ServerExtension {
    public static final Map<String, String> languageDomains = new HashMap<String, String>();
    public static final Map<String, String> domainLanguages = new HashMap<String, String>();

    static {
        languageDomains.put("java", "STATIC_JAVA");
        languageDomains.put("cpp", "STATIC_C");
        languageDomains.put("cs", "STATIC_CS");

        domainLanguages.put("STATIC_JAVA", "java");
        domainLanguages.put("STATIC_C", "cpp");
        domainLanguages.put("STATIC_CS", "cs");
    }

    private static final Logger LOG = LoggerFactory.getLogger(CoverityRulesRepositories.class);
    Settings settings;
    Language[] languages;

    public CoverityRulesRepositories(Language[] languages, Settings settings) {
        this.settings = settings;
        this.languages = languages;
    }

    @Override
    public List<CoverityRules> provide() {
        List<CoverityRules> rules = new ArrayList<CoverityRules>();
        for(Language lang : languages) {
            String langKey = lang.getKey();
            String domain = languageDomains.get(lang.getKey());
            if(domain != null) {
                rules.add(new CoverityRules(langKey, domain, settings));
            }
        }
        return rules;
    }
}
