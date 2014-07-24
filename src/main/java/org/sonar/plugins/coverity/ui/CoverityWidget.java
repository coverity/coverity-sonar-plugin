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

import org.sonar.api.web.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WidgetCategory("Coverity")
@Description("Displays the breakdown of outstanding Coverity defects by impact.")
public class CoverityWidget extends AbstractRubyTemplate implements RubyRailsWidget {

    private static final Logger LOG = LoggerFactory.getLogger(CoverityWidget.class);

    public String getId() {
        return "coverity";
    }

    public String getTitle() {
        return "Coverity";
    }

    @Override
    // It specifies the template in which the ruby code will be added
    protected String getTemplatePath() {
        return "/org/sonar/plugins/coverity/ui/coverity-widget.html.erb";
    }

}

