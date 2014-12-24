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

package org.sonar.plugins.coverity.server;

import org.sonar.api.Extension;
import org.sonar.api.resources.AbstractLanguage;

/**
 * Fix Bug 71347. When adding a new profile Sonarqube's server checks if the language of that profile is on a list of
 * accepted languages. If not, it will not store rules for that profile, but it will try to create a profile anyway,
 * resulting on a null pointer exception when trying to access the rules for that profile. This can cause the server to
 * crash at star up.
 * This class defines a language that will be added to that list of accepted languages.
 */
public class CLanguage extends AbstractLanguage implements Extension {
    public static final CLanguage INSTANCE = new CLanguage();

    /**
     * CLanguage key
     */
    public static final String KEY = "c";

    /**
     * CLanguage name
     */
    public static final String NAME = "C";

    /**
     * Default package name for classes without package def
     */
    public static final String DEFAULT_PACKAGE_NAME = "[default]";

    /**
     * C files knows suffixes
     */
    public static final String[] SUFFIXES = {".c"};

    /**
     * Default constructor
     */
    public CLanguage() {
        super(KEY, NAME);
    }

    @Override
    public String[] getFileSuffixes() {
        return SUFFIXES;
    }
}
