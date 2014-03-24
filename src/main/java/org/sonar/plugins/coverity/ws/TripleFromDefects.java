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

public final class TripleFromDefects {
    private final String checkerName;
    private final String checkerSubcategory;
    private final String domain;

    public TripleFromDefects(String checkerName, String checkerSubcategory, String domain) {
        this.checkerName = checkerName;
        this.checkerSubcategory = checkerSubcategory;
        this.domain = domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TripleFromDefects that = (TripleFromDefects) o;

        if (checkerName != null ? !checkerName.equals(that.checkerName) : that.checkerName != null) return false;
        if (checkerSubcategory != null ? !checkerSubcategory.equals(that.checkerSubcategory) : that.checkerSubcategory != null)
            return false;
        if (domain != null ? !domain.equals(that.domain) : that.domain != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = checkerName != null ? checkerName.hashCode() : 0;
        result = 31 * result + (checkerSubcategory != null ? checkerSubcategory.hashCode() : 0);
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        return result;
    }
}
