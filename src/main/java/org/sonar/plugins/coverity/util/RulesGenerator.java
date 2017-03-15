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

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sonar.plugins.coverity.server.InternalRule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class RulesGenerator {

    static Map<String, Map<String, InternalRule>> rulesList = new HashMap<String, Map<String, InternalRule>>();
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

        int javaRule = 0;
        int cppRule = 0;
        int csRule = 0;

        for (String key: rulesList.get(JAVA_LANGUAGE).keySet()) {
            javaRule += rulesList.get(JAVA_LANGUAGE).get(key).getSubcategory().size();
        }

        for (String key: rulesList.get(CPP_LANGUAGE).keySet()) {
            cppRule += rulesList.get(CPP_LANGUAGE).get(key).getSubcategory().size();
        }

        for (String key: rulesList.get(CS_LANGUAGE).keySet()) {
            csRule += rulesList.get(CS_LANGUAGE).get(key).getSubcategory().size();
        }

        System.out.println("Total Java Rules: " + javaRule);
        System.out.println("Total C/C++ Rules: " + cppRule);
        System.out.println("Total C# Rules: " + csRule);
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
                    InternalRule rule = new InternalRule(checkerName, name, getSeverity(impact), getDescription(subcategoryLongDescription));
                    setRuleType(rule, qualityKind, securityKind);
                    putRuleIntoMap(lang, rule, subcategory);

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
            System.out.println("File Path : " + jsonFile.getAbsolutePath() + " JSONArray Size: " + issues.size());

            Iterator<JSONObject> iterator = issues.iterator();

            while( iterator.hasNext() ) {
                JSONObject childJSON = (JSONObject) iterator.next();

                String checkerName = (String) childJSON.get("type");
                String subcategory = (String) childJSON.get("subtype");

                JSONObject desc = (JSONObject) childJSON.get("description");
                String description = (String) desc.get("en");

                JSONObject properties = (JSONObject) childJSON.get("cim_checker_properties");
                String impact = (String) properties.get("impact");
                boolean qualityKind = (boolean) properties.get("qualityKind");
                boolean securityKind = (boolean) properties.get("securityKind");

                InternalRule rule = new InternalRule(checkerName, checkerName, getSeverity(impact), getDescription(description));
                setRuleType(rule, qualityKind, securityKind);

                putRuleIntoMap(JAVA_LANGUAGE, rule, subcategory);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static String findLanguage(String lang) {
        if (lang.equals("Java") || lang.equals("STATIC_JAVA") || lang.equals("DYNAMIC_JAVA")) {
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

    public static void putRuleIntoMap(String language, InternalRule rule, String subcategory) {
        String key = rule.getKey();

        if (!rulesList.containsKey(language)) {
            rulesList.put(language, new HashMap<String, InternalRule>());
        }

        if (!rulesList.get(language).containsKey(key)) {
            rule.getSubcategory().add(subcategory);

            rulesList.get(language).put(key, rule);
        } else {
            if (!rulesList.get(language).get(key).getSubcategory().contains(subcategory)) {
                rulesList.get(language).get(key).getSubcategory().add(subcategory);
            }
        }
    }

    public static void setRuleType(InternalRule rule, boolean qualityKind, boolean securityKind) {
        if (!qualityKind & securityKind) {
            rule.setRuleType(VULNERABILITY);
        } else {
            rule.setRuleType(BUG);
        }
    }

}
