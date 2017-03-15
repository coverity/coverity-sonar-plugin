package org.sonar.plugins.coverity.server;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<String> tags;
    private String subcategory;
    private String description;
    private String ruleType = "";

    public InternalRule(String key, String name, String severity, String subcategory, String description){
        this.key = key;
        this.name = name;
        this.severity = severity;
        this.subcategory = subcategory;
        this.description = description;
        this.tags = new ArrayList<>();
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

    public String getSubcategory() {
        return subcategory;
    }

    public String getDescription() {
        return description;
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

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.key.hashCode();
        return hash;
    }

}
