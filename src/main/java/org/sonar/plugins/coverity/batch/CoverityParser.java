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
import org.sonar.plugins.coverity.defect.CoverityDefect;
import org.sonar.plugins.coverity.metrics.MetricService;
import org.sonar.plugins.coverity.util.CoverityRuleUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CoverityParser {
    private static final Logger LOG = LoggerFactory.getLogger(CoverityParser.class);

    private SensorContext sensorContext;
    private FileSystem fileSystem;
    private HashMap<String, List<CoverityDefect>> coverityDefectsMap;

    public CoverityParser(SensorContext sensorContext, List<CoverityDefect> coverityDefects){
        this.sensorContext = sensorContext;
        this.fileSystem = sensorContext.fileSystem();

        coverityDefectsMap = new HashMap<>();
        populateCoverityDefectsMap(coverityDefects);
    }

    public void scanFiles(){
        Iterable<InputFile> inputFiles = fileSystem.inputFiles(fileSystem.predicates().all());

        for(InputFile inputFile : inputFiles){
            if (inputFile.isFile()){
                scan(inputFile);
            }
        }
    }

    private void scan(InputFile inputFile){
        String inputFilePath = (new File(inputFile.uri())).getAbsolutePath();
        LOG.debug("[Coverity] InputFile Path: " + inputFilePath);

        if (coverityDefectsMap.containsKey(inputFilePath)){
            for (CoverityDefect defect : coverityDefectsMap.get(inputFilePath)){

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
        }

        // Only add metrics if inputFile's language is known
        if (!StringUtils.isEmpty(inputFile.language())){
            MetricService.addMetric(sensorContext, CoreMetrics.NCLOC, inputFile.lines(), inputFile);
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
}
