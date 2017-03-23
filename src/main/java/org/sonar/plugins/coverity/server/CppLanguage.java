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

package org.sonar.plugins.coverity.server;

import org.sonar.api.ExtensionPoint;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.server.ServerSide;

/**
 * This class defines a language that will be added to that list of accepted languages. This language is specific to
 * the Coverity plugin C/C++ languages. Combining C and C++ was done for simplicity and a unique key/name was chosen
 * intentionally to avoid conflicts with existing sonar plugins (such as community version, c++, and licensed
 *  version, cpp).
 */
@ServerSide
@ExtensionPoint
public class CppLanguage extends AbstractLanguage {
    public static final CppLanguage INSTANCE = new CppLanguage();

    /**
     * Coverity C/C++ language key
     */
    public static final String KEY = "cov-cpp";

    /**
     * Cpp name
     */
    public static final String NAME = "C/C++";

    /**
     * Default package name for classes without package def
     */
    public static final String DEFAULT_PACKAGE_NAME = "[default]";

    /**
     * Cpp files knows suffixes
     */
    public static final String[] SUFFIXES = {".cpp", ".cc", ".c++", ".cp", ".cxx", ".c", ".hxx", ".hpp", ".hh", ".h"};

    /**
     * Default constructor
     */
    public CppLanguage() {
        super(KEY, NAME);
    }

    @Override
    public String[] getFileSuffixes() {
        return SUFFIXES;
    }
}
