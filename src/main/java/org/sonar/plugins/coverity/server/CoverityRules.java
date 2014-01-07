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

import com.coverity.ws.v6.CheckerPropertyDataObj;
import com.coverity.ws.v6.CheckerPropertyFilterSpecDataObj;
import com.coverity.ws.v6.CovRemoteServiceException_Exception;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.plugins.coverity.CoverityPlugin;
import org.sonar.plugins.coverity.CoverityUtil;
import org.sonar.plugins.coverity.ws.CIMClient;

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
        return createRulesFromDisk();
    }

    public List<Rule> createRulesFromDisk() {
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

    public List<Rule> createRulesFromServer() {
        ArrayList<Rule> rules = new ArrayList<Rule>();
        String host = settings.getString(CoverityPlugin.COVERITY_CONNECT_HOSTNAME);
        int port = settings.getInt(CoverityPlugin.COVERITY_CONNECT_PORT);
        String user = settings.getString(CoverityPlugin.COVERITY_CONNECT_USERNAME);
        String password = settings.getString(CoverityPlugin.COVERITY_CONNECT_PASSWORD);
        boolean ssl = settings.getBoolean(CoverityPlugin.COVERITY_CONNECT_SSL);

        if(host == null || port == 0 || user == null || password == null) {
            return new ArrayList<Rule>();
        }

        CIMClient instance = new CIMClient(host, port, user, password, ssl);

        LOG.info("Connecting to instance: " + (ssl ? "https://" : "http://") + user + "/" + password + "@" + host + ":" + port);

        CheckerPropertyFilterSpecDataObj filter = new CheckerPropertyFilterSpecDataObj();
        filter.getDomainList().add(domain);
        try {
            List<CheckerPropertyDataObj> checkers = instance.getConfigurationService().getCheckerProperties(filter);

            for(CheckerPropertyDataObj cpdo : checkers) {
                Rule r = Rule.create(
                        CoverityPlugin.REPOSITORY_KEY,
                        CoverityUtil.flattenCheckerSubcategoryId(cpdo.getCheckerSubcategoryId()),
                        cpdo.getSubcategoryShortDescription()
                );
                r.setLanguage(getLanguage());
                r.setDescription(cpdo.getSubcategoryLongDescription());
                rules.add(r);
            }

        } catch(CovRemoteServiceException_Exception e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }

        return rules;
    }
}
