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

public class CheckerGenerator {

    public static Map<String, Map<String, InternalRule>> rulesList = new HashMap<String, Map<String, InternalRule>>();

    /*
    CheckerGenerator is used to generate rules based on the coverity quality checker-properties.json files
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
//                generateRulesForFindBugCheckers(file);
            } else {
                generateRulesForQualityCheckers(file);
            }
        }

        int javaRule = 0;
        int cppRule = 0;
        int csRule = 0;

        for (String key: rulesList.get("java").keySet()) {
            javaRule += rulesList.get("java").get(key).getSubcategory().size();
        }

        for (String key: rulesList.get("cpp").keySet()) {
            cppRule += rulesList.get("cpp").get(key).getSubcategory().size();
        }

        for (String key: rulesList.get("cs").keySet()) {
            csRule += rulesList.get("cs").get(key).getSubcategory().size();
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

                String key = null;
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

                    if (!rulesList.containsKey(lang)) {
                        rulesList.put(lang, new HashMap<String, InternalRule>());
                    }

                    if (!rulesList.get(lang).containsKey(checkerName)) {
                        String severity = "MAJOR";
                        if(impact.equals("High")){
                            severity = "BLOCKER";
                        }
                        if(impact.equals("Medium")){
                            severity = "CRITICAL";
                        }

                        String linkRegex = "\\(<a href=\"([^\"]*?)\" target=\"_blank\">(.*?)</a>\\)";
                        String codeRegex = "<code>(.*?)</code>";

                        subcategoryLongDescription = subcategoryLongDescription.replaceAll(linkRegex, "");
                        subcategoryLongDescription = subcategoryLongDescription.replaceAll(codeRegex, "$1");
                        subcategoryLongDescription = subcategoryLongDescription.trim();

                        InternalRule rule = new InternalRule(checkerName, name, severity, subcategoryLongDescription);
                        rule.getSubcategory().add(subcategory);

                        rulesList.get(lang).put(checkerName, rule);
                    } else {
                        if (!rulesList.get(lang).get(checkerName).getSubcategory().contains(subcategory)) {
                            rulesList.get(lang).get(checkerName).getSubcategory().add(subcategory);
                        }
                    }

                    if (lang.equals("java")) {
                        javaRule++;
                    } else if (lang.equals("cpp")) {
                        cppRule++;
                    } else if (lang.equals("cs")) {
                        csRule++;
                    }

                }


                /**
                 * Checkers specify the languages they support by providing a "domain" field or by providing a list of
                 * langueges under the field "families".
                 * First we decide what are the languages corresponding for each checker. If this can not be determined,
                 * an error is printed to the console.
                 */
//                if(domain == null && families == null){
//                    System.out.println("Language is not defined for checker: " + checkerName);
//                }
//
//                List<String> languages = new ArrayList<String>();
//
//                if((domain != null && domain.equals("STATIC_JAVA")) || (families != null && families.contains("Java"))){
//                    languages.add("java");
//                }
//
//                if((domain != null && domain.equals("STATIC_C")) || (families != null && (families.contains("C/C++") ||
//                        families.contains("Objective-C/C++")))){
//                    languages.add("cpp");
//                }
//
//                if((domain != null && domain.equals("STATIC_CS")) || (families != null && families.contains("C#"))){
//                    languages.add("cs");
//                }

                /**
                 * Rules are created for each language and store on a hashmap. There is one map per language.
                 */
//                for(String language : languages){
//                    String key = languageDomains.get(language) + "_" + checkerName ;
//                    InternalRule ir = new InternalRule();
//                    ir.setKey(key);
//                    ir.setName(checkerName);
//                    if(subcategoryShortDescription.isEmpty() || subcategoryShortDescription == null){
//                        ir.setName(key);
//                    } else {
//                        subcategoryShortDescription = org.apache.commons.lang.StringEscapeUtils.escapeXml(subcategoryShortDescription);
//                        ir.setName(subcategoryShortDescription);
//                    }
//                    String severity = "MAJOR";
//                    if(impact.equals("High")){
//                        severity = "BLOCKER";
//                    }
//                    if(impact.equals("Medium")){
//                        severity = "CRITICAL";
//                    }
//                    ir.setSeverity(severity);
//                    ir.setDescription(subcategoryLongDescription);
//                    mapOfCheckerPropMaps.get(language).put(key, ir);
//                }
            }

            /**
             * Print out to the console the results of the rules generation so that developers can analyze these results.
             */
