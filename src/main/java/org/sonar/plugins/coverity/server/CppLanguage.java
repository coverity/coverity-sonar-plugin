/*
 * Coverity Sonar Plugin
 * Copyright (c) 2021 Synopsys, Inc
 * support@coverity.com
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonar.plugins.coverity.server;

import org.sonar.api.ExtensionPoint;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.coverity.CoverityPlugin;

/**
 * This class defines a language that will be added to that list of accepted languages. This language is specific to
 * the Coverity plugin C/C++ languages. Combining C and C++ was done for simplicity and a unique key/name was chosen
 * intentionally to avoid conflicts with existing sonar plugins (such as community version, c++, and licensed
 *  version, cpp).
 */
@ServerSide
@ExtensionPoint
public class CppLanguage extends AbstractLanguage {
    /**
     * Coverity C/C++ language key
     */
    public static final String KEY = "cov-cpp";

    /**
     * Coverity C/C++ language name
     */
    public static final String NAME = "C/C++";

    /**
     * Default Coverity C/C++ file suffixes including Objective-C/C++
     */
    public static final String DEFAULT_SUFFIXES = ".cpp, .cc, .c++, .cp, .cxx, .c, .hxx, .hpp, .hh, .h, .m, .mm";

    private final String[] covSuffixes;

    /**
     * Default constructor
     */
    public CppLanguage(Configuration config) {
        super(KEY, NAME);

        String[] configuredSuffixes = config.getStringArray(CoverityPlugin.COVERITY_C_CPP_SOURCE_FILE_SUFFIXES);
        if (configuredSuffixes != null && configuredSuffixes.length > 0) {
            covSuffixes = configuredSuffixes;
        } else {
            covSuffixes = DEFAULT_SUFFIXES.split(",");
        }
    }

    @Override
    public String[] getFileSuffixes() {
        return covSuffixes;
    }
}
