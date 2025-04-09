package io.spring.initializr.generator.project;

public class AssociationDescription {
    private String firstClassName;
    private String secondClassName;
    private AssotiationType assotiationType;

    public AssociationDescription(){}

    public AssociationDescription(String firstClassName, String secondClassName, AssotiationType assotiationType) {
        this.firstClassName = firstClassName;
        this.secondClassName = secondClassName;
        this.assotiationType = assotiationType;
    }

    public String getFirstClassName() {
        return firstClassName;
    }

    public void setFirstClassName(String firstClassName) {
        this.firstClassName = firstClassName;
    }

    public String getSecondClassName() {
        return secondClassName;
    }

    public void setSecondClassName(String secondClassName) {
        this.secondClassName = secondClassName;
    }

    public AssotiationType getAssotiationType() {
        return assotiationType;
    }

    public void setAssotiationType(AssotiationType assotiationType) {
        this.assotiationType = assotiationType;
    }
}
