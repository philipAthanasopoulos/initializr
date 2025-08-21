package io.spring.initializr.generator.project.contributor;

import io.spring.initializr.generator.project.DomainClassDescription;
import io.spring.initializr.generator.project.FieldDescription;
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
            if (domainClassDescription.isGenerateFrontendController()) {
                generateListTemplate(domainClassDescription, projectRoot);
                generateEditTemplate(domainClassDescription, projectRoot);
                generateViewTemplate(domainClassDescription, projectRoot);
                generateAddTemplate(domainClassDescription, projectRoot);
            }
        }
    }

    private void generateAddTemplate(DomainClassDescription domainClassDescription, Path projectRoot) throws IOException {
        Path file = generateTemplateFile(domainClassDescription, projectRoot, "/add.html");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
            writer.write(String.format(
                    "<!DOCTYPE html>%n" +
                            "<html xmlns:th=\"http://www.thymeleaf.org\">%n" +
                            "<body>%n" +
                            "\t<h1>Add %s</h1>%n" +
                            "\t<form th:action=\"@{/%s/add}\" th:object=\"${%s}\" method=\"POST\">%n" +
                            "\t\t<table>%n" +
                            getFieldsInputs(domainClassDescription) +
                            "\t\t</table>%n" +
                            "\t\t<input type=\"submit\" value=\"Create\" />%n" +
                            "\t</form>%n" +
                            "</body>%n" +
                            "</html>%n",
                    domainClassDescription.getClassName(),
                    domainClassDescription.getClassName().toLowerCase() + "s",
                    domainClassDescription.getClassName().toLowerCase()
            ));
        }
    }

    private String getFieldsInputs(DomainClassDescription description) {
        String res = "";
        for (FieldDescription field : description.getFields()) {
            if (field.getFieldName().equals("id")) {
                continue;
            }
            res += String.format(
                    "\t\t<tr>%n" +
                            "\t\t\t\t<td>%s</td>%n" +
                            "\t\t\t\t<td><input type=\"text\" th:field=\"*{%s}\" name=\"%s\" /></td>%n" +
                            "\t\t</tr>%n",
                    field.getFieldName(),
                    field.getFieldName(),
                    field.getFieldName()
            );
        }
        return res;
    }

    private void generateViewTemplate(DomainClassDescription domainClassDescription, Path projectRoot) throws IOException {
        Path file = generateTemplateFile(domainClassDescription, projectRoot, "/view.html");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
            writer.write(String.format(
                    "<!DOCTYPE html>%n" +
                            "<html xmlns:th=\"http://www.thymeleaf.org\">%n" +
                            "<body>%n" +
                            "\t<h1>View %s</h1>%n" +
                            "\t<div th:object=\"${%s}\">%n" +
                            "\t\t<table>%n" +
                            getFieldsViews(domainClassDescription) +
                            "\t\t</table>%n" +
                            "<a th:href=\"@{/%ss/edit/{id}(id = ${%s.id})}\">Edit %s</a> | " +
                            "<form th:action=\"@{/%ss/delete/{id}(id=${%s.id})}\" method=\"post\" style=\"display:inline;\">" +
                            "<button type=\"submit\">Delete %s</button>" +
                            " </form>" +
                            "\t</div>%n" +
                            "</body>%n" +
                            "</html>%n",
                    domainClassDescription.getClassName(),
                    domainClassDescription.getClassName().toLowerCase(),
                    domainClassDescription.getClassName().toLowerCase(),
                    domainClassDescription.getClassName().toLowerCase(),
                    domainClassDescription.getClassName().toLowerCase(),
                    domainClassDescription.getClassName().toLowerCase(),
                    domainClassDescription.getClassName().toLowerCase(),
                    domainClassDescription.getClassName().toLowerCase(),
                    domainClassDescription.getClassName().toLowerCase(),
                    domainClassDescription.getClassName().toLowerCase(),
                    domainClassDescription.getClassName().toLowerCase()
            ));
        }
    }

    private String getFieldsViews(DomainClassDescription description) {
        String res = "";
        for (FieldDescription field : description.getFields()) {
            res += String.format(
                    "\t\t<tr>%n" +
                            "\t\t\t\t<td><b>%s: </b></td>%n" +
                            "\t\t\t\t<td th:text=\"${%s.%s}\"></td>%n" +
                            "\t\t</tr>%n",
                    field.getFieldName(),
                    description.getClassName().toLowerCase(),
                    field.getFieldName()
            );
        }
        return res;
    }

    private void generateEditTemplate(DomainClassDescription domainClassDescription, Path projectRoot) throws IOException {
        Path file = generateTemplateFile(domainClassDescription, projectRoot, "/edit.html");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
            writer.write(String.format(
                    "<!DOCTYPE html>%n" +
                            "<html xmlns:th=\"http://www.thymeleaf.org\">%n" +
                            "<body>%n" +
                            "\t<h1>Edit %s</h1>%n" +
                            "\t\t<form th:action=\"@{/%s/edit/{id}(id=${%s.id})}\" th:object=\"${%s}\" method=\"POST\">%n" +
                            "\t\t\t<table>%n" +
                            "%s" +
                            "\t\t\t</table>%n" +
                            "\t\t\t<input type=\"submit\" value=\"Submit Edit\" />%n" +
                            "\t\t</form>%n" +
                            "</body>%n" +
                            "</html>%n",
                    domainClassDescription.getClassName(),
                    domainClassDescription.getClassName().toLowerCase() + "s",
                    domainClassDescription.getClassName().toLowerCase(),
                    domainClassDescription.getClassName().toLowerCase(),
                    getFieldsInputs(domainClassDescription)
            ));
        }
    }

    private void generateListTemplate(DomainClassDescription domainClassDescription, Path projectRoot) throws IOException {
        Path file = generateTemplateFile(domainClassDescription, projectRoot, "/list.html");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
            writer.write(String.format(
                    "<!DOCTYPE html>%n" +
                            "<html xmlns:th=\"http://www.thymeleaf.org\">%n" +
                            "<body>%n" +
                            "\t<h1>List of %s</h1>%n" +
                            "\t\t<div>%n" +
                            "\t\t\t<table>%n" +
                            "\t\t\t\t<tr th:each=\"%s : ${%s}\">%n" +
                            getFieldsViews(domainClassDescription) +
                            "\t\t\t\t<tr>%n" +
                            "\t\t\t\t\t<td>%n" +
                            "\t\t\t\t\t\t<a th:href=\"@{/%s/{id}(id=${%s.id})}\">View</a>%n" +
                            "\t\t\t\t\t</td>%n" +
                            "\t\t\t\t</tr>%n" +
                            "\t\t\t</table>%n" +
                            "\t\t</div>%n" +
                            "\t<a th:href=\"@{/%s/add}\">Add %s</a>%n" +
                            "</body>%n" +
                            "</html>%n",
                    domainClassDescription.getClassName() + "s",
                    domainClassDescription.getClassName().toLowerCase(),
                    domainClassDescription.getClassName().toLowerCase() + "s",
                    domainClassDescription.getClassName().toLowerCase() + "s",
                    domainClassDescription.getClassName().toLowerCase(),
                    domainClassDescription.getClassName().toLowerCase() + "s",
                    domainClassDescription.getClassName()
            ));
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

