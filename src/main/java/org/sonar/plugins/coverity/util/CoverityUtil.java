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

package org.sonar.plugins.coverity.util;

import com.coverity.ws.v6.CheckerSubcategoryIdDataObj;
import com.coverity.ws.v6.DefectInstanceDataObj;
import org.sonar.api.config.Settings;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.coverity.CoverityPlugin;
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
