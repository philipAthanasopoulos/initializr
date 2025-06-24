package io.spring.initializr.generator.project;

import java.util.List;

public class DomainClassDescription {

    private String className;
    private List<FieldDescription> fields;
    private boolean generateRestController;
    private boolean generateFrontendController;


    public DomainClassDescription() {
    }

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

}
