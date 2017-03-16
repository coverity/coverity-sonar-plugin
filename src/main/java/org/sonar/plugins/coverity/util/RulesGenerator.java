/*
 * Coverity Sonar Plugin
 * Copyright (c) 2017 Coverity, Inc
 * support@coverity.com
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonar.plugins.coverity.util;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sonar.plugins.coverity.server.InternalRule;

import java.io.*;
import java.util.*;

public class RulesGenerator {

    static Map<String, Map<String, List<InternalRule>>> rulesList = new HashMap<String, Map<String, List<InternalRule>>>();
    static final String JAVA_LANGUAGE = "java";
    static final String CPP_LANGUAGE = "cpp";
    static final String CS_LANGUAGE = "cs";
    static final String VULNERABILITY = "VULNERABILITY";
    static final String BUG = "BUG";

    /*
    RulesGenerator is used to generate rules based on the coverity quality checker-properties.json files
    and Find bug checkers that coverity will understand( required to be named findbugs-checker-properties.json )
    The file paths are needed to passed as main method's parameters.
     */
    public static void main(String[] args) throws Exception {

        File xmlDir = new File("src/main/resources/org/sonar/plugins/coverity/server");

        if (args.length == 0) {
            System.out.println("Need to provide path to the checker-properties.json files or find bugs checker file");
            return;
        }

        for (String filePath : args) {
            File file = new File(filePath);
            if (filePath.endsWith("findbugs-checker-properties.json")) {
                generateRulesForFindBugCheckers(file);
            } else {
                generateRulesForQualityCheckers(file);
            }
        }

        addNoneSubcategory();
        addFallbackRuleForLanguage();
        addDifferentOriginRules();

        writeRulesToFiles(xmlDir);
    }

    public static void generateRulesForQualityCheckers(File jsonFile) throws Exception {
        JSONParser parser = new JSONParser();
        int javaRule = 0;
        int cppRule = 0;
        int csRule = 0;

        try {
            Object obj = parser.parse(new FileReader(jsonFile.getAbsolutePath()));
            JSONArray jsonObject =  (JSONArray) obj;

            Iterator<JSONObject> iterator = jsonObject.iterator();

            while( iterator.hasNext() ) {
                JSONObject childJSON = (JSONObject)iterator.next();

                String checkerName = (String) childJSON.get("checkerName");
                String subcategory = (String) childJSON.get("subcategory");
                String impact = (String) childJSON.get("impact");
                String subcategoryLongDescription = (String) childJSON.get("subcategoryLongDescription");
                List<String> families = (ArrayList<String>) childJSON.get("families");
                String domain = (String) childJSON.get("domain");
                String language = (String) childJSON.get("language");

                String category = (String) childJSON.get("category");
                String subcategoryShortDescription = (String) childJSON.get("subcategoryShortDescription");
                String name = category + " : " + subcategoryShortDescription;
                String key = checkerName + "_" + subcategory;

                boolean qualityKind;
                boolean securityKind;
                Object quality = childJSON.get("qualityKind");
                if (quality instanceof String) {
                    qualityKind = Boolean.parseBoolean((String) quality);
                }else {
                    qualityKind = (boolean) quality;
                }

                Object security = childJSON.get("securityKind");
                if (security instanceof String) {
                    securityKind = Boolean.parseBoolean((String) security);
                }else {
                    securityKind = (boolean) security;
                }

                List<String> languages = new ArrayList<>();

                // Using families
                if (StringUtils.isEmpty(domain) && StringUtils.isEmpty(language)
                        && families != null && !families.isEmpty()) {
                    for (String family : families) {
                        String lang = findLanguage(family);
                        if (!StringUtils.isEmpty(lang)) {
                            languages.add(lang);
                        }
                    }
                }

                // Using domain
                else if (!StringUtils.isEmpty(domain) && StringUtils.isEmpty(language)
                        && (families == null || families.isEmpty())) {
                    String lang = findLanguage(domain);
                    if (!StringUtils.isEmpty(lang)) {
                        languages.add(lang);
                    }
                }

                // Using language
                else if (!StringUtils.isEmpty(language) && StringUtils.isEmpty(domain)
                        && (families == null || families.isEmpty())) {
                    String lang = findLanguage(language);
                    if (!StringUtils.isEmpty(lang)) {
                        languages.add(lang);
                    }
                }

                for(String lang : languages) {
                    InternalRule rule = new InternalRule(
                            key,
                            name,
                            checkerName,
                            getSeverity(impact),
                            subcategory,
                            getDescription(subcategoryLongDescription),
                            getRuleType(qualityKind, securityKind));
                    putRuleIntoMap(lang, rule);

                    if (lang.equals(JAVA_LANGUAGE)) {
                        javaRule++;
                    } else if (lang.equals(CPP_LANGUAGE)) {
                        cppRule++;
                    } else if (lang.equals(CS_LANGUAGE)) {
                        csRule++;
                    }
                }
            }

            /**
             * Print out to the console the results of the rules generation so that developers can analyze these results.
             */
            System.out.println("FilePath: " + jsonFile.getAbsolutePath() + " JavaRule: " + javaRule);
            System.out.println("FilePath: " + jsonFile.getAbsolutePath() + " CppRule: " + cppRule);
            System.out.println("FilePath: " + jsonFile.getAbsolutePath() + " CsRule: " + csRule);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void generateRulesForFindBugCheckers(File jsonFile) throws Exception {
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(jsonFile.getAbsolutePath()));
            JSONObject root = (JSONObject) obj;
            JSONArray issues =  (JSONArray) root.get("issue_type");
            System.out.println("FilePath : " + jsonFile.getAbsolutePath() + " JSONArray Size: " + issues.size());

            Iterator<JSONObject> iterator = issues.iterator();

            while( iterator.hasNext() ) {
                JSONObject childJSON = (JSONObject) iterator.next();

                String checkerName = (String) childJSON.get("type");
                String subcategory = (String) childJSON.get("subtype");

                JSONObject name = (JSONObject) childJSON.get("name");
                String ruleName = (String) name.get("en");

                JSONObject desc = (JSONObject) childJSON.get("description");
                String description = (String) desc.get("en");

                JSONObject properties = (JSONObject) childJSON.get("cim_checker_properties");
                String impact = (String) properties.get("impact");
                boolean qualityKind = (boolean) properties.get("qualityKind");
                boolean securityKind = (boolean) properties.get("securityKind");

                String key = checkerName + "_" + subcategory;

                InternalRule rule = new InternalRule(
                        key,
                        ruleName,
                        checkerName,
                        getSeverity(impact),
                        subcategory,
                        getDescription(description),
                        getRuleType(qualityKind, securityKind));
                putRuleIntoMap(JAVA_LANGUAGE, rule);
            }
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
        for(String language : rulesList.keySet()){
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
            String domain = null;
            if (language.equals(JAVA_LANGUAGE)) {
                domain = "STATIC_JAVA";
            } else if (language.equals(CPP_LANGUAGE)) {
                domain = "STATIC_C";
            } else if (language.equals(CS_LANGUAGE)) {
                domain = "STATIC_CS";
            }

            for (String key : rulesList.get(language).keySet()) {
                for (InternalRule rule : rulesList.get(language).get(key)) {
                    xmlFileOut.println("    <rule>");
                    xmlFileOut.println("        <key>" + StringEscapeUtils.escapeXml(domain + "_" + rule.getKey()) + "</key>");
                    xmlFileOut.println("        <name>" + StringEscapeUtils.escapeXml(rule.getRuleName()) + "</name>");
                    xmlFileOut.println("        <internalKey>" + StringEscapeUtils.escapeXml(domain + "_" + rule.getKey()) + "</internalKey>");
                    xmlFileOut.println("        <description>" + StringEscapeUtils.escapeXml(rule.getDescription()) + "</description>");
                    xmlFileOut.println("        <severity>" + StringEscapeUtils.escapeXml(rule.getSeverity()) + "</severity>");
                    xmlFileOut.println("        <cardinality>SINGLE</cardinality>");
                    xmlFileOut.println("        <status>READY</status>");
                    xmlFileOut.println("        <type>" + StringEscapeUtils.escapeXml(rule.getRuleType()) + "</type>");

                    for (String tag : rule.getTags()) {
                        xmlFileOut.println("        <tag>" + StringEscapeUtils.escapeXml(tag) + "</tag>");
                    }

                    xmlFileOut.println("    </rule>");
                }
            }

            xmlFileOut.println("</rules>");
            xmlFileOut.close();
            System.out.println("The following file has been updated: " + xmlFile.getPath());
        }
    }

    public static String findLanguage(String lang) {
        if (lang.equals("Java") || lang.equals("STATIC_JAVA")) {
            return JAVA_LANGUAGE;
        } else if (lang.equals("C/C++")){
            return CPP_LANGUAGE;
        } else if (lang.equals("C#")) {
            return CS_LANGUAGE;
        }

        return StringUtils.EMPTY;
    }

    public static String getDescription(String description) {
        String linkRegex = "\\(<a href=\"([^\"]*?)\" target=\"_blank\">(.*?)</a>\\)";
        String codeRegex = "<code>(.*?)</code>";

        description = description.replaceAll(linkRegex, "");
        description = description.replaceAll(codeRegex, "$1");
        description = description.trim();

        return description;
    }

    public static String getSeverity(String impact) {
        String severity = "MAJOR";
        if(impact.equals("High")){
            severity = "BLOCKER";
        }
        if(impact.equals("Medium")){
            severity = "CRITICAL";
        }

        return severity;
    }

    public static void putRuleIntoMap(String language, InternalRule rule) {
        String key = rule.getCheckerName();

        if (!rulesList.containsKey(language)) {
            rulesList.put(language, new HashMap<String, List<InternalRule>>());
        }

        if (!rulesList.get(language).containsKey(key)) {
            List<InternalRule> list = new ArrayList<InternalRule>();
            list.add(rule);
            rulesList.get(language).put(key, list);
        } else {
            if (!rulesList.get(language).get(key).contains(rule)) {
                rulesList.get(language).get(key).add(rule);
            }
        }
    }

    public static String getRuleType(boolean qualityKind, boolean securityKind) {
        if (!qualityKind & securityKind) {
            return VULNERABILITY;
        } else {
            return BUG;
        }
    }

    public static void addNoneSubcategory() {
        Map<String, List<InternalRule>> missingList = new HashMap<String, List<InternalRule>>();
        int missingChecker = 0;

        // Find rule without "none" subcategory
        for (String language : rulesList.keySet()) {
            for (String key: rulesList.get(language).keySet()) {
                boolean isNoneIncluded = false;
                InternalRule rule = null;
                for (InternalRule currentRule : rulesList.get(language).get(key)) {
                    if (currentRule.getSubcategory().equals("none")) {
                        isNoneIncluded = true;
                    }
                    rule = currentRule;
                }

                if (!isNoneIncluded) {
                    if (!missingList.containsKey(language)) {
                        missingList.put(language, new ArrayList<>());
                    }
                    missingList.get(language).add(rule);
                    missingChecker++;
                }
            }
        }

        // Print the number of missing rules without "none" subcategory
        System.out.println("Missing None Subcategory Checerk: " + missingChecker);

        // Add rules with "none" subcategory
        for (String language : missingList.keySet()) {
            for (InternalRule rule : missingList.get(language)) {
                InternalRule newRule = new InternalRule(
                        rule.getCheckerName() + "none",
                        rule.getRuleName(),
                        rule.getCheckerName(),
                        rule.getSeverity(),
                        "none",
                        rule.getDescription(),
                        BUG);

                rulesList.get(language).get(newRule.getCheckerName()).add(newRule);
            }
        }
    }

    public static void addFallbackRuleForLanguage() {
        for (String language : rulesList.keySet()) {
            InternalRule newRule = new InternalRule(
                "coverity-" + language,
                "Coverity General " + StringUtils.upperCase(language),
                "coverity-" + language,
                "MAJOR",
                "none",
                "Coverity General " + StringUtils.upperCase(language),
                BUG
            );

            List<InternalRule> list = new ArrayList<InternalRule>();
            list.add(newRule);
            rulesList.get(language).put(newRule.getCheckerName(), list);
        }
    }

    public static void addDifferentOriginRules() {

        List<InternalRule> rules = new ArrayList<>();
        InternalRule misraRule = new InternalRule(
                "MISRA.*",
                "Coverity MISRA : Coding Standard Violation",
                "MISRA.*",
                "MAJOR",
                "none",
                "Coverity MISRA : Coding Standard Violation",
                BUG
        );
        rules.add(misraRule);

        InternalRule pwRule = new InternalRule(
                "PW.*",
                "Coverity PW : Parse Warnings",
                "PW.*",
                "MAJOR",
                "none",
                "Coverity PW : Parse Warnings",
                BUG
        );
        rules.add(pwRule);

        InternalRule swRule = new InternalRule(
                "SW.*",
                "Coverity SW : Semantic Warnings",
                "SW.*",
                "MAJOR",
                "none",
                "Coverity SW : Semantic Warnings",
                BUG
        );
        rules.add(swRule);

        InternalRule rwRule = new InternalRule(
                "RW.*",
                "Coverity RW : Recovery Warnings",
                "RW.*",
                "MAJOR",
                "none",
                "Coverity RW : Recovery Warnings",
                BUG
        );
        rules.add(rwRule);

        InternalRule msvscaRule = new InternalRule(
                "MSVSCA.*",
                "Coverity MSVSCA : Microsoft Visual Studio Code Analysis",
                "MSVSCA.*",
                "MAJOR",
                "none",
                "Coverity MSVSCA : Microsoft Visual Studio Code Analysis",
                BUG
        );
        rules.add(msvscaRule);

        for (InternalRule rule : rules) {
            List<InternalRule> tempList = new ArrayList<InternalRule>();
            tempList.add(rule);
            if (rule.getKey().equals("MSVSCA.*")) {
                rulesList.get(CS_LANGUAGE).put(rule.getCheckerName(), tempList);
            } else {
                rulesList.get(JAVA_LANGUAGE).put(rule.getCheckerName(), tempList);
            }
        }
    }

    public static void printRulesList() {

        int javaRules = 0;
        int cppRules = 0;
        int csRules = 0;

        for (String language : rulesList.keySet()) {
            for (String checkerName : rulesList.get(language).keySet()) {
                if (language.equals(JAVA_LANGUAGE)) {
                    javaRules += rulesList.get(language).get(checkerName).size();
                } else if (language.equals(CPP_LANGUAGE)) {
                    cppRules += rulesList.get(language).get(checkerName).size();
                } else if (language.equals(CS_LANGUAGE)) {
                    csRules += rulesList.get(language).get(checkerName).size();
                }
            }
        }

        System.out.println("Total Java Rules: " + javaRules);
        System.out.println("Total C/C++ Rules: " + cppRules);
        System.out.println("Total C# Rules: " + csRules);
    }

}