//            System.out.println("Number of rules loaded from JSON for java: " + javaCheckerProp.size());
//            System.out.println("Number of rules loaded from JSON for cs: " + csCheckerProp.size());
//            System.out.println("Number of rules loaded from JSON for cpp: " + cppCheckerProp.size());
//            System.out.println("Total number of checkers on JSON: " + iteratorCount);

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
        List<String> checkerList = new ArrayList<>();
        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(jsonFile.getAbsolutePath()));
            JSONObject root = (JSONObject) obj;
            JSONArray issues =  (JSONArray) root.get("issue_type");
            System.out.println("File Path : " + jsonFile.getAbsolutePath() + " JSONArray Size: " + issues.size());

            Iterator<JSONObject> iterator = issues.iterator();

            // Count of all checkers found on the given JSON file.
//            int iteratorCount = 0;

            while( iterator.hasNext() ) {
                JSONObject childJSON = (JSONObject)iterator.next();

                String checkerName = (String) childJSON.get("type");
                String subcategory = (String) childJSON.get("subtype");
                String key = null;

                if (!StringUtils.isEmpty(checkerName) && !StringUtils.isEmpty(subcategory)) {
                    key = checkerName + "_" + subcategory;
                }

                if (StringUtils.isEmpty(key)) {
                    System.out.println("Key cannot be null. Aborting.....");
                    return;
                }

                checkerList.add(key);

//                {
//                    String linkRegex = "\\(<a href=\"([^\"]*?)\" target=\"_blank\">(.*?)</a>\\)";
//                    String codeRegex = "<code>(.*?)</code>";
//
//                    subcategoryLongDescription = subcategoryLongDescription.replaceAll(linkRegex, "");
//                    subcategoryLongDescription = subcategoryLongDescription.replaceAll(codeRegex, "$1");
//                    subcategoryLongDescription = subcategoryLongDescription.trim();
//                }

                /**
                 * Checkers specify the languages they support by providing a "domain" field or by providing a list of
                 * langueges under the field "families".
                 * First we decide what are the languages corresponding for each checker. If this can not be determined,
                 * an error is printed to the console.
                 */
//                if(domain == null && families == null){
//                    System.out.println("Language is not defined for checker: " + checkerName);
//                }
//
//                List<String> languages = new ArrayList<String>();
//
//                if((domain != null && domain.equals("STATIC_JAVA")) || (families != null && families.contains("Java"))){
//                    languages.add("java");
//                }
//
//                if((domain != null && domain.equals("STATIC_C")) || (families != null && (families.contains("C/C++") ||
//                        families.contains("Objective-C/C++")))){
//                    languages.add("cpp");
//                }
//
//                if((domain != null && domain.equals("STATIC_CS")) || (families != null && families.contains("C#"))){
//                    languages.add("cs");
//                }

                /**
                 * Rules are created for each language and store on a hashmap. There is one map per language.
                 */
//                for(String language : languages){
//                    String key = languageDomains.get(language) + "_" + checkerName ;
//                    InternalRule ir = new InternalRule();
//                    ir.setKey(key);
//                    ir.setName(checkerName);
//                    if(subcategoryShortDescription.isEmpty() || subcategoryShortDescription == null){
//                        ir.setName(key);
//                    } else {
//                        subcategoryShortDescription = org.apache.commons.lang.StringEscapeUtils.escapeXml(subcategoryShortDescription);
//                        ir.setName(subcategoryShortDescription);
//                    }
//                    String severity = "MAJOR";
//                    if(impact.equals("High")){
//                        severity = "BLOCKER";
//                    }
//                    if(impact.equals("Medium")){
//                        severity = "CRITICAL";
//                    }
//                    ir.setSeverity(severity);
//                    ir.setDescription(subcategoryLongDescription);
//                    mapOfCheckerPropMaps.get(language).put(key, ir);
//                }
            }

            /**
             * Print out to the console the results of the rules generation so that developers can analyze these results.
             */
//            System.out.println("Number of rules loaded from JSON for java: " + javaCheckerProp.size());
//            System.out.println("Number of rules loaded from JSON for cs: " + csCheckerProp.size());
//            System.out.println("Number of rules loaded from JSON for cpp: " + cppCheckerProp.size());
//            System.out.println("Total number of checkers on JSON: " + iteratorCount);
            System.out.println("Total number of parsed checkers : " + checkerList.size());

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
            return "java";
        } else if (lang.equals("C/C++")){
            return "cpp";
        } else if (lang.equals("C#")) {
            return "cs";
        }

        return StringUtils.EMPTY;
    }

}
