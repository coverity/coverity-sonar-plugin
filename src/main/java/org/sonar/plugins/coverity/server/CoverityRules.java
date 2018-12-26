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

package org.sonar.plugins.coverity.server;

import org.sonar.api.ExtensionPoint;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.plugins.coverity.CoverityPlugin;
import java.io.*;

import static org.sonar.plugins.coverity.util.CoverityUtil.getValue;

/* From Sonarqube-4.3+ the interface RulesDefinition replaces the (previously deprecated and currently dropped) RulesRepository.
 * This class loads rules into the server by means of an XmlLoader. However we still need to activate these rules under
 * a profile and then again in CoveritySensor.
 */
@ServerSide
@ExtensionPoint
public class CoverityRules implements RulesDefinition {

    private RulesDefinitionXmlLoader xmlLoader = new RulesDefinitionXmlLoader();

    public CoverityRules(RulesDefinitionXmlLoader xmlLoader) {
        this.xmlLoader = xmlLoader;
    }

    @Override
    public void define(Context context) {
        for(String language : CoverityPlugin.COVERITY_LANGUAGES){
            NewRepository repository = context.createRepository(CoverityPlugin.REPOSITORY_KEY + "-" + language, language).setName("coverity-" + language);
            String fileDir = "coverity-" + language + ".xml";
            InputStream in = getClass().getResourceAsStream(fileDir);
            xmlLoader.load(repository, in, "UTF-8");
            repository.done();
        }

    }
}


