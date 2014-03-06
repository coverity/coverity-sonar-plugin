/*
 * Coverity Sonar Plugin
 * Copyright (C) 2014 Coverity, Inc.
 * support@coverity.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
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
    public void getMapOfCheckerPropertyDataObj() throws Exception {

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
