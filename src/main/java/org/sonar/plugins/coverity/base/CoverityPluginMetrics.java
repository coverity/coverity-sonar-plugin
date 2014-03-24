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

package org.sonar.plugins.coverity.base;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import java.util.Arrays;
import java.util.List;

// This class construct the metrics that will be used by org/sonar/plugins/coverity/batch/CoveritySensor.java
// to build measures that will be used by ruby
public class CoverityPluginMetrics implements Metrics{

    public static String DOMAIN = new String("Coverity");

    // This metric will contain the URL to CIM.
    public static final Metric COVERITY_URL_CIM_METRIC = new Metric.Builder("COVERITY-URL-CIM-METRIC", "Url cim metric", Metric.ValueType.STRING)
            .setDirection(Metric.DIRECTION_NONE)
            .setQualitative(true)
            .setDomain(DOMAIN)
            .create();

    // This metric will contain the name of the project.
    public static final Metric COVERITY_PROJECT_NAME = new Metric.Builder("COVERITY-PROJECT-NAME", "Project Name", Metric.ValueType.STRING)
            .setDirection(Metric.DIRECTION_NONE)
            .setQualitative(true)
            .setDomain(DOMAIN)
            .create();

    // This metric will contain the URL to the project
    public static final Metric COVERITY_PROJECT_URL = new Metric.Builder("COVERITY-PROJECT-URL", "Project Url", Metric.ValueType.STRING)
            .setDirection(Metric.DIRECTION_NONE)
            .setQualitative(true)
            .setDomain(DOMAIN)
            .create();

    // This metric will contain the total number of defects in the project.
    // This also counts defects for which the method getResourceForFile() returned null.
    public static final Metric COVERITY_OUTSTANDING_ISSUES = new Metric.Builder("COVERITY-OUTSTANDING-ISSUES", "Outstanding Issues", Metric.ValueType.STRING)
            .setDirection(Metric.DIRECTION_NONE)
            .setQualitative(true)
            .setDomain(DOMAIN)
            .create();

    // This metric will contain the number of defects with high impact.
    public static final Metric COVERITY_HIGH_IMPACT  = new Metric.Builder("COVERITY-HIGH-IMPACT", "High Impact", Metric.ValueType.STRING)
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(false)
            .setDomain(DOMAIN)
            .create();

    // This metric will contain the number of defects with medium impact.
    public static final Metric COVERITY_MEDIUM_IMPACT  = new Metric.Builder("COVERITY-MEDIUM-IMPACT", "Medium Impact", Metric.ValueType.STRING)
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(false)
            .setDomain(DOMAIN)
            .create();

    // This metric will contain the number of defects with low impact.
    public static final Metric COVERITY_LOW_IMPACT  = new Metric.Builder("COVERITY-LOW-IMPACT", "Low Impact", Metric.ValueType.STRING)
            .setDirection(Metric.DIRECTION_WORST)
            .setQualitative(false)
            .setDomain(DOMAIN)
            .create();


    public List<Metric> getMetrics() {
        return Arrays.asList(COVERITY_URL_CIM_METRIC, COVERITY_PROJECT_NAME, COVERITY_PROJECT_URL, COVERITY_OUTSTANDING_ISSUES, COVERITY_HIGH_IMPACT,
                COVERITY_MEDIUM_IMPACT, COVERITY_LOW_IMPACT);
    }
}
