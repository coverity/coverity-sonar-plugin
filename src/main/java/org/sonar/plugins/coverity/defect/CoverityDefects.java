/*
 * Coverity Sonar Plugin
 * Copyright (c) 2020 Synopsys, Inc
 * support@coverity.com
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonar.plugins.coverity.defect;

import com.coverity.ws.v9.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.coverity.CoverityPlugin;
import org.sonar.plugins.coverity.metrics.CoverityPluginMetrics;
import org.sonar.plugins.coverity.metrics.MetricService;
import org.sonar.plugins.coverity.util.CoverityUtil;
import org.sonar.plugins.coverity.ws.CIMClient;
import org.sonar.plugins.coverity.ws.CIMClientFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
    CoverityDefects class is responsible of connecting to CoverityConnect to retrieve Coverity defects
    Also, it is responsible for adding custom metrics at SensorContext.module level
 */
public class CoverityDefects {
    private static final Logger LOG = LoggerFactory.getLogger(CoverityDefects.class);

    private CIMClient cimClient;
    private SensorContext sensorContext;
    private Configuration config;
    private ClassLoader oldCL;

    private StreamDataObj streamDefects;
    private ProjectDataObj projectDefects;

    private boolean isStreamDefects;
    private String covStream;
    private String covProject;

    private int totalDefects = 0;
    private int highImpactDefects = 0;
    private int mediumImpactDefects = 0;
    private int lowImpactDefects = 0;

    public CoverityDefects(CIMClientFactory cimClientFactory, SensorContext sensorContext){
        this.sensorContext = sensorContext;

        Configuration config = sensorContext.config();
        this.config = config;
        this.cimClient = cimClientFactory.create(config);

        oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        System.setProperty("javax.xml.soap.MetaFactory", "com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl");
    }

    public boolean validateServerConfig(){
        covStream = config.get(CoverityPlugin.COVERITY_STREAM).orElse(StringUtils.EMPTY);
        covProject = config.get(CoverityPlugin.COVERITY_PROJECT).orElse(StringUtils.EMPTY);

        /**
         * Checks whether a stream has been specified.
         */
        if (covStream != null && !covStream.isEmpty()){
            isStreamDefects = true;

            // Find specified stream
            try{

                streamDefects = cimClient.getStream(covStream);
                if(streamDefects == null) {
                    LOG.error("Couldn't find stream: " + covStream);
                    Thread.currentThread().setContextClassLoader(oldCL);
                    return false;
                }
                LOG.info("Found stream: " + covStream + " (" + streamDefects.getId() + ")");

            } catch (IOException | CovRemoteServiceException_Exception e) {
                LOG.error("Error while trying to find stream: " + covStream);
                Thread.currentThread().setContextClassLoader(oldCL);
                return false;
            }
        } else{
            LOG.debug("Stream has not been specified. Proceed with checking project");
            isStreamDefects = false;

            /**
             * Checks whether a project has been specified.
             */
            if(covProject == null || covProject.isEmpty()) {
                LOG.error("Couldn't find project: " + covProject);
                Thread.currentThread().setContextClassLoader(oldCL);
                return false;
            }

            // Find specified project
            try {
                projectDefects = cimClient.getProject(covProject);
                if(projectDefects == null) {
                    LOG.error("Couldn't find project: " + covProject);
                    Thread.currentThread().setContextClassLoader(oldCL);
                    return false;
                }
                LOG.info("Found project: " + covProject + " (" + projectDefects.getProjectKey() + ")");

            } catch (IOException | CovRemoteServiceException_Exception e) {
                LOG.error("Error while trying to find project: " + covProject);
                Thread.currentThread().setContextClassLoader(oldCL);
                return false;
            }
        }

        return true;
    }

