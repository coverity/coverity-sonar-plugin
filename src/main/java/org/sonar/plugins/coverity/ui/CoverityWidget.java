package org.sonar.plugins.coverity.ui;

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
import org.sonar.api.web.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WidgetCategory("Coverity")
public class CoverityWidget extends AbstractRubyTemplate implements RubyRailsWidget {

    private static final Logger LOG = LoggerFactory.getLogger(CoverityWidget.class);

    public CoverityWidget(){
        LOG.info("Widget is being instantiated");
    }

    public String getId() {
        return "coverity";
    }

    public String getTitle() {
        return "Coverity";
    }

    @Override
    protected String getTemplatePath() {

        return "/org/sonar/plugins/coverity/ui/coverity-widget.html.erb";
    }

}

