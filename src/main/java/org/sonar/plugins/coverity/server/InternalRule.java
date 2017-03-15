package org.sonar.plugins.coverity.server;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * InternalRule.class is used to produce objects containing all the information necessary to define a Sonarqube rule.
 * This informaition will be printed out to an XML file. When reading the resulting file, the DocumentBuilder parser has
 * trouble reading the "&" symbol. Because of these fact, all "&" must be repalced by "&amp". This is done by the
 * setters of this class.
 */
public class InternalRule{
    private String key = "";
    private String name = "";
    private String severity = "";
    private String description = "";
    private List<String> tags;
    private List<String> subcategory;
    private String ruleType = "";

    public InternalRule(String key, String name, String severity, String description){
        this.key = key;
        this.name = name;
        this.severity = severity;
        this.description = description;
        this.tags = new ArrayList<>();
        this.subcategory = new ArrayList<>();
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getSubcategory() {
        return subcategory;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setRuleType(String type) {
        this.ruleType = type;
    }

    public String getRuleType() {
        return this.ruleType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!InternalRule.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final InternalRule other = (InternalRule) obj;
        if (!this.key.equals(other.key)) {
            return false;
        }
        if (!CollectionUtils.isEqualCollection(this.subcategory, other.subcategory)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.key.hashCode();
        return hash;
    }

}
