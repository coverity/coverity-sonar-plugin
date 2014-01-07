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
package org.sonar.plugins.coverity;

import com.google.common.collect.ImmutableList;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.coverity.batch.CoveritySensor;
import org.sonar.plugins.coverity.server.CoverityRulesRepositories;

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


	// This is where you're going to declare all your Sonar extensions
    public List getExtensions() {
        int i = 0;
        return ImmutableList.of(
                //Properties
                PropertyDefinition.builder(CoverityPlugin.COVERITY_ENABLE)
                        .name("Enable Coverity")
                        .description("Enable Coverity defect import")
                        .defaultValue("false")
                        .type(PropertyType.BOOLEAN)
                        .onQualifiers(Qualifiers.PROJECT)
                        .index(++i)
                        .build(),
                PropertyDefinition.builder(CoverityPlugin.COVERITY_CONNECT_HOSTNAME)
                        .name("Connect Hostname")
                        .description("Hostname of the Connect server to import defects from")
                        .type(PropertyType.STRING)
                        .index(++i)
                        .build(),
                PropertyDefinition.builder(CoverityPlugin.COVERITY_CONNECT_PORT)
                        .name("Connect Port")
                        .description("Port of the Connect server to import defects from")
                        .type(PropertyType.INTEGER)
                        .index(++i)
                        .build(),
                PropertyDefinition.builder(CoverityPlugin.COVERITY_CONNECT_USERNAME)
                        .name("Connect Username")
                        .description("Username to access defects in Connect with")
                        .type(PropertyType.STRING)
                        .index(++i)
                        .build(),
                PropertyDefinition.builder(CoverityPlugin.COVERITY_CONNECT_PASSWORD)
                        .name("Connect Password")
                        .description("Password to access defects in Connect with")
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
                        .description("Coverity project corresponding to this Sonar project")
                        .type(PropertyType.STRING)
                        .onlyOnQualifiers(Qualifiers.PROJECT)
                        .index(++i)
                        .build(),

                //Batch
                CoveritySensor.class,

                //Server
                CoverityRulesRepositories.class

                //UI

        );
    }
}
