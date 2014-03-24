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

package org.sonar.plugins.coverity.ws;

import com.coverity.ws.v6.CheckerPropertyDataObj;
import com.coverity.ws.v6.CheckerPropertyFilterSpecDataObj;
import com.coverity.ws.v6.CheckerSubcategoryIdDataObj;
import com.coverity.ws.v6.ConfigurationService;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CIMClientTest {

    @Test
    public void testGetMapOfCheckerPropertyDataObj() throws Exception {

        CIMClient cimClient = mock(CIMClient.class);
        CheckerPropertyFilterSpecDataObj checkerPropertyFilterSpecDataObj = mock(CheckerPropertyFilterSpecDataObj.class);
        ConfigurationService configurationService = mock(ConfigurationService.class);

        String checkerName = "Checker Name Test";
        String subcategory = "Checker Subcategory Test";
        String domain = "Checker Domain";

        CheckerSubcategoryIdDataObj checkerSubcategoryIdDataObj = new CheckerSubcategoryIdDataObj();
        CheckerPropertyDataObj checkerPropertyDataObjTest = new CheckerPropertyDataObj();

        checkerSubcategoryIdDataObj.setCheckerName(checkerName);
        checkerSubcategoryIdDataObj.setSubcategory(subcategory);
        checkerSubcategoryIdDataObj.setDomain(domain);
        checkerPropertyDataObjTest.setCheckerSubcategoryId(checkerSubcategoryIdDataObj);

        TripleFromDefects key = new TripleFromDefects(checkerName, subcategory, domain );

        List<CheckerPropertyDataObj> testList = new ArrayList<CheckerPropertyDataObj>();
        testList.add(checkerPropertyDataObjTest);

        when(cimClient.getConfigurationService()).thenReturn(configurationService);
        when(configurationService.getCheckerProperties(checkerPropertyFilterSpecDataObj)).thenReturn(testList);

        List<CheckerPropertyDataObj> checkerSubcategoryList = cimClient.getConfigurationService()
                .getCheckerProperties(checkerPropertyFilterSpecDataObj);

        Map<TripleFromDefects, CheckerPropertyDataObj> mapOfCheckerPropertyDataObj = new HashMap<TripleFromDefects,
                        CheckerPropertyDataObj>();

        for (CheckerPropertyDataObj checkerPropertyDataObj : checkerSubcategoryList) {
                TripleFromDefects keyInMapOfCheckerPropertyDataObj = new TripleFromDefects(
                        checkerPropertyDataObj.getCheckerSubcategoryId().getCheckerName(),
                        checkerPropertyDataObj.getCheckerSubcategoryId().getSubcategory(),
                        checkerPropertyDataObj.getCheckerSubcategoryId().getDomain()
                        );

                mapOfCheckerPropertyDataObj.put(keyInMapOfCheckerPropertyDataObj, checkerPropertyDataObj);
        }

        assertEquals(checkerName, mapOfCheckerPropertyDataObj.get(key).getCheckerSubcategoryId().getCheckerName());
        assertEquals(subcategory ,mapOfCheckerPropertyDataObj.get(key).getCheckerSubcategoryId().getSubcategory());
        assertEquals(domain ,mapOfCheckerPropertyDataObj.get(key).getCheckerSubcategoryId().getDomain());

    }
}
