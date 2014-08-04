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

package org.sonar.plugins.coverity;

import com.google.common.collect.ImmutableList;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.coverity.base.CoverityPluginMetrics;
import org.sonar.plugins.coverity.batch.CoveritySensor;
import org.sonar.plugins.coverity.server.CoverityProfiles;
import org.sonar.plugins.coverity.server.CoverityRules;
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
    public static final String COVERITY_PROJECT = "sonar.coverity.project";
    public static final String COVERITY_PREFIX = "sonar.coverity.prefix";
    public static final String COVERITY_CONNECT_SSL = "sonar.coverity.ssl";
    public static final String REPOSITORY_KEY = "coverity";

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
                /*
                * Coverity analysis may not be performed on the same directory as Sonar analysis,
                * so in some case we need to remove the beginning of the filename to make it
                * relative to Sonar's project root.This can be done by specifying the prefix to remove from filenames
                * with the 'sonar.coverity.prefix' key.
                * */
                PropertyDefinition.builder(CoverityPlugin.COVERITY_PREFIX)
                        .name("Coverity Files Prefix")
                        .description("Prefix to strip from filenames to match this Sonar project")
                        .type(PropertyType.STRING)
                        .onlyOnQualifiers(Qualifiers.PROJECT)
                        .index(++i)
                        .build(),

                //Batch
                CoveritySensor.class,

                //Server
                CoverityRules.class,
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
