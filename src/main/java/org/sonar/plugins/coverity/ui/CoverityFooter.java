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
package org.sonar.plugins.coverity.ui;

import org.sonar.api.config.Settings;
import org.sonar.api.web.Footer;
import org.sonar.plugins.coverity.util.CoverityUtil;

public final class CoverityFooter implements Footer {
    Settings settings;

    public CoverityFooter(Settings settings) {
        this.settings = settings;
    }

    public String getHtml() {
        String url = CoverityUtil.createURL(settings);
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
