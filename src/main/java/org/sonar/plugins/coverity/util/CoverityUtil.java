/*
 * Coverity Sonar Plugin
 * Copyright (c) 2019 Synopsys, Inc
 * support@coverity.com
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonar.plugins.coverity.util;

import com.coverity.ws.v9.DefectInstanceDataObj;
import org.sonar.api.config.Settings;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.coverity.CoverityPlugin;
import org.sonar.plugins.coverity.ws.CIMClient;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoverityUtil {
    public static RuleKey getRuleKey(String language, String key) {
        return RuleKey.of(CoverityPlugin.REPOSITORY_KEY + "-" + language, key);
    }

    public static String getValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodes.item(0);
        return node.getNodeValue();
    }

    public static String flattenDefectInstanceCheckerName(DefectInstanceDataObj dido) {
        return dido.getDomain() + "_" + dido.getCheckerName();
    }

    public static String createURL(CIMClient client) {
        return createURL(client.getHost(), client.getPort(), client.isUseSSL());
    }

    public static String createURL(Settings settings) {
        String host = settings.getString(CoverityPlugin.COVERITY_CONNECT_HOSTNAME);
        int port = settings.getInt(CoverityPlugin.COVERITY_CONNECT_PORT);
        boolean ssl = settings.getBoolean(CoverityPlugin.COVERITY_CONNECT_SSL);

        return createURL(host, port, ssl);
    }

    public static String createURL(String host, int port, boolean ssl) {
        if(host == null || port == 0) {
            return null;
        }
        return String.format("http%s://%s:%d/", (ssl ? "s" : ""), host, port);
    }

    public static List<File> listFiles(File dir, List<File> listOfFiles){
        List<File> tmpList = Arrays.asList(dir.listFiles());
        for(File possibleFile : tmpList){
            if(possibleFile.isFile()){
                listOfFiles.add(possibleFile);
            } else if (possibleFile.isDirectory()){
                listFiles(possibleFile, listOfFiles);
            }
        }
        return listOfFiles;
    }

    public static List<File> listFiles(File dir){
        if(dir.isFile()){
            List<File> tmpList = new ArrayList<File>();
            tmpList.add(dir);
            return tmpList;
        } else {
            return listFiles(dir, new ArrayList<File>());
        }
    }
}
