/*
 * Coverity Sonar Plugin
 * Copyright (C) 2014 Coverity, Inc.
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
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoverityRulesRepositories extends ExtensionProvider implements ServerExtension {
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
