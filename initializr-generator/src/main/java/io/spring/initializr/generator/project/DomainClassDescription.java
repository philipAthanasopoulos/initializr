package io.spring.initializr.generator.project;

import java.util.List;

public class DomainClassDescription {

    private String className;
    private List<FieldDescription> fields;
    private boolean generateRestController;
    private boolean generateFrontendController;
    private boolean useLombok;

    public DomainClassDescription(){}


    public DomainClassDescription(String className, List<FieldDescription> fields, boolean generateRestController, boolean generateFrontendController, boolean useLombok) {
        this.className = className;
        this.fields = fields;
        this.generateRestController = generateRestController;
        this.generateFrontendController = generateFrontendController;
        this.useLombok = useLombok;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<FieldDescription> getFields() {
        return fields;
    }

    public void setFields(List<FieldDescription> fields) {
        this.fields = fields;
    }

    public boolean isGenerateRestController() {
        return generateRestController;
    }

    public void setGenerateRestController(boolean generateRestController) {
        this.generateRestController = generateRestController;
    }

    public boolean isGenerateFrontendController() {
        return generateFrontendController;
    }

    public void setGenerateFrontendController(boolean generateFrontendController) {
        this.generateFrontendController = generateFrontendController;
    }

    public boolean isUseLombok() {
        return useLombok;
    }

    public void setUseLombok(boolean useLombok) {
        this.useLombok = useLombok;
    }
}
