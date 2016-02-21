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

import org.sonar.api.rules.RulePriority;
import org.sonar.plugins.coverity.server.InternalRule;

import java.io.*;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.sonar.plugins.coverity.server.CoverityRules;

public class FileGenerator {
    public static Map<String, String> languageDomains = new HashMap<String, String>();

    static {
        languageDomains.put("java", "STATIC_JAVA");
        languageDomains.put("cpp", "STATIC_C");
        languageDomains.put("cs", "STATIC_CS");
    }

    public static Map<String, InternalRule> javaCheckerProp = new HashMap<String, InternalRule>();
    public static Map<String, InternalRule> cppCheckerProp = new HashMap<String, InternalRule>();
    public static Map<String, InternalRule> csCheckerProp = new HashMap<String, InternalRule>();

    public static Map<String, Map<String, InternalRule>> mapOfCheckerPropMaps = new HashMap<String, Map<String, InternalRule>>();

    static{
        mapOfCheckerPropMaps.put("java", javaCheckerProp);
        mapOfCheckerPropMaps.put("cpp", cppCheckerProp);
        mapOfCheckerPropMaps.put("cs", csCheckerProp);
    }

    public static void generateRulesFromJSONFile(File jsonFile) throws Exception {

        /**
         * Rules are no longer imported from a CIM instance using the deprecated v6 getCheckersProperties().
         * Instead we parse a JSON file containing all the checker properties. This file can be found at:
         * http://artifactory.internal.synopsys.com:8081/artifactory/simple/libs-snapshots-local/com/coverity/prevent/prevent-checker-info/8.0.0-SNAPSHOT/
         */
        org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();

        try {
            Object obj = parser.parse(new FileReader(jsonFile.getAbsolutePath()));

            JSONArray jsonObject =  (JSONArray) obj;

            Iterator<JSONObject> iterator = jsonObject.iterator();

            // Count of all checkers found on the given JSON file.
            int iteratorCount = 0;

            while( iterator.hasNext() ) {
                iteratorCount++;
                JSONObject childJSON = (JSONObject)iterator.next();

                String checkerName = (String) childJSON.get("checkerName");
                String impact = (String) childJSON.get("impact");
                String domain = (String) childJSON.get("domain");
                String subcategoryLongDescription = (String) childJSON.get("subcategoryLongDescription");
                String subcategoryShortDescription = (String) childJSON.get("subcategoryShortDescription");
                List<String> families = (ArrayList<String>) childJSON.get("families");

                {
                    String linkRegex = "\\(<a href=\"([^\"]*?)\" target=\"_blank\">(.*?)</a>\\)";
                    String codeRegex = "<code>(.*?)</code>";

                    subcategoryLongDescription = subcategoryLongDescription.replaceAll(linkRegex, "");
                    subcategoryLongDescription = subcategoryLongDescription.replaceAll(codeRegex, "$1");
                }

                /**
                 * Checkers specify the languages they support by providing a "domain" field or by providing a list of
                 * langueges under the field "families".
                 * First we decide what are the languages corresponding for each checker. If this can not be determined,
                 * an error is printed to the console.
                 */
                if(domain == null && families == null){
                    System.out.println("Language is not defined for checker: " + checkerName);
                }

                List<String> languages = new ArrayList<String>();

                if((domain != null && domain.equals("STATIC_JAVA")) || (families != null && families.contains("Java"))){
                    languages.add("java");
                }

                if((domain != null && domain.equals("STATIC_C")) || (families != null && (families.contains("C/C++") ||
                        families.contains("Objective-C/C++")))){
                    languages.add("cpp");
                }

                if((domain != null && domain.equals("STATIC_CS")) || (families != null && families.contains("C#"))){
                    languages.add("cs");
                }

                /**
                 * Rules are created for each language and store on a hashmap. There is one map per language.
                 */
                for(String language : languages){
                    String key = languageDomains.get(language) + "_" + checkerName ;
                    InternalRule ir = new InternalRule();
                    ir.setKey(key);
                    ir.setName(checkerName);
                    if(subcategoryShortDescription.isEmpty() || subcategoryShortDescription == null){
                        ir.setName(key);
                    } else {
                        subcategoryShortDescription = org.apache.commons.lang.StringEscapeUtils.escapeXml(subcategoryShortDescription);
                        ir.setName(subcategoryShortDescription);
                    }
                    String severity = "MAJOR";
                    if(impact.equals("High")){
                        severity = "BLOCKER";
                    }
                    if(impact.equals("Medium")){
                        severity = "CRITICAL";
                    }
                    ir.setSeverity(severity);
                    ir.setDescription(subcategoryLongDescription);
                    mapOfCheckerPropMaps.get(language).put(key, ir);
                }
            }

            /**
             * Print out to the console the results of the rules generation so that developers can analyze these results.
             */
            System.out.println("Number of rules loaded from JSON for java: " + javaCheckerProp.size());
            System.out.println("Number of rules loaded from JSON for cs: " + csCheckerProp.size());
            System.out.println("Number of rules loaded from JSON for cpp: " + cppCheckerProp.size());
            System.out.println("Total number of checkers on JSON: " + iteratorCount);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write the result of the rules generation to one xml file per language. This is the step that actually updates the
     * resources used by the plugin.
     */
    public static void writeRulesToFiles(File xmlDir){
        /**
         * Print out rules for each language.
         */
        for(String language : languageDomains.keySet()){
            File xmlFile = new File(xmlDir, "coverity-" + language + ".xml");
            PrintWriter xmlFileOut = null;
            try {
                xmlFileOut = new PrintWriter(xmlFile,"UTF-8" );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            xmlFileOut.println("<rules>");

            for(InternalRule rule : mapOfCheckerPropMaps.get(language).values()){
                //xml
                xmlFileOut.println("    <rule>");
                xmlFileOut.println("        <name>" + rule.getName() + "</name>");
                xmlFileOut.println("        <key>" + rule.getKey() + "</key>");
                xmlFileOut.println("        <severity>" + rule.getSeverity() + "</severity>");
                xmlFileOut.println("        <configKey>" + rule.getKey() + "</configKey>");
                xmlFileOut.println("        <description><![CDATA[ " + rule.getDescription() + "]]></description>");
                xmlFileOut.println("    </rule>");
            }
            xmlFileOut.println("</rules>");
            xmlFileOut.close();
            System.out.println("The following file has been updated: " + xmlFile.getPath());
        }
    }

    public static void loadRulesFromXMLFiles(File oldXMLDir){

        for(String language : languageDomains.keySet()){

            File oldFile = new File(oldXMLDir.getAbsolutePath(), "old-coverity-" + language + ".xml");
            Document doc = null;

            try {
                InputStream in = new FileInputStream(oldFile.getAbsolutePath());
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                doc = dBuilder.parse(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            doc.getDocumentElement().normalize();

            NodeList nodes = doc.getElementsByTagName("rule");

            int oldRulesloadedPerLanguage = 0;
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                String key = "";
                String name = "";
                String severity = "";
                String description = "";

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    key = CoverityUtil.getValue("key", element);
                    name = CoverityUtil.getValue("name", element);
                    severity = CoverityUtil.getValue("severity", element);
                    description = CoverityUtil.getValue("description", element);
                }

                InternalRule ir = new InternalRule();
                ir.setName(name);
                ir.setKey(key);
                ir.setDescription(description);
                ir.setSeverity(severity);
                ir.setLanguage(language);

                mapOfCheckerPropMaps.get(language).put(key, ir);
                oldRulesloadedPerLanguage++;
            }
            System.out.println("Number of rules loaded from old xml for language " + language + ": " + oldRulesloadedPerLanguage);
        }
    }

    public static void main(String[] args) throws Exception {

        File xmlDir = new File("src/main/resources/org/sonar/plugins/coverity/server");
        File oldXmlDir = new File("/Users/frossi/Desktop/workspace/sonar-feb-2016/sonar2/sonar_plugin/src/main/resources/org/sonar/plugins/coverity/old-list-of-rules/");
        /**
         * JSON file containing checker properties. The file can be found at
         * http://artifactory.internal.synopsys.com:8081/artifactory/simple/libs-snapshots-local/com/coverity/prevent/prevent-checker-info/8.0.0-SNAPSHOT/
         * Download the jar file: prevent-checker-info-8.0.0-20151212.023543-256.jar.
         * Extract it and get the file: checker-properties.json
         * Modify the path given below.
         */
        File jsonFile = new File("/Users/frossi/Desktop/workspace/sonar-feb-2016/checker-properties.json");

        System.out.println("xmlDir=" + xmlDir.getAbsolutePath());
        System.out.println("OldXmlDir=" + oldXmlDir.getAbsolutePath());
        System.out.println("JSONFile=" + jsonFile.getAbsolutePath());

        generateRulesFromJSONFile(jsonFile);
        loadRulesFromXMLFiles(oldXmlDir);

        /**
         * Print out to the console the results of the rules generation so that developers can analyze these results.
         */
        System.out.println("Number of rules for Java: " + javaCheckerProp.values().size());
        System.out.println("Number of rules for C++: " + cppCheckerProp.values().size());
        System.out.println("Number of rules for C#: " + csCheckerProp.values().size());
        int totalRulesCount = javaCheckerProp.values().size()
                + cppCheckerProp.values().size() + csCheckerProp.values().size();
        System.out.println("Total number of rules: " + totalRulesCount);

        writeRulesToFiles(xmlDir);
    }
}
