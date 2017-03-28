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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

import static org.sonar.plugins.coverity.util.CoverityUtil.getValue;

public class RulesGeneratorTest {

    private static final String outputFilePath = "./test";
    private static final String qualityJsonFile = "src/test/java/org/sonar/plugins/coverity/util/quality-checker-properties.json";
    private static final String findbugsJsonFile = "src/test/java/org/sonar/plugins/coverity/util/findbugs-checker-properties.json";

    private static final String javaOutputFilePath = "./test/coverity-java.xml";
    private static final String cppOutputFilePath = "./test/coverity-cov-cpp.xml";
    private static final String csOutputFilePath = "./test/coverity-cs.xml";

    @Before
    public void setUp() {
        createTestDirectory();
        RulesGenerator.setOutputFilePath(outputFilePath);
    }

    @After
    public void tearDown() throws IOException {

        File testDir = new File(outputFilePath);
        if (testDir.exists()) {
            File[] files = testDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    Assert.assertTrue(file.delete());
                }
            }
            Assert.assertTrue(testDir.delete());
        }
    }

    @Test
    public void rulesGeneratorTest() throws Exception {
        File qualityCheckerFile = new File(qualityJsonFile);
        File findbugCheckerFile = new File(findbugsJsonFile);

        Assert.assertTrue(qualityCheckerFile.exists());
        Assert.assertTrue(findbugCheckerFile.exists());

        RulesGenerator.setOutputFilePath(outputFilePath);
        RulesGenerator.main(new String[] {qualityCheckerFile.getAbsolutePath(), findbugCheckerFile.getAbsolutePath()});

        File javaOutputFile = new File(javaOutputFilePath);
        File cppOutputFile = new File(cppOutputFilePath);
        File csOutputFile = new File(csOutputFilePath);

        Assert.assertTrue(javaOutputFile.exists());
        Assert.assertTrue(cppOutputFile.exists());
        Assert.assertTrue(csOutputFile.exists());

        checkCsOutputFile(csOutputFile);
        checkJavaOutputFile(javaOutputFile);
        checkCppOutputFile(cppOutputFile);
    }

    private void createTestDirectory() {
        File testDir = new File(outputFilePath);
        if (!testDir.exists()) {
            Assert.assertTrue(testDir.mkdir());
        }
    }

    private void checkCsOutputFile(File outputFile) throws IOException {
        NodeList nodes = parseNodeList(outputFile);
        Assert.assertNotNull(nodes);

        boolean testSubcategory = false;
        boolean noneSubcategory = false;
        boolean msvsca = false;
        boolean general = false;

        Assert.assertEquals(4, nodes.getLength());
        for (int i = 0 ; i < nodes.getLength() ; i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String key = getValue("key", element);

                // STATIC_CS_coverity-cs
                if (key.equals("STATIC_CS_coverity-cs")) {
                    Assert.assertEquals("Coverity General CS", getValue("name", element));
                    general = true;
                }

                // STATIC_CS_MSVSCA.*
                else if (key.equals("STATIC_CS_MSVSCA.*")) {
                    Assert.assertEquals("Coverity MSVSCA : Microsoft Visual Studio Code Analysis", getValue("name", element));
                    msvsca = true;
                }

                // STATIC_CS_C# Example Checker_none
                else if (key.equals("STATIC_CS_C# Example Checker_none")) {
                    Assert.assertEquals("C# Example Checker : Short Description", getValue("name", element));
                    noneSubcategory = true;
                }

                // STATIC_CS_C# Example Checker_test-subcategory
                else if (key.equals("STATIC_CS_C# Example Checker_test-subcategory")) {
                    Assert.assertEquals("C# Example Checker : Short Description", getValue("name", element));
                    testSubcategory = true;
                }

            }
        }

        Assert.assertTrue(general && msvsca && noneSubcategory && testSubcategory);
    }

    private void checkJavaOutputFile(File outputFile) throws IOException {
        NodeList nodes = parseNodeList(outputFile);
        Assert.assertNotNull(nodes);

        boolean noneSubcategory = false;
        boolean fbGeneric = false;
        boolean fbNone = false;
        boolean general = false;

        Assert.assertEquals(4, nodes.getLength());
        for (int i = 0 ; i < nodes.getLength() ; i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String key = getValue("key", element);

                // STATIC_JAVA_coverity-java
                if (key.equals("STATIC_JAVA_coverity-java")) {
                    Assert.assertEquals("Coverity General JAVA", getValue("name", element));
                    general = true;
                }

                // STATIC_JAVA_Java Example Checker_none
                else if (key.equals("STATIC_JAVA_Java Example Checker_none")) {
                    Assert.assertEquals("Java Example Checker : Short Description", getValue("name", element));
                    noneSubcategory = true;
                }

                // STATIC_JAVA_FB.AM_CREATES_EMPTY_JAR_FILE_ENTRY_generic
                else if (key.equals("STATIC_JAVA_FB.AM_CREATES_EMPTY_JAR_FILE_ENTRY_generic")) {
                    Assert.assertEquals("The code calls putNextEntry(), immediately followed by a call to closeEntry().", getValue("description", element));
                    fbGeneric = true;
                }

                // STATIC_JAVA_FB.AM_CREATES_EMPTY_JAR_FILE_ENTRY_none
                else if (key.equals("STATIC_JAVA_FB.AM_CREATES_EMPTY_JAR_FILE_ENTRY_none")) {
                    Assert.assertEquals("The code calls putNextEntry(), immediately followed by a call to closeEntry().", getValue("description", element));
                    fbNone = true;
                }
            }
        }

        Assert.assertTrue(general && fbGeneric && noneSubcategory && fbNone);
    }

    private void checkCppOutputFile(File outputFile) throws IOException {
        NodeList nodes = parseNodeList(outputFile);
        Assert.assertNotNull(nodes);

        boolean noneSubcategory = false;
        boolean general = false;
        boolean pwRule = false;
        boolean swRule = false;
        boolean rwRule = false;
        boolean misraRule = false;


        Assert.assertEquals(6, nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String key = getValue("key", element);

                // STATIC_C_coverity-cov-cpp
                if (key.equals("STATIC_C_coverity-cov-cpp")) {
                    Assert.assertEquals("Coverity General COV-CPP", getValue("name", element));
                    general = true;
                }

                // STATIC_C_CPP Example Checker_none
                else if (key.equals("STATIC_C_CPP Example Checker_none")) {
                    Assert.assertEquals("CPP Example Checker : Short Description", getValue("name", element));
                    Assert.assertEquals("VULNERABILITY", getValue("type", element));
                    noneSubcategory = true;
                }

                // STATIC_C_PW.*
                else if (key.equals("STATIC_C_PW.*")) {
                    Assert.assertEquals("Coverity PW : Parse Warnings", getValue("name", element));
                    pwRule = true;
                }

                // STATIC_C_SW.*
                else if (key.equals("STATIC_C_SW.*")) {
                    Assert.assertEquals("Coverity SW : Semantic Warnings", getValue("name", element));
                    swRule = true;
                }

                // STATIC_C_RW.*
                else if (key.equals("STATIC_C_RW.*")) {
                    Assert.assertEquals("Coverity RW : Recovery Warnings", getValue("name", element));
                    rwRule = true;
                }

                // STATIC_C_MISRA.*
                else if (key.equals("STATIC_C_MISRA.*")) {
                    Assert.assertEquals("Coverity MISRA : Coding Standard Violation", getValue("name", element));
                    misraRule = true;
                }
            }
        }

        Assert.assertTrue(general && noneSubcategory && pwRule && swRule && rwRule && misraRule);
    }

    private NodeList parseNodeList(File outputFile) throws IOException {
        InputStream in = new FileInputStream(outputFile);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        Document doc = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(in);
            doc.getDocumentElement().normalize();

            return doc.getElementsByTagName("rule");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        in.close();

        return null;
    }
}
