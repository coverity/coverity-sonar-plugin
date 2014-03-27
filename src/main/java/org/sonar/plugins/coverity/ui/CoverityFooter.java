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

        String serverUrl = CoverityUtil.createURL(settings);
        String url;
        String text;

        if(serverUrl == null) {
            url = "http://coverity.com";
            serverUrl = url + "/";
            text = "Coverity";
        }
        else {
            url = serverUrl + "reports.htm#p" + covProjectObj.getProjectKey();
            text = "Coverity Connect";
        }

        return String.format(
                "<div style=\"text-align:center\">" +
                "<a href=\"%s\"><img src=\"" + serverUrl + "favicon.ico\" />%s</a>" +
                "</div>",
                url, text);
    }


}
