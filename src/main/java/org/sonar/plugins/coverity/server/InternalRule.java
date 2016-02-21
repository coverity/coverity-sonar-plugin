package org.sonar.plugins.coverity.server;

/**
 * InternalRule.class is used to produce objects containing all the information necessary to define a Sonarqube rule.
 * This informaition will be printed out to an XML file. When reading the resulting file, the DocumentBuilder parser has
 * trouble reading the "&" symbol. Because of these fact, all "&" must be repalced by "&amp". This is done by the
 * setters of this class.
 */
public class InternalRule{
    String key;
    String name;
    String severity;
    String description;
    String language;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        key = key.replaceAll("&", "&amp;");
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        name = name.replaceAll("&", "&amp;");
        this.name = name;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        severity = severity.replaceAll("&", "&amp;");
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        description = description.replaceAll("&", "&amp;");
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        language = language.replaceAll("&", "&amp;");
        this.language = language;
    }

    public InternalRule(){};

    public InternalRule(String key, String name, String severity, String description, String language){
        this.key = key;
        this.name = name;
        this.severity = severity;
        this.description = description;
        this.language = language;
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
