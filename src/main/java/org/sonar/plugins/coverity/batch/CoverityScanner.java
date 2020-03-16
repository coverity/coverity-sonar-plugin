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

package org.sonar.plugins.coverity.batch;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.api.batch.fs.internal.DefaultTextRange;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.coverity.CoverityPlugin;
import org.sonar.plugins.coverity.defect.CoverityDefect;
import org.sonar.plugins.coverity.metrics.MetricService;
import org.sonar.plugins.coverity.util.CoverityRuleUtil;
import org.sonar.plugins.coverity.util.CoverityUtil;

import java.io.File;
import java.util.*;

/*
    CoverityScanner is responsible for adding coverity defects as SonarQube issues.
    Also, it provides metrics at the InputFile level
 */
public class CoverityScanner {
    private static final Logger LOG = LoggerFactory.getLogger(CoverityScanner.class);

    private SensorContext sensorContext;
    private FileSystem fileSystem;
    private HashMap<String, List<CoverityDefect>> coverityDefectsMap;
    private HashSet<InputFile> foundInputFiles;

    public CoverityScanner(SensorContext sensorContext, List<CoverityDefect> coverityDefects){
        this.sensorContext = sensorContext;
        this.fileSystem = sensorContext.fileSystem();

        coverityDefectsMap = new HashMap<>();
        populateCoverityDefectsMap(coverityDefects);
    }

    public void scanFiles(){
        Iterable<InputFile> inputFiles = fileSystem.inputFiles(fileSystem.predicates().all());
        foundInputFiles = new HashSet<>();
        addCoverityIssues();

        // Following codes are required to add CoreMetrics.NCLOC metrics for any input files
        // that coverity defects free. Without below logic, some input files will not have
        // any lines of codes for particular files in SonarQube.
        for(InputFile inputFile : inputFiles){
            if (inputFile.isFile()
                && !foundInputFiles.contains(inputFile)
                && !StringUtils.isEmpty(inputFile.language())){
                MetricService.addMetric(sensorContext, CoreMetrics.NCLOC, inputFile.lines(), inputFile);
            }
        }
    }

    private void addCoverityIssues(){
        for (String defectPath : coverityDefectsMap.keySet()){
            InputFile inputFile = findInputFile(defectPath);
            if (inputFile == null){
                LOG.error("[Coverity] Could not find the local input file");
                continue;
            }

            for (CoverityDefect defect : coverityDefectsMap.get(defectPath)){
                ActiveRule activeRule = CoverityRuleUtil.findActiveRule(
                        sensorContext,
                        defect.getDomain(),
                        defect.getCheckerName(),
                        defect.getSubcategory(),
                        inputFile.language());

                if (activeRule == null){
                    LOG.error("[Coverity] Could not find active rule for " + defect.getCid());
                    continue;
                }

                String message = defect.getDefectMessage();
                final DefaultTextPointer start = new DefaultTextPointer(defect.getLineNumber(), 0);
                NewIssue issue = sensorContext.newIssue();

                NewIssueLocation issueLocation = issue
                        .newLocation()
                        .on(inputFile)
                        .at(new DefaultTextRange(start, start))
                        .message(message);

                issue.forRule(activeRule.ruleKey())
                        .at(issueLocation);

                issue.save();
            }

            MetricService.addMetric(sensorContext, CoreMetrics.NCLOC, inputFile.lines(), inputFile);
            foundInputFiles.add(inputFile);
        }
    }

    private void populateCoverityDefectsMap(List<CoverityDefect> coverityDefects){
        for(CoverityDefect defect : coverityDefects){
            if (!coverityDefectsMap.containsKey(defect.getEventPath())){
                coverityDefectsMap.put(defect.getEventPath(), new ArrayList<CoverityDefect>());
            }

            coverityDefectsMap.get(defect.getEventPath()).add(defect);
            LOG.debug("[Coverity] CID: " + defect.getCid() + "\tEventPath: " + defect.getEventPath());
        }
    }

    private InputFile findInputFile(String defectPath){
        final FileSystem fileSystem = sensorContext.fileSystem();
        InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasPath(defectPath));

        if(inputFile == null) {
            for(File possibleFile : getListOfLocalFiles()){
                if(possibleFile.getAbsolutePath().endsWith(defectPath)) {
                    inputFile = fileSystem.inputFile(fileSystem.predicates().hasPath(possibleFile.getAbsolutePath()));
                    break;
                }
            }
        }

        return inputFile;
    }

    private List<File> getListOfLocalFiles(){
        String covSrcDir = sensorContext.config().get(CoverityPlugin.COVERITY_SOURCE_DIRECTORY).orElse(StringUtils.EMPTY);
        List<File> listOfFiles = new ArrayList<File>();
        String sonarSourcesString = null;

        if(covSrcDir != null && !covSrcDir.isEmpty()){
            sonarSourcesString = covSrcDir;
        } else {
            sonarSourcesString = sensorContext.config().get("sonar.sources").orElse(StringUtils.EMPTY);
        }
        if(sonarSourcesString != null && !sonarSourcesString.isEmpty()){
            List<String> sonarSources = Arrays.asList(sonarSourcesString.split(","));
            for(String dir : sonarSources){
                File folder = new File(dir);
                listOfFiles.addAll(CoverityUtil.listFiles(folder));
            }
        }

        return listOfFiles;
    }
}