    public List<CoverityDefect> retrieveCoverityDefects(){
        List<CoverityDefect> coverityDefectList = new ArrayList<CoverityDefect>();
        totalDefects = 0;
        highImpactDefects = 0;
        mediumImpactDefects = 0;
        lowImpactDefects = 0;

        try{
            List<MergedDefectDataObj> defects = retrieveDefects();
            if (defects == null || defects.isEmpty()){
                return coverityDefectList;
            }

            Map<Long, StreamDefectDataObj> streamDefects = cimClient.getStreamDefectsForMergedDefects(defects);
            LOG.info("Found " + streamDefects.size() + " defects");

            for (MergedDefectDataObj defect : defects){
                if (shouldFilterOut(defect)){
                    continue;
                }

                List<DefectInstanceDataObj> defectInstances = streamDefects.get(defect.getCid()).getDefectInstances();

                if (defectInstances == null || defectInstances.isEmpty()) {
                    LOG.info("The merged defect with CID " + defect.getCid() + "has no defect instances defined.");
                    continue;
                }

                for (DefectInstanceDataObj defectInstance : defectInstances){
                    EventDataObj mainEvent = getMainEvent(defectInstance);
                    String mainEventFilePath = getMainEventFilePath(mainEvent);
                    if (StringUtils.isEmpty(mainEventFilePath)){
                        mainEventFilePath = defect.getFilePathname();
                    }

                    String severity = defectInstances.get(0).getImpact().getDisplayName();
                    countCoverityDefectSeverity(severity);

                    CoverityDefect coverityDefect = new CoverityDefect(
                            defect.getCid(),
                            defect.getDomain(),
                            mainEventFilePath,
                            defectInstance.getSubcategory(),
                            defectInstance.getCheckerName(),
                            defect.getMergeKey(),
                            generateMessageTemplate(),
                            mainEvent.getEventDescription(),
                            defectInstance.getLongDescription(),
                            mainEvent.getEventTag(),
                            defect.getDisplayType(),
                            severity,
                            mainEvent.getLineNumber()
                    );

                    coverityDefectList.add(coverityDefect);
                }

            }

        }catch(Exception e){
            LOG.error("[Coverity] Exception occurred during retrieving defects", e);
        }

        return coverityDefectList;
    }

    private List<MergedDefectDataObj> retrieveDefects() throws Exception {
        List<MergedDefectDataObj> defects = null;

        if (isStreamDefects){
            LOG.info("Fetching defects for stream: " + covStream);
            defects = cimClient.getDefectsFromStream(covStream);
        } else {
            LOG.info("Fetching defects for project: " + covProject);
            defects = cimClient.getDefectsFromProject(covProject);
        }

        return defects;
    }

    private boolean shouldFilterOut(MergedDefectDataObj defectDataObj){
        List<DefectStateAttributeValueDataObj> listOfAttributes = defectDataObj.getDefectStateAttributeValues();
        String status = StringUtils.EMPTY;

        for(DefectStateAttributeValueDataObj defectAttribute : listOfAttributes){
            if(defectAttribute.getAttributeDefinitionId().getName().equals("DefectStatus")){
                status = defectAttribute.getAttributeValueId().getName();
            }
        }

        if ("Dismissed".equals(status) || "Fixed".equals(status) || "Absent Dismissed".equals(status)) {
            LOG.info("Skipping resolved defect (CID " + defectDataObj.getCid() + ", status '" + status + "')");
            return true;
        }

        return false;
    }

    private String generateMessageTemplate() {
        StringBuilder url = new StringBuilder();
        url.append(String.format("%s://%s:%d/query/defects.htm?", cimClient.isUseSSL() ? "https" : "http", cimClient.getHost(), cimClient.getPort()));

        if (isStreamDefects){
            url.append(String.format("stream=%s", streamDefects.getId().getName()));
        } else{
            url.append(String.format("projectId=%s", projectDefects.getProjectKey()));
        }
        return url.toString();
    }

