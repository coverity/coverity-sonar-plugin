/*
 * Coverity Sonar Plugin
 * Copyright 2024 Black Duck Software, Inc. All rights reserved.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonar.plugins.coverity.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.coverity.CoverityPlugin;
import org.sonar.plugins.coverity.defect.CoverityDefect;
import org.sonar.plugins.coverity.defect.CoverityDefects;
import org.sonar.plugins.coverity.ws.CIMClientFactory;

import java.util.*;

public class CoveritySensor implements Sensor {
    private static final Logger LOG = LoggerFactory.getLogger(CoveritySensor.class);
    private CIMClientFactory cimClientFactory;

    public CoveritySensor(CIMClientFactory cimClientFactory) {
        this.cimClientFactory = cimClientFactory;
    }

    @Override
    public void describe(SensorDescriptor descriptor) {

        String[] repositories = new String[CoverityPlugin.COVERITY_LANGUAGES.size()];
        for(int i = 0; i < CoverityPlugin.COVERITY_LANGUAGES.size(); i++) {
            repositories[i] = CoverityPlugin.REPOSITORY_KEY + "-" + CoverityPlugin.COVERITY_LANGUAGES.get(i);
        }

        descriptor.name(this.toString())
                .createIssuesForRuleRepositories(repositories);
    }

    @Override
    public void execute(SensorContext context) {
        Configuration config = context.config();
        boolean enabled = config.getBoolean(CoverityPlugin.COVERITY_ENABLE).orElse(false);

        LOG.info("[Coverity] " + CoverityPlugin.COVERITY_ENABLE + "=" + enabled);

        if(!enabled) {
            return;
        }

        CoverityDefects coverityDefects = new CoverityDefects(cimClientFactory, context);
        if (!coverityDefects.validateServerConfig()){
            // TODO: Provide more useful log message
            LOG.error("[Coverity] Validation of Coverity Project/Stream failed.");
            return;
        }

        List<CoverityDefect> defects = coverityDefects.retrieveCoverityDefects();
        CoverityScanner scanner = new CoverityScanner(context, defects);
        scanner.scanFiles();
        coverityDefects.addCoverityMeasures();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
