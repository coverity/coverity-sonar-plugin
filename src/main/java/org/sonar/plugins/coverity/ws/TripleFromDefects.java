/*
 * Coverity Plugin
 * Copyright (C) 2013 Coverity, Inc.
 * support@coverity.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02
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
