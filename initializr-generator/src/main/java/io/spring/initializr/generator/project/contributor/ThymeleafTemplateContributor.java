package io.spring.initializr.generator.project.contributor;

import io.spring.initializr.generator.project.DomainClassDescription;
import io.spring.initializr.generator.project.ProjectDescription;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ThymeleafTemplateContributor implements ProjectContributor {

    private List<DomainClassDescription> domainClassDescriptions = new ArrayList<>();

    public ThymeleafTemplateContributor(ProjectDescription description) {
        this.domainClassDescriptions = description.getDomainClassDescriptions();
    }

    @Override
    public void contribute(Path projectRoot) throws IOException {
        for (DomainClassDescription domainClassDescription : this.domainClassDescriptions) {
            generateListTemplate(domainClassDescription, projectRoot);
            generateEditTemplate(domainClassDescription, projectRoot);
            generateViewTemplate(domainClassDescription, projectRoot);
            generateAddTemplate(domainClassDescription, projectRoot);
        }
    }

    private void generateAddTemplate(DomainClassDescription domainClassDescription, Path projectRoot) throws IOException {
        Path file = generateTemplateFile(domainClassDescription, projectRoot, "/add.html");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
            writer.write("<!DOCTYPE html>");
        }
    }

    private void generateViewTemplate(DomainClassDescription domainClassDescription, Path projectRoot) throws IOException {
        Path file = generateTemplateFile(domainClassDescription, projectRoot, "/view.html");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
            writer.write("<!DOCTYPE html>");
        }
    }

    private void generateEditTemplate(DomainClassDescription domainClassDescription, Path projectRoot) throws IOException {
        Path file = generateTemplateFile(domainClassDescription, projectRoot, "/edit.html");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
            writer.write("<!DOCTYPE html>");
        }
    }

    private void generateListTemplate(DomainClassDescription domainClassDescription, Path projectRoot) throws IOException {
        Path file = generateTemplateFile(domainClassDescription, projectRoot, "/list.html");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
            writer.write("<!DOCTYPE html>");
        }
    }

    private Path generateTemplateFile(DomainClassDescription domainClassDescription, Path projectRoot, String x) throws IOException {
        String domainClassName = domainClassDescription.getClassName().toLowerCase();
        Path output = projectRoot.resolve("src/main/resources/templates/" + domainClassName + x);
        if (!Files.exists(output)) {
            Files.createDirectories(output.getParent());
            Files.createFile(output);
        }
        return output;
    }

    @Override
    public int getOrder() {
        return ProjectContributor.super.getOrder();
    }
}

