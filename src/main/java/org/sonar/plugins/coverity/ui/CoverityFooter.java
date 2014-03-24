/*
 * Coverity Plugin
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02
 */

package org.sonar.plugins.coverity.ui;

import com.coverity.ws.v6.CovRemoteServiceException_Exception;
import com.coverity.ws.v6.ProjectDataObj;
import org.sonar.api.config.Settings;
import org.sonar.api.web.Footer;
import org.sonar.plugins.coverity.CoverityPlugin;
import org.sonar.plugins.coverity.util.CoverityUtil;
import org.sonar.plugins.coverity.ws.CIMClient;

import java.io.IOException;

public final class CoverityFooter implements Footer {
    Settings settings;

    public CoverityFooter(Settings settings) {
        this.settings = settings;
    }

    public String getHtml() {

        String host = settings.getString(CoverityPlugin.COVERITY_CONNECT_HOSTNAME);
        int port = settings.getInt(CoverityPlugin.COVERITY_CONNECT_PORT);
        String user = settings.getString(CoverityPlugin.COVERITY_CONNECT_USERNAME);
        String password = settings.getString(CoverityPlugin.COVERITY_CONNECT_PASSWORD);
        boolean ssl = settings.getBoolean(CoverityPlugin.COVERITY_CONNECT_SSL);

        String covProject = settings.getString(CoverityPlugin.COVERITY_PROJECT);

        CIMClient instance = new CIMClient(host, port, user, password, ssl);

        ProjectDataObj covProjectObj=null;

        try {
            covProjectObj = instance.getProject(covProject);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CovRemoteServiceException_Exception e) {
            e.printStackTrace();
        }

        String url = CoverityUtil.createURL(settings)+"reports.htm#p"+ covProjectObj.getProjectKey();
        String text = "Coverity Connect";

        if(url == null) {
            url = "http://coverity.com";
            text = "Coverity";
        }

        return String.format(
                "<div style=\"text-align:center\">" +
                "<a href=\"%s\"><img src=\"http://www.coverity.com/favicon.ico\" />%s</a>" +
                "</div>",
                url, text);
    }


}
