package io.spring.initializr.generator.spring.code;

import java.util.List;

public class DomainClassDescription {

    private String className;
    private List<FieldDescription> fields;

    public DomainClassDescription(String className, List<FieldDescription> fields) {
        this.className = className;
        this.fields = fields;
    }

    public List<FieldDescription> getFields() {
        return fields;
    }

    public void setFields(List<FieldDescription> fields) {
        this.fields = fields;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

}
