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

package org.sonar.plugins.coverity.batch;

import com.coverity.ws.v6.CheckerPropertyDataObj;
import com.coverity.ws.v6.DefectInstanceDataObj;
import com.coverity.ws.v6.EventDataObj;
import com.coverity.ws.v6.MergedDefectDataObj;
import com.coverity.ws.v6.ProjectDataObj;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.coverity.ws.CIMClient;
import org.sonar.plugins.coverity.ws.TripleFromDefects;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CoveritySensorTest {
    Settings settings;
    RulesProfile profile;
    ResourcePerspectives resourcePerspectives;
    CoveritySensor sensor;

    @Before
    public void setUp() throws Exception {
        settings = mock(Settings.class);
        profile = mock(RulesProfile.class);
        resourcePerspectives = mock(ResourcePerspectives.class);

        sensor = new CoveritySensor(settings, profile, resourcePerspectives);
    }

    @Test
    public void testShouldExecuteOnProject() throws Exception {
        Project project = mock(Project.class);
        //assertTrue(sensor.shouldExecuteOnProject(project));
    }

    @Test
    public void testGetIssueMessage() throws Exception {
        //
    }

    @Test
    public void testGetCheckerProperties() throws Exception {
        //
    }

    @Test
    public void testGetDefectURL() throws Exception {
        CIMClient instance = mock(CIMClient.class);
        ProjectDataObj projectObj = mock(ProjectDataObj.class);
        MergedDefectDataObj mddo = mock(MergedDefectDataObj.class);

        String target = "http://&&HOST&&:999999/sourcebrowser.htm?projectId=888888#mergedDefectId=777777";

        when(instance.getHost()).thenReturn("&&HOST&&");
        when(instance.getPort()).thenReturn(999999);
        when(projectObj.getProjectKey()).thenReturn(888888L);
        when(mddo.getCid()).thenReturn(777777L);
        String url = sensor.getDefectURL(instance, projectObj, mddo);

        assertEquals(target, url);
    }

    @Test
    public void testGetMainEvent() throws Exception {
        DefectInstanceDataObj dido = new DefectInstanceDataObj();

        EventDataObj em = new EventDataObj();
        em.setMain(true);

        dido.getEvents().add(em);

        int n = 10;
        for(int i = 0; i < n; i++) {
            dido.getEvents().add(new EventDataObj());
        }

        Collections.swap(dido.getEvents(), 0, n / 2);

        EventDataObj result = sensor.getMainEvent(dido);
        assertEquals("Found wrong event", em, result);
    }

    @Test
    public void testGetResourceForFile() throws Exception {
        //
    }

    @Test
    public void testGetCoverityLogoMeasures() throws Exception {

        SensorContext sensorContextTest = mock(SensorContext.class);
        Metric coverityUrlCimMetricTest = mock(Metric.class);
        Measure measure = new Measure(coverityUrlCimMetricTest);
        final String CIM_URL = "testUrl";
        measure.setData(CIM_URL);
        sensorContextTest.saveMeasure(measure);

        when(sensorContextTest.getMeasure(coverityUrlCimMetricTest)).thenReturn(measure);

        assertEquals(CIM_URL, (sensorContextTest.getMeasure(coverityUrlCimMetricTest)).getData());
    }
}