    private EventDataObj getMainEvent(DefectInstanceDataObj defectInstance) {
        if(defectInstance.getEvents() != null && !defectInstance.getEvents().isEmpty()){
            for(EventDataObj edo : defectInstance.getEvents()) {
                if(edo.isMain()) {
                    return edo;
                }
            }
        }

        // If no event is marked as "main" the first event is returned.
        return defectInstance.getEvents().get(0);
    }

    private String getMainEventFilePath(EventDataObj mainEvent){
        String mainEventFilePath = StringUtils.EMPTY;
        String platform = System.getProperty("os.name");

        if (mainEvent != null){
            FileIdDataObj fileIdDataObj = mainEvent.getFileId();
            if (fileIdDataObj != null){
                mainEventFilePath = fileIdDataObj.getFilePathname();
            }
        }

        if (!StringUtils.isEmpty(mainEventFilePath)){
            mainEventFilePath = applyStripPath(mainEventFilePath);

            if (platform.startsWith("Windows")) {
                mainEventFilePath = mainEventFilePath.replace("/", "\\");
            }
        }

        return mainEventFilePath;
    }

    private String applyStripPath(String mainEventFilePath){
        String currentDir = System.getProperty("user.dir");
        File currentDirFile = new File(currentDir);

        String stripPrefix = config.get(CoverityPlugin.COVERITY_PREFIX).orElse(StringUtils.EMPTY);
        String strippedFilePath;

        if (!StringUtils.isEmpty(stripPrefix)&& mainEventFilePath.startsWith(stripPrefix)){
            strippedFilePath = mainEventFilePath.substring(stripPrefix.length());
            mainEventFilePath = new File(currentDirFile, strippedFilePath).getAbsolutePath();
            LOG.info("Full path after prefix being stripped: " + mainEventFilePath);
        }

        return mainEventFilePath;
    }

    private void countCoverityDefectSeverity(String severity){
        if(!StringUtils.isEmpty(severity)){
             totalDefects++;
             if (severity.equals(CoverityDefect.SEVERITY_HIGH)) {
                 highImpactDefects++;
             }else if (severity.equals(CoverityDefect.SEVERITY_MEDIUM)) {
                 mediumImpactDefects++;
             }else {
                 lowImpactDefects++;
             }
         }
    }

    public void addCoverityMeasures() {
        String covProject = config.get(CoverityPlugin.COVERITY_PROJECT).orElse(null);

        if (covProject != null) {
            MetricService.addMetric(sensorContext,
                    CoverityPluginMetrics.COVERITY_PROJECT_NAME,
                    covProject,
                    sensorContext.module());
        }

        String projectUrl  = CoverityUtil.createURL(cimClient);
        if (projectUrl != null) {
            MetricService.addMetric(sensorContext,
                    CoverityPluginMetrics.COVERITY_URL_CIM_METRIC,
                    projectUrl,
                    sensorContext.module());
        }

        if (projectDefects != null){
            String ProductKey= String.valueOf(projectDefects.getProjectKey());
            projectUrl = projectUrl+"reports.htm#p"+ProductKey;

            MetricService.addMetric(sensorContext,
                    CoverityPluginMetrics.COVERITY_PROJECT_URL,
                    projectUrl,
                    sensorContext.module());
        }

        MetricService.addMetric(sensorContext,
                CoverityPluginMetrics.COVERITY_OUTSTANDING_ISSUES,
                totalDefects,
                sensorContext.module());

        MetricService.addMetric(sensorContext,
                CoverityPluginMetrics.COVERITY_HIGH_IMPACT,
                highImpactDefects,
                sensorContext.module());

        MetricService.addMetric(sensorContext,
                CoverityPluginMetrics.COVERITY_MEDIUM_IMPACT,
                mediumImpactDefects,
                sensorContext.module());

        MetricService.addMetric(sensorContext,
                CoverityPluginMetrics.COVERITY_LOW_IMPACT,
                lowImpactDefects,
                sensorContext.module());
    }
}
