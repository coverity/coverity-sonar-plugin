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

package org.sonar.plugins.coverity.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.Extension;
import org.sonar.api.ExtensionProvider;
import org.sonar.api.ServerExtension;
import org.sonar.api.batch.rule.Rules;
import org.sonar.api.config.Settings;
import org.sonar.api.rules.Rule;
//import org.sonar.api.rules.RuleRepository;
//import org.sonar.api.rules.XMLRuleParser;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.plugins.coverity.CoverityPlugin;
import org.sonar.plugins.coverity.util.FileGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* From Sonarqube-4.3+ the interface RulesDefinition replaces the (previously deprecated and currently dropped) RulesRepository.
 * This class loads rules into the server by means of an XmlLoader. However we still need to activate these rules under
 * a profile and then again in CoveritySensor.
 */

public class CoverityRules implements RulesDefinition, Extension {

    private RulesDefinitionXmlLoader xmlLoader = new RulesDefinitionXmlLoader();
    private static final Logger LOG = LoggerFactory.getLogger(CoverityRules.class);

    public CoverityRules(RulesDefinitionXmlLoader xmlLoader) {
        this.xmlLoader = xmlLoader;
    }

    Map<String, NodeList> mapOfNodeLists = new HashMap<String, NodeList>();

    NodeList javaNodes;
    NodeList cppNodes;
    NodeList csNodes;

    static List<String> languages = new ArrayList<String>();
    static{
        languages.add("java");
        languages.add("cpp");
        languages.add("cs");
    }

    public static List<InternalRule> javaRulesToBeActivated = new ArrayList<InternalRule>();
    public static List<InternalRule> cppRulesToBeActivated = new ArrayList<InternalRule>();
    public static List<InternalRule> cRulesToBeActivated = new ArrayList<InternalRule>();
    public static List<InternalRule> cppComunityRulesToBeActivated = new ArrayList<InternalRule>();
    public static List<InternalRule> csRulesToBeActivated = new ArrayList<InternalRule>();

    static Map<String, List> mapOfRuleLists = new HashMap<String, List>();

    static {
        mapOfRuleLists.put("java",javaRulesToBeActivated);
        mapOfRuleLists.put("cpp",cppRulesToBeActivated);
        mapOfRuleLists.put("c++",cppComunityRulesToBeActivated);
        mapOfRuleLists.put("c",cRulesToBeActivated);
        mapOfRuleLists.put("cs",csRulesToBeActivated);
    }

    /* The interface RulesDefinition provides a default parser: "XmlLoader". However, XmlLoader stores rules as
    *  "NewRules" a class that does not provides getters for certain fields such as severity. We need to access these
    *  fields later on when activating rules in CoverityProfiles. So in order to have more control over our rules we
    *  define "InternalRule.class" and we complete its fields by doing a parsing by ourselves. This is the propose of
    *  "parseRules()".
    * */
    public void parseRules(){

        for(String language : languages){

            String fileDir = "coverity-" + language + ".xml";
            InputStream in = getClass().getResourceAsStream(fileDir);

            LOG.info("This is the absolute path for rules: " + in.toString());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = null;
            Document doc = null;
            try {
                dBuilder = dbFactory.newDocumentBuilder();
                doc = dBuilder.parse(in);
            } catch (ParserConfigurationException e) {
                LOG.error("Error parsing rules." + e.getCause());
            }
             catch (SAXException e) {
                 LOG.error("Error parsing rules." + e.getCause());
             } catch (IOException e) {
                LOG.error("Error parsing rules." + e.getCause());
            }
            doc.getDocumentElement().normalize();

            NodeList nodes = doc.getElementsByTagName("rule");

            if(language.equals("java")){
                javaNodes = nodes;
                mapOfNodeLists.put("java", javaNodes);
            } else if (language.equals("cpp")){
                cppNodes = nodes;
                mapOfNodeLists.put("cpp", cppNodes);
            } else if (language.equals("cs")){
                csNodes = nodes;
                mapOfNodeLists.put("cs", csNodes);
            }

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                String key = "";
                String name = "";
                String severity = "";
                String description = "";

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    key = getValue("key", element);
                    name = getValue("name", element);
                    severity = getValue("severity", element);
                    description = getValue("description", element);
                }

                InternalRule internalRule = new InternalRule(key, name, severity, description, language);

                mapOfRuleLists.get(language).add(internalRule);
                if(language.equals("cpp")){
                    mapOfRuleLists.get("c++").add(internalRule);
                    mapOfRuleLists.get("c").add(internalRule);
                }
            }
        }
    }

    private static String getValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodes.item(0);
        return node.getNodeValue();
    }

    @Override
    public void define(Context context) {
        parseRules();

        /* These extra repositories are added in order to support the community version of c++ plugin and the licensed
        *  version (called cpp). Also we create a "c profile", although rules for c, cpp and c++ are the same.
        */
        List<String> otherLanguages = new ArrayList<String>();
        otherLanguages.add("c++");
        otherLanguages.add("c");

        for(String language : otherLanguages){
            NewRepository repository = context.createRepository(CoverityPlugin.REPOSITORY_KEY + "-" + language, language).setName("coverity-" + language);
            String fileDir = "coverity-cpp.xml";
            InputStream in = getClass().getResourceAsStream(fileDir);
            xmlLoader.load(repository, in, "UTF-8");
            repository.done();
        }

        for(String language : languages){
            NewRepository repository = context.createRepository(CoverityPlugin.REPOSITORY_KEY + "-" + language, language).setName("coverity-" + language);
            String fileDir = "coverity-" + language + ".xml";
            InputStream in = getClass().getResourceAsStream(fileDir);
            xmlLoader.load(repository, in, "UTF-8");
            repository.done();
        }

    }

    class InternalRule{
        String key;
        String name;
        String severity;
        String description;
        String language;

        InternalRule(String key, String name, String severity, String description, String language){
            this.key = key;
            this.name = name;
            this.severity = severity;
            this.description = description;
            this.language = language;
        }

    }
}


