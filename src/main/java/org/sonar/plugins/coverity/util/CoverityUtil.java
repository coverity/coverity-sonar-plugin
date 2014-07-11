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

package org.sonar.plugins.coverity.util;

import com.coverity.ws.v6.CheckerSubcategoryIdDataObj;
import com.coverity.ws.v6.DefectInstanceDataObj;
import org.sonar.api.config.Settings;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.coverity.CoverityPlugin;
//import org.sonar.plugins.coverity.server.CoverityRulesRepositories;
import org.sonar.plugins.coverity.server.CoverityRulesRepositories;
import org.sonar.plugins.coverity.ws.CIMClient;

public class CoverityUtil {
    public static RuleKey getRuleKey(String language, DefectInstanceDataObj dido) {
        return RuleKey.of(CoverityPlugin.REPOSITORY_KEY + "-" + language, flattenCheckerSubcategoryId(dido.getCheckerSubcategoryId()));
    }

    public static String flattenCheckerSubcategoryId(CheckerSubcategoryIdDataObj csido) {
        return csido.getDomain() + "_" + csido.getCheckerName() + "_" + csido.getSubcategory();
    }

    public static String createURL(CIMClient client) {
        return createURL(client.getHost(), client.getPort(), client.isUseSSL());
    }

    public static String createURL(Settings settings) {
        String host = settings.getString(CoverityPlugin.COVERITY_CONNECT_HOSTNAME);
        int port = settings.getInt(CoverityPlugin.COVERITY_CONNECT_PORT);
        boolean ssl = settings.getBoolean(CoverityPlugin.COVERITY_CONNECT_SSL);

        return createURL(host, port, ssl);
    }

    public static String createURL(String host, int port, boolean ssl) {
        if(host == null || port == 0) {
            return null;
        }
        return String.format("http%s://%s:%d/", (ssl ? "s" : ""), host, port);
    }
}
