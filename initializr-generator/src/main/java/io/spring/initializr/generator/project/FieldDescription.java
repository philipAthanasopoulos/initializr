package io.spring.initializr.generator.project;

public class FieldDescription {
    private String fieldName;
    private String classType;

    public FieldDescription(){}

    public FieldDescription(String fieldName, String classType) {
        this.fieldName = fieldName;
        this.classType = classType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }
}
