/*
 * Coverity Sonar Plugin
 * Copyright 2024 Black Duck Software, Inc. All rights reserved.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonar.plugins.coverity.server;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.coverity.CoverityPlugin;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CppLanguageTest {
    private Configuration config;

    @Before
    public void setUp() {
        config = mock(Configuration.class);
    }

    @Test
    public void getFileSuffixes_returnsDefaultFileSuffixes() {
        CppLanguage language = new CppLanguage(config);

        String[] expectedSuffixes = CppLanguage.DEFAULT_SUFFIXES.split(",");

        assertArrayEquals(expectedSuffixes, language.getFileSuffixes());
    }

    @Test
    public void getFileSuffixes_returnsConfiguredFileSuffixes() {
        when(config.getStringArray(CoverityPlugin.COVERITY_C_CPP_SOURCE_FILE_SUFFIXES)).thenReturn(new String[] {".c", ".cpp", ".cxx"});
        CppLanguage language = new CppLanguage(config);

        String[] expectedSuffixes = {".c", ".cpp",".cxx"};

        assertArrayEquals(expectedSuffixes, language.getFileSuffixes());
    }
}
