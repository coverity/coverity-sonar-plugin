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
import org.sonar.api.config.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoverityRulesRepositories extends ExtensionProvider implements ServerExtension {
    private static final Logger LOG = LoggerFactory.getLogger(CoverityRulesRepositories.class);
    private static Map<String, String> languageDomains = new HashMap<String, String>();

    static {
        languageDomains.put("java", "STATIC_JAVA");
        languageDomains.put("cpp", "STATIC_C");
        languageDomains.put("cs", "STATIC_CS");
    }

    Settings settings;

    public CoverityRulesRepositories(Settings settings) {
        this.settings = settings;
    }

    @Override
    public List<CoverityRules> provide() {
        List<CoverityRules> rules = new ArrayList<CoverityRules>();
        for(Map.Entry<String, String> entry : languageDomains.entrySet()) {
            rules.add(new CoverityRules(entry.getKey(), entry.getValue(), settings));
        }
        return rules;
    }
}
