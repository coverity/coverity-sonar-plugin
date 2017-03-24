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

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.plugins.coverity.CoverityPlugin;

import static org.junit.Assert.assertArrayEquals;

public class CppLanguageTest {

    private Settings settings;

    @Before
    public void setUp() {
        settings = new Settings();
    }

    @Test
    public void getFileSuffixes_returnsDefaultFileSuffixes() {
        CppLanguage language = new CppLanguage(settings);

        String[] expectedSuffixes = CppLanguage.DEFAULT_SUFFIXES.split(",");

        assertArrayEquals(expectedSuffixes, language.getFileSuffixes());
    }

    @Test
    public void getFileSuffixes_returnsConfiguredFileSuffixes() {
        settings.setProperty(CoverityPlugin.COVERITY_C_CPP_SOURCE_FILE_SUFFIXES, ".c, .cpp, .cxx");
        CppLanguage language = new CppLanguage(settings);

        String[] expectedSuffixes = {".c", ".cpp",".cxx"};

        assertArrayEquals(expectedSuffixes, language.getFileSuffixes());
    }
}
