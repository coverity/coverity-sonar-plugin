/*
 * Coverity Sonar Plugin
 * Copyright (c) 2017 Coverity, Inc
 * support@coverity.com
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonar.plugins.coverity.ui;

import org.sonar.api.config.Settings;
import org.sonar.api.web.Footer;
import org.sonar.plugins.coverity.CoverityPlugin;
import org.sonar.plugins.coverity.util.CoverityUtil;


public final class CoverityFooter implements Footer{
    Settings settings;

    public CoverityFooter(Settings settings) {
        this.settings = settings;
    }

    public String getHtml() {
        String host = settings.getString(CoverityPlugin.COVERITY_CONNECT_HOSTNAME);
        int port = settings.getInt(CoverityPlugin.COVERITY_CONNECT_PORT);

        /**
         * After the server has been started but before we run the command "sonar-runner" (or setting properties from
         * the GUI), the properties for "settings" are not set yet.
         * Because of this we cannot make the footer to redirect the user to his CIM instance. In this case we will
         * send him to "www.coverity.com".
         */
        if(host == null || port == 0 ){
            String url = "http://www.coverity.com";
            String text = "Coverity";
            return String.format(
                    "<div style=\"text-align:center\">" +
                            "<a href=\"%s\"><img src=\"http://go.coverity.com/rs/157-LQW-289/images/sig-truly-covered-check-50x50.png\n\" />%s</a>" +
                            "</div>",
                    url, text);
        }

        String url = CoverityUtil.createURL(settings)+"reports.htm#p";
        String text = "Coverity Connect";

        return String.format(
                "<div style=\"text-align:center\">" +
                        "<a href=\"%s\"><img src=\"http://go.coverity.com/rs/157-LQW-289/images/sig-truly-covered-check-50x50.png\n\" />%s</a>" +
                        "</div>",
                url, text);
    }
}
