/*
 * Coverity Sonar Plugin
 * Copyright (c) 2020 Synopsys, Inc
 * support@coverity.com
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonar.plugins.coverity.server;

import edu.emory.mathcs.backport.java.util.Collections;
import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.plugins.coverity.CoverityPlugin;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class CoverityRulesTest {

    @Test
    public void defineAddsRepositoryForLanguages() {
        final RulesDefinition.Context context = new RulesDefinition.Context();
        final RulesDefinitionXmlLoader xmlLoader = new RulesDefinitionXmlLoader();

        CoverityRules rules = new CoverityRules(xmlLoader);

        rules.define(context);

        final List<RulesDefinition.Repository> repositoryList = context.repositories();

        assertNotNull(repositoryList);
        assertEquals(CoverityPlugin.COVERITY_LANGUAGES.size(), repositoryList.size());

        List<String> expectedRepositoryKeys =
                CoverityPlugin.COVERITY_LANGUAGES.stream().map(l->CoverityPlugin.REPOSITORY_KEY + "-" + l).collect(Collectors.toList());
        List<String> repositoryKeys =
                repositoryList.stream().map(r->r.key()).collect(Collectors.toList());
        Collections.sort(expectedRepositoryKeys);
        Collections.sort(repositoryKeys);
        assertArrayEquals(expectedRepositoryKeys.toArray(), repositoryKeys.toArray());

        List<String> expectedRepositoryNames =
                CoverityPlugin.COVERITY_LANGUAGES.stream().map(l-> "coverity-" + l).collect(Collectors.toList());
        List<String> repositoryNames =
                repositoryList.stream().map(r->r.name()).collect(Collectors.toList());
        Collections.sort(expectedRepositoryNames);
        Collections.sort(repositoryNames);
        assertArrayEquals(expectedRepositoryNames.toArray(), repositoryNames.toArray());

    }
}
