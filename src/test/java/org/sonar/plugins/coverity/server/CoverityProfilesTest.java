///*
// * Coverity Sonar Plugin
// * Copyright (c) 2019 Synopsys, Inc
// * support@coverity.com
// *
// * All rights reserved. This program and the accompanying materials are made
// * available under the terms of the Eclipse Public License v1.0 which
// * accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html.
// */
//package org.sonar.plugins.coverity.server;
//
//import edu.emory.mathcs.backport.java.util.Arrays;
//import org.junit.Test;
//import org.sonar.api.profiles.RulesProfile;
//import org.sonar.api.rules.ActiveRule;
//import org.sonar.api.rules.Rule;
//import org.sonar.api.rules.RuleFinder;
//import org.sonar.api.utils.ValidationMessages;
//import org.sonar.plugins.coverity.CoverityPlugin;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static org.junit.Assert.*;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//public class CoverityProfilesTest {
//
//    @Test
//    public void provideReturnsProfileDefinitionsForLanguages() {
//        RuleFinder ruleFinder = mock(RuleFinder.class);
//
//        // setup a single rule for each language to test
//        for (String coverityLanguage: CoverityPlugin.COVERITY_LANGUAGES) {
//            when(ruleFinder.findAll(any()))
//                    .thenReturn(Arrays.asList(new Rule[]{new Rule(CoverityPlugin.class.getName(), "COV_TEST-" + coverityLanguage + "-rule")}));
//        }
//
//        CoverityProfiles profiles = new CoverityProfiles(ruleFinder);
//
//        List<CoverityProfiles.CoverityProfile> profileList = profiles.provide();
//
//        assertNotNull(profileList);
//        assertEquals(CoverityPlugin.COVERITY_LANGUAGES.size(), profileList.size());
//
//        List<String> expectedProfileNames =
//                CoverityPlugin.COVERITY_LANGUAGES.stream().map(l->String.format("Coverity(%s)", l)).collect(Collectors.toList());
//        List<String> profileNames =
//            profileList.stream().map(p->p.toString()).collect(Collectors.toList());
//
//        assertArrayEquals(expectedProfileNames.toArray(), profileNames.toArray());
//
//        for (CoverityProfiles.CoverityProfile profile : profileList) {
//            // verify when profile created there is one active rule
//            final RulesProfile rulesProfile = profile.createProfile(ValidationMessages.create());
//            assertTrue(rulesProfile.getName().startsWith("Coverity("));
//            assertEquals(1, rulesProfile.getActiveRules().size());
//            final ActiveRule activeRule = rulesProfile.getActiveRules().get(0);
//            assertTrue(activeRule.getRuleKey().startsWith("COV_TEST-"));
//            assertTrue(activeRule.getRuleKey().endsWith("-rule"));
//        }
//    }
//}
