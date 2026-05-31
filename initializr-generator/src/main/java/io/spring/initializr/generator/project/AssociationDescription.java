package io.spring.initializr.generator.project;

public class AssociationDescription {
    private String firstClassName;
    private String secondClassName;
    private AssociationType associationType;

    public AssociationDescription(){}

    public AssociationDescription(String firstClassName, String secondClassName, AssociationType associationType) {
        this.firstClassName = firstClassName;
        this.secondClassName = secondClassName;
        this.associationType = associationType;
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

    public AssociationType getAssotiationType() {
        return associationType;
    }

    public void setAssotiationType(AssociationType associationType) {
        this.associationType = associationType;
    }
}
