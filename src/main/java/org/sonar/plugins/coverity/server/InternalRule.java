package org.sonar.plugins.coverity.server;

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
    private String ruleName = "";
    private String severity = "";
    private List<String> tags;
    private String subcategory;
    private String description;
    private String ruleType = "";
    private String checkerName = "";

    public InternalRule(String key, String ruleName, String checkerName, String severity, String subcategory, String description, String ruleType){
        this.key = key;
        this.ruleName = ruleName;
        this.severity = severity;
        this.subcategory = subcategory;
        this.description = description;
        this.checkerName = checkerName;
        this.ruleType = ruleType;
        this.tags = new ArrayList<>();
    }

    public String getKey() {
        return key;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getCheckerName() {
        return checkerName;
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
