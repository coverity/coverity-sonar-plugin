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

package org.sonar.plugins.coverity.util;

import com.coverity.ws.v6.CheckerPropertyDataObj;
import com.coverity.ws.v6.CheckerPropertyFilterSpecDataObj;
import org.sonar.plugins.coverity.ws.CIMClient;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileGenerator {
    public static Map<String, String> languageDomains = new HashMap<String, String>();

    static {
        languageDomains.put("java", "STATIC_JAVA");
        languageDomains.put("cpp", "STATIC_C");
        languageDomains.put("c++", "STATIC_C");
        languageDomains.put("cs", "STATIC_CS");
    }

    public static void generateRulesFiles(File propsFile, File xmlDir, File htmlDir, CIMClient instance) throws Exception {
        propsFile.getParentFile().mkdirs();
        xmlDir.mkdirs();
        PrintWriter propsFileOut = new PrintWriter(propsFile);

        for(Map.Entry<String, String> entry : languageDomains.entrySet()) {
            String language = entry.getKey();
            String domain = entry.getValue();

            //File htmlDirDir = new File(htmlDir, "coverity-" + language);
            //htmlDirDir.mkdirs();

            File xmlFile = new File(xmlDir, "coverity-" + language + ".xml");
            PrintWriter xmlFileOut = new PrintWriter(xmlFile);
            xmlFileOut.println("<rules>");

            CheckerPropertyFilterSpecDataObj filter = new CheckerPropertyFilterSpecDataObj();
            filter.getDomainList().add(domain);
            List<CheckerPropertyDataObj> checkers = instance.getConfigurationService().getCheckerProperties(filter);

            for(CheckerPropertyDataObj cpdo : checkers) {
                String key = CoverityUtil.flattenCheckerSubcategoryId(cpdo.getCheckerSubcategoryId());

                //File htmlFile = new File(htmlDirDir, key + ".html");

                //PrintWriter htmlFileOut = new PrintWriter(htmlFile);

                String desc = cpdo.getSubcategoryLongDescription();
                {
                    String linkRegex = "\\(<a href=\"([^\"]*?)\" target=\"_blank\">(.*?)</a>\\)";
                    String codeRegex = "<code>(.*?)</code>";

                    desc = desc.replaceAll(linkRegex, "");
                    desc = desc.replaceAll(codeRegex, "$1");
                }

                //xml
                xmlFileOut.println("<rule>");
                xmlFileOut.println("<key>" + key + "</key>");
                xmlFileOut.println("<priority>" + "MAJOR" + "</priority>");
                xmlFileOut.println("<configKey>" + key + "</configKey>");
                xmlFileOut.println("<description><![CDATA[ " + desc + "]]></description>");
                xmlFileOut.println("</rule>");

                //props
                propsFileOut.println("rule.coverity-" + language + "." + key + ".name=" + cpdo.getSubcategoryShortDescription());

                //html
                //htmlFileOut.println(cpdo.getSubcategoryLongDescription());

                //htmlFileOut.close();
            }

            xmlFileOut.println("</rules>");
            xmlFileOut.close();
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

        CIMClient instance = new CIMClient("jvinson-wrkst", 14800, "admin", "coverity", false);

        generateRulesFiles(propsFile, xmlDir, htmlDir, instance);
    }
}
