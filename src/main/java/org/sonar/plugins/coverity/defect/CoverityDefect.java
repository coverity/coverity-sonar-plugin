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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class CoverityDefect {
    public static String DEFAULT_SUBCATEGORY = "none";
    public static String SEVERITY_HIGH = "High";
    public static String SEVERITY_MEDIUM = "Medium";

    private Long cid;
    private String domain;
    private String eventPath;
    private String subcategory;
    private String checkerName;
    private String mergeKey;
    private String messageTemplate;
    private String description;
    private String longDescription;
    private String eventTag;
    private String displayType;
    private String severity;
    private int lineNumber;

    public CoverityDefect(
            Long cid,
            String domain,
            String eventPath,
            String subcategory,
            String checkerName,
            String mergeKey,
            String messageTemplate,
            String description,
            String longDescription,
            String eventTag,
            String displayType,
            String severity,
            int lineNumber){
        this.cid = cid;
        this.domain = domain;
        this.eventPath = eventPath;
        this.subcategory = !StringUtils.isEmpty(subcategory) ? subcategory : DEFAULT_SUBCATEGORY;
        this.checkerName = checkerName;
        this.mergeKey = mergeKey;
        this.messageTemplate = messageTemplate;
        this.description = description;
        this.longDescription = longDescription;
        this.eventTag = eventTag;
        this.displayType = displayType;
        this.severity = severity;
        this.lineNumber = lineNumber;
    }

    public Long getCid(){
        return this.cid;
    }

    public String getDomain(){
        return this.domain;
    }

    public String getEventPath(){
        return this.eventPath;
    }

    public String getSubcategory(){
        return this.subcategory;
    }

    public String getCheckerName(){
        return this.checkerName;
    }

    public String getDescription(){
        return this.description;
    }

    public String getSeverity(){
        return this.severity;
    }

    public int getLineNumber(){
        return this.lineNumber;
    }

    public String getDefectMessage(){
        StringBuilder message = new StringBuilder();
        message.append("[" + displayType + "] ");

        if (StringUtils.isEmpty(description) || StringUtils.isEmpty(eventTag)){
            message.append(longDescription);
        } else {
            message.append(eventTag + ": " + description);
        }

        StringBuilder url = new StringBuilder(messageTemplate);
        url.append(String.format("&mergeKey=%s", mergeKey));

        return StringEscapeUtils.unescapeHtml(message.toString()) + " ( CID " + cid + " : " + url + " )";
    }
}
