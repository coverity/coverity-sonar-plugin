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
package org.sonar.plugins.coverity.ws;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.coverity.CoverityPlugin;

import javax.annotation.Nonnull;

@ScannerSide
public class CIMClientFactory {

    public CIMClient create(@Nonnull Configuration config) {
        Validate.notNull(config);

        String host = config.get(CoverityPlugin.COVERITY_CONNECT_HOSTNAME).orElse(StringUtils.EMPTY);
        int port = config.getInt(CoverityPlugin.COVERITY_CONNECT_PORT).orElse(0);
        String user = config.get(CoverityPlugin.COVERITY_CONNECT_USERNAME).orElse(StringUtils.EMPTY);
        String password = config.get(CoverityPlugin.COVERITY_CONNECT_PASSWORD).orElse(StringUtils.EMPTY);
        boolean ssl = config.getBoolean(CoverityPlugin.COVERITY_CONNECT_SSL).orElse(false);

        return new CIMClient(host, port, user, password, ssl);
    }
}
