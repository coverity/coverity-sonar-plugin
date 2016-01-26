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

package org.sonar.plugins.coverity.util;

import com.coverity.ws.v9.MergedDefectDataObj;
import com.coverity.ws.v9.ProjectDataObj;
import org.sonar.plugins.coverity.ws.CIMClient;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

public class FileGenerator {
    public static Map<String, String> languageDomains = new HashMap<String, String>();
    static String name;

    static {
        languageDomains.put("java", "STATIC_JAVA");
        languageDomains.put("cpp", "STATIC_C");
        languageDomains.put("cs", "STATIC_CS");
    }

    public static void generateRulesFiles(File propsFile, File xmlDir, File htmlDir, CIMClient instance) throws Exception {
        propsFile.getParentFile().mkdirs();
        xmlDir.mkdirs();
        PrintWriter propsFileOut = new PrintWriter(propsFile);

        Set<MergedDefectDataObj> defectsOnCIMInstance = new HashSet<MergedDefectDataObj>();

        for(ProjectDataObj covProject : instance.getProjects()){
            defectsOnCIMInstance.addAll(instance.getDefects(covProject.getId().getName()));
        }

        for(Map.Entry<String, String> entry : languageDomains.entrySet()) {
            List<String> lineList = new ArrayList<String>();
            String language = entry.getKey();
            String domain = entry.getValue();

            File xmlFile = new File(xmlDir, "coverity-" + language + ".xml");
            PrintWriter xmlFileOut = new PrintWriter(xmlFile,"UTF-8" );
            xmlFileOut.println("<rules>");

            for(MergedDefectDataObj mddo : defectsOnCIMInstance) {
                String key = CoverityUtil.flattenMergedDefectCheckerName(mddo);
                String desc = mddo.getDisplayCategory();
                {
                    String linkRegex = "\\(<a href=\"([^\"]*?)\" target=\"_blank\">(.*?)</a>\\)";
                    String codeRegex = "<code>(.*?)</code>";

                    desc = desc.replaceAll(linkRegex, "");
                    desc = desc.replaceAll(codeRegex, "$1");
                }

                //xml
                xmlFileOut.println("<rule>");
                name = mddo.getDisplayType();
                if(name.isEmpty() || name == null){
                    xmlFileOut.println("<name>" + key + "</name>");
                } else {
                    name = org.apache.commons.lang.StringEscapeUtils.escapeXml(name);
                    xmlFileOut.println("<name>" + name + "</name>");
                }
                xmlFileOut.println("<key>" + key + "</key>");
                String severity = "MAJOR";
                String impact = mddo.getDisplayImpact();
                if(impact.equals("High")){
                    severity = "BLOCKER";
                }
                if(impact.equals("Medium")){
                    severity = "CRITICAL";
                }
                xmlFileOut.println("<severity>" + severity + "</severity>");
                xmlFileOut.println("<configKey>" + key + "</configKey>");
                xmlFileOut.println("<description><![CDATA[ " + desc + "]]></description>");
                xmlFileOut.println("</rule>");

                //props
                lineList.add("rule.coverity-java." + key + ".name=" + mddo.getDisplayType());
            }

            xmlFileOut.println("</rules>");
            xmlFileOut.close();

            Collections.sort(lineList);

            for(String line : lineList){
                propsFileOut.println(line);
            }
        }

        propsFileOut.close();
    }

    public static void main(String[] args) throws Exception {
        File propsFile = new File("src/main/resources/org/sonar/l10n/coverity.properties");
        File xmlDir = new File("src/main/resources/org/sonar/plugins/coverity/server");
        File htmlDir = new File("src/main/resources/org/sonar/l10n/coverity/rules");

        System.out.println("propsFile=" + propsFile.getAbsolutePath());
        System.out.println("xmlDir=" + xmlDir.getAbsolutePath());
        System.out.println("htmlDir=" + htmlDir.getAbsolutePath());

        CIMClient instance = new CIMClient("frossi-wrkst", 8082, "admin", "coverity", false);

        generateRulesFiles(propsFile, xmlDir, htmlDir, instance);
    }
}
