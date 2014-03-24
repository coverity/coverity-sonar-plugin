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

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class CoverityPluginTest {
    @Test
    public void testGetExtensions() throws Exception {
        List list = new CoverityPlugin().getExtensions();

        assertTrue("Plugin doesn't register any extensions", list.size() > 0);
    }
}
