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

package org.sonar.plugins.coverity;

import com.google.common.collect.ImmutableList;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.coverity.base.CoverityPluginMetrics;
import org.sonar.plugins.coverity.batch.CoveritySensor;
import org.sonar.plugins.coverity.server.CoverityProfiles;
import org.sonar.plugins.coverity.server.CoverityRulesRepositories;
import org.sonar.plugins.coverity.ui.CoverityFooter;
import org.sonar.plugins.coverity.ui.CoverityWidget;

import java.util.List;

public final class CoverityPlugin extends SonarPlugin {
    public static final String COVERITY_ENABLE = "sonar.coverity.enable";
    public static final String COVERITY_CONNECT_HOSTNAME = "sonar.coverity.connect.hostname";
    public static final String COVERITY_CONNECT_PORT = "sonar.coverity.connect.port";
    public static final String COVERITY_CONNECT_USERNAME = "sonar.coverity.connect.username";
    public static final String COVERITY_CONNECT_PASSWORD = "sonar.coverity.connect.password";
    public static final String COVERITY_PROJECT = "sonar.coverity.stream";
    public static final String COVERITY_CONNECT_SSL = "sonar.coverity.ssl";
    public static final String REPOSITORY_KEY = "coverity";
    //public static final String COVERITY_WIDGET = "coverity";

    // This is where you're going to declare all your Sonar extensions
    public List getExtensions() {
        int i = 0;
        return ImmutableList.of(
                //Properties
                PropertyDefinition.builder(CoverityPlugin.COVERITY_ENABLE)
                        .name("Enable Coverity")
                        .description("Enables Coverity issue import")
                        .defaultValue("false")
                        .type(PropertyType.BOOLEAN)
                        .onQualifiers(Qualifiers.PROJECT)
                        .index(++i)
                        .build(),
                PropertyDefinition.builder(CoverityPlugin.COVERITY_CONNECT_HOSTNAME)
                        .name("Coverity Connect Hostname")
                        .description("Hostname of the Coverity Connect server from which to import issues")
                        .type(PropertyType.STRING)
                        .index(++i)
                        .build(),
                PropertyDefinition.builder(CoverityPlugin.COVERITY_CONNECT_PORT)
                        .name("Coverity Connect Port")
                        .description("Port of the Coverity Connect server from which to import issues")
                        .type(PropertyType.INTEGER)
                        .index(++i)
                        .build(),
                PropertyDefinition.builder(CoverityPlugin.COVERITY_CONNECT_USERNAME)
                        .name("Coverity Connect Username")
                        .description("Username to access issues in Coverity Connect")
                        .type(PropertyType.STRING)
                        .index(++i)
                        .build(),
                PropertyDefinition.builder(CoverityPlugin.COVERITY_CONNECT_PASSWORD)
                        .name("Coverity Connect Password")
                        .description("Password to access issues in Coverity Connect")
                        .type(PropertyType.PASSWORD)
                        .index(++i)
                        .build(),
                PropertyDefinition.builder(CoverityPlugin.COVERITY_CONNECT_SSL)
                        .name("Use SSL")
                        .description("Use SSL to interact with Coverity Connect")
                        .defaultValue("false")
                        .type(PropertyType.BOOLEAN)
                        .index(++i)
                        .build(),
                PropertyDefinition.builder(CoverityPlugin.COVERITY_PROJECT)
                        .name("Coverity Project")
                        .description("The project in Coverity Connect corresponding to this Sonar project")
                        .type(PropertyType.STRING)
                        .onlyOnQualifiers(Qualifiers.PROJECT)
                        .index(++i)
                        .build(),

                //Batch
                CoveritySensor.class,

                //Server
                CoverityRulesRepositories.class,
                CoverityProfiles.class,

                //UI
                CoverityFooter.class,

                //UI
                CoverityWidget.class,

                //Base
                CoverityPluginMetrics.class
        );
    }
}
