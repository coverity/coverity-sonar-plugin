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

package org.sonar.plugins.coverity;

import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.utils.Version;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class CoverityPluginTest {
    @Test
    public void testGetExtensions() throws Exception {
        Plugin.Context context = new Plugin.Context(Version.parse("5.6.6"));
        final CoverityPlugin coverityPlugin = new CoverityPlugin();
        coverityPlugin.define(context);

        List list = context.getExtensions();
        assertTrue("Plugin doesn't register any extensions", list.size() > 0);
    }
}
