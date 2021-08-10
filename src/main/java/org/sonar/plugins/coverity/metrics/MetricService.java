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

package org.sonar.plugins.coverity.metrics;

import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.Metric;

import java.io.Serializable;

public class MetricService {

    public static <T extends Serializable> void addMetric(SensorContext sensorContext, Metric<T> metric, T value, InputComponent inputComponent){
        try{
            sensorContext.<T>newMeasure().forMetric(metric).on(inputComponent).withValue(value).save();
        }catch(Exception e){
            // Sometimes other plugins already add the same metric which cases IllegalStateException
            return;
        }
    }
}
