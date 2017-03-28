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
import org.sonar.plugins.coverity.server.CppLanguage;
import org.sonar.plugins.coverity.server.InternalRule;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RulesGenerator {

    static Map<String, Map<String, List<InternalRule>>> rulesList = new HashMap<String, Map<String, List<InternalRule>>>();
    static final String JAVA_LANGUAGE = "java";
    static final String CPP_LANGUAGE = CppLanguage.KEY;
    static final String CS_LANGUAGE = "cs";
    static final String JAVASCRIPT_LANGUAGE = "js";
    static final String PYTHON_LANGUAGE = "py";
    static final String PHP_LANGUAGE = "php";
    static final String OBJECTIVE_C = "objective-c";
    static final String VULNERABILITY = "VULNERABILITY";
    static final String BUG = "BUG";

    static String outputFilePath = "src/main/resources/org/sonar/plugins/coverity/server";

    /*
    RulesGenerator is used to generate rules based on the coverity quality checker-properties.json files
    and Find bug checkers that coverity will understand( required to be named findbugs-checker-properties.json )
    The file paths are needed to passed as main method's parameters.
     */
    public static void main(String[] args) throws Exception {

        File xmlDir = new File(outputFilePath);

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

        try {
            Object obj = parser.parse(new InputStreamReader(new FileInputStream(jsonFile.getAbsolutePath()), StandardCharsets.UTF_8));
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
                            getRuleType(qualityKind, securityKind),
                            lang);
                    addLanguageTag(rule);
                    addRuleTypeTag(rule, qualityKind, securityKind);
                    putRuleIntoMap(lang, rule);
                }
            }

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
            Object obj = parser.parse(new InputStreamReader(new FileInputStream(jsonFile.getAbsolutePath()), StandardCharsets.UTF_8));

            JSONObject root = (JSONObject) obj;
            JSONArray issues =  (JSONArray) root.get("issue_type");

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
                        getRuleType(qualityKind, securityKind),
                        JAVA_LANGUAGE);
                addAdditionalTag(rule, "findbugs");
                addLanguageTag(rule);
                addRuleTypeTag(rule, qualityKind, securityKind);
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
            } else if (language.equals(CPP_LANGUAGE) || language.equals(OBJECTIVE_C)) {
                domain = "STATIC_C";
            } else if (language.equals(CS_LANGUAGE)) {
                domain = "STATIC_CS";
            } else if (language.equals(JAVASCRIPT_LANGUAGE) || language.equals(PYTHON_LANGUAGE) || language.equals(PHP_LANGUAGE)) {
                domain = "OTHER";
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
        } else if (lang.equals("JavaScript")) {
            return JAVASCRIPT_LANGUAGE;
        } else if (lang.equals("Python")) {
            return PYTHON_LANGUAGE;
        } else if (lang.equals("PHP")) {
            return PHP_LANGUAGE;
        } else if (lang.equals("Objective-C/C++")) {
            return OBJECTIVE_C;
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
        String lang;
        if (language.equals(OBJECTIVE_C)) {
            lang = CPP_LANGUAGE;
        } else {
            lang = language;
        }

        if (!rulesList.containsKey(lang)) {
            rulesList.put(lang, new HashMap<String, List<InternalRule>>());
        }

        if (!rulesList.get(lang).containsKey(key)) {
            List<InternalRule> list = new ArrayList<InternalRule>();
            list.add(rule);
            rulesList.get(lang).put(key, list);
        } else {
            if (!rulesList.get(lang).get(key).contains(rule)) {
                rulesList.get(lang).get(key).add(rule);
            } else {
                for(InternalRule rule1 : rulesList.get(lang).get(key)) {
                    if (rule1.equals(rule)) {
                        for (String tag: rule.getTags()) {
                            if (!rule1.getTags().contains(tag)) {
                                rule1.getTags().add(tag);
                            }
                        }
                    }
                }
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
                }
            }
        }

        // Add rules with "none" subcategory
        for (Map.Entry<String, List<InternalRule>> entry : missingList.entrySet()) {
            for (InternalRule rule : entry.getValue()) {
                InternalRule newRule = new InternalRule(
                        rule.getCheckerName() + "_none",
                        rule.getRuleName(),
                        rule.getCheckerName(),
                        rule.getSeverity(),
                        "none",
                        rule.getDescription(),
                        BUG,
                        entry.getKey());
                addLanguageTag(newRule);
                addRuleTypeTag(newRule, true, false);
                rulesList.get(entry.getKey()).get(newRule.getCheckerName()).add(newRule);
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
                BUG,
                language
            );

            List<InternalRule> list = new ArrayList<InternalRule>();
            addLanguageTag(newRule);
            addRuleTypeTag(newRule, true, false);
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
                BUG,
                CS_LANGUAGE
        );
        addAdditionalTag(misraRule, "misra");
        rules.add(misraRule);

        InternalRule pwRule = new InternalRule(
                "PW.*",
                "Coverity PW : Parse Warnings",
                "PW.*",
                "MAJOR",
                "none",
                "Coverity PW : Parse Warnings",
                BUG,
                CPP_LANGUAGE
        );
        addAdditionalTag(pwRule, "parse-warning");
        rules.add(pwRule);

        InternalRule swRule = new InternalRule(
                "SW.*",
                "Coverity SW : Semantic Warnings",
                "SW.*",
                "MAJOR",
                "none",
                "Coverity SW : Semantic Warnings",
                BUG,
                CPP_LANGUAGE
        );
        addAdditionalTag(swRule, "semantic-warning");
        rules.add(swRule);

        InternalRule rwRule = new InternalRule(
                "RW.*",
                "Coverity RW : Recovery Warnings",
                "RW.*",
                "MAJOR",
                "none",
                "Coverity RW : Recovery Warnings",
                BUG,
                CPP_LANGUAGE
        );
        addAdditionalTag(rwRule, "recovery-warning");
        rules.add(rwRule);

        InternalRule msvscaRule = new InternalRule(
                "MSVSCA.*",
                "Coverity MSVSCA : Microsoft Visual Studio Code Analysis",
                "MSVSCA.*",
                "MAJOR",
                "none",
                "Coverity MSVSCA : Microsoft Visual Studio Code Analysis",
                BUG,
                CPP_LANGUAGE
        );
        addAdditionalTag(msvscaRule, "msvsca");
        rules.add(msvscaRule);

        for (InternalRule rule : rules) {
            List<InternalRule> tempList = new ArrayList<InternalRule>();
            addLanguageTag(rule);
            addRuleTypeTag(rule, true, false);
            tempList.add(rule);
            if (rule.getKey().equals("MSVSCA.*")) {
                rulesList.get(CS_LANGUAGE).put(rule.getCheckerName(), tempList);
            } else {
                rulesList.get(CPP_LANGUAGE).put(rule.getCheckerName(), tempList);
            }
        }
    }

    public static void setOutputFilePath(String filePath) {
        outputFilePath = filePath;
    }

    public static void addAdditionalTag(InternalRule rule, String tag) {
        if (rule != null && !StringUtils.isEmpty(tag)) {
            rule.getTags().add(tag);
        }
    }

    public static void addLanguageTag(InternalRule rule) {
        if (rule.getLanguage().equals(JAVA_LANGUAGE)) {
            rule.getTags().add("java");
        } else if (rule.getLanguage().equals(CPP_LANGUAGE)) {
            rule.getTags().add("c++");
            rule.getTags().add("c");
        } else if (rule.getLanguage().equals(CS_LANGUAGE)) {
            rule.getTags().add("c#");
        } else if (rule.getLanguage().equals(JAVASCRIPT_LANGUAGE)) {
            rule.getTags().add("js");
        } else if (rule.getLanguage().equals(PYTHON_LANGUAGE)) {
            rule.getTags().add("python");
        } else if (rule.getLanguage().equals(PHP_LANGUAGE)) {
            rule.getTags().add("php");
        } else if (rule.getLanguage().equals(OBJECTIVE_C)) {
            rule.getTags().add("objective-c");
        }
    }

    public static void addRuleTypeTag(InternalRule rule, boolean qualityKind, boolean securityKind) {
        if (qualityKind) {
            rule.getTags().add("quality");
        }

        if (securityKind) {
            rule.getTags().add("security");
        }
    }
}
