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
import org.sonar.api.config.Settings;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.plugins.coverity.CoverityPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CoverityRules extends RuleRepository {
    private static final Logger LOG = LoggerFactory.getLogger(CoverityRules.class);
    Settings settings;
    String domain;

    public CoverityRules(String language, String domain, Settings settings) {
        super(CoverityPlugin.REPOSITORY_KEY + "-" + language, language);
        this.domain = domain;
        this.settings = settings;
    }

    @Override
    public List<Rule> createRules() {
        List<Rule> rules;
        try {
            InputStream is = getClass().getResourceAsStream("/org/sonar/plugins/coverity/server/coverity-" + getLanguage() + ".xml");
            rules = new XMLRuleParser().parse(is);
            is.close();
        } catch(IOException e) {
            LOG.error("Failed to parse rules xml for language: " + getLanguage());
            e.printStackTrace();
            return new ArrayList<Rule>();
        }
        return rules;
    }
}
