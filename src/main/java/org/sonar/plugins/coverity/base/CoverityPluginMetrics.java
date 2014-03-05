package org.sonar.plugins.coverity.base;

/*
 * Coverity Sonar Plugin
 * Copyright (C) 2014 Coverity, Inc.
 * support@coverity.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

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
