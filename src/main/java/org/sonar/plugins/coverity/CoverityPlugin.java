/*
 * Coverity Sonar Plugin
 * Copyright (c) 2020 Synopsys, Inc
 * support@coverity.com
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonar.plugins.coverity;

import com.google.common.collect.ImmutableList;
import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.coverity.metrics.CoverityPluginMetrics;
import org.sonar.plugins.coverity.batch.CoveritySensor;
import org.sonar.plugins.coverity.server.CoverityProfiles;
import org.sonar.plugins.coverity.server.CoverityRules;
import org.sonar.plugins.coverity.ui.CoverityWidget;
import org.sonar.plugins.coverity.server.CppLanguage;
import org.sonar.plugins.coverity.ws.CIMClientFactory;

import java.util.Arrays;
import java.util.List;

public final class CoverityPlugin implements Plugin {
    public static final String COVERITY_ENABLE = "sonar.coverity.enable";
    public static final String COVERITY_CONNECT_HOSTNAME = "sonar.coverity.connect.hostname";
    public static final String COVERITY_CONNECT_PORT = "sonar.coverity.connect.port";
    public static final String COVERITY_CONNECT_USERNAME = "sonar.coverity.connect.username";
    public static final String COVERITY_CONNECT_PASSWORD = "sonar.coverity.connect.password";
    public static final String COVERITY_PROJECT = "sonar.coverity.project";
    public static final String COVERITY_STREAM = "sonar.coverity.stream";
    public static final String COVERITY_PREFIX = "sonar.coverity.prefix";
    public static final String COVERITY_SOURCE_DIRECTORY = "sonar.coverity.sources.directory";
    public static final String COVERITY_CONNECT_SSL = "sonar.coverity.ssl";
    public static final String COVERITY_C_CPP_SOURCE_FILE_SUFFIXES = "sonar.coverity.cov-cpp.suffixes";
    public static final String REPOSITORY_KEY = "coverity";

    public static List<String> COVERITY_LANGUAGES =
            Arrays.asList(
                    "java",
                    "cs",
                    "js",
                    "py",
                    "php",
                    CppLanguage.KEY);

    // This is where you're going to declare all your Sonar extensions
    private List getExtensions() {
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

                // language properties
                PropertyDefinition.builder(COVERITY_C_CPP_SOURCE_FILE_SUFFIXES)
                        .name("C/C++ source files suffixes")
                        .description("Comma-separated list of source file suffixes to retrieve issues from Coverity Connect.")
                        .defaultValue(CppLanguage.DEFAULT_SUFFIXES)
                        .subCategory("Languages")
                        .multiValues(true)
                        .index(1)
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
                /**
                 * When importing defects from cim into sonar, our plugin needs to create "issuable" objects for sonarqube.
                 * This is accomplished my matching the path of a defect on CIM with the path of a file under the sources
                 * directory used by sonar. By default, this value is "sonar.sources" which is set up on a properties
                 * file if sonar-runner is being used as executor. However, if maven is being used as executor it will
                 * not use a properties file. Instead, this property will be maven's "sourceDirectory" which might
                 * conflict with files under maven tests folder. In other words, if a file under tests has been analyzed
                 * by coverity analisis, running this executor will result in that file not being indexed by sonar.
                 * The solution for this problem is to add a property that will tell coverity which are the sources that
                 * were analyzed by coverity, which my defer from the ones scanned by the sonar executor.
                 */
                PropertyDefinition.builder(CoverityPlugin.COVERITY_SOURCE_DIRECTORY)
                        .name("Coverity Sources Directory")
                        .description("Directory that sonar will scan for sources that match defects path on CIM")
                        .type(PropertyType.STRING)
                        .onlyOnQualifiers(Qualifiers.PROJECT)
                        .index(++i)
                        .build(),

                //Batch
                CoveritySensor.class,
                CIMClientFactory.class,

                //Server
                CoverityRules.class,
                CoverityProfiles.class,
                CppLanguage.class,

                //UI
                CoverityWidget.class,

                //Base
                CoverityPluginMetrics.class
        );
    }

    @Override
    public void define(Context context) {
        context.addExtensions(getExtensions());
    }
}
