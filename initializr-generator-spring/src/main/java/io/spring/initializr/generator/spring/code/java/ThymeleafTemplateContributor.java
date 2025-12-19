package io.spring.initializr.generator.spring.code.java;

import io.spring.initializr.generator.project.DomainClassDescription;
import io.spring.initializr.generator.project.FieldDescription;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.contributor.ProjectContributor;
import org.atteo.evo.inflector.English;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.atteo.evo.inflector.English.plural;

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
                            "  <body>%n" +
                            "    <h1>Add %s</h1>%n" +
                            "    <form th:action=\"@{/%s/add}\" th:object=\"${%s}\" method=\"POST\">%n" +
                            "      <table>%n" +
                            getFieldsInputs(domainClassDescription, 4) +
                            "      </table>%n" +
                            "      <input type=\"submit\" value=\"Create\" />%n" +
                            "    </form>%n" +
                            "  </body>%n" +
                            "</html>%n",
                    domainClassDescription.getClassName(),
                    plural(domainClassDescription.getClassName().toLowerCase()),
                    domainClassDescription.getClassName().toLowerCase()
            ));
        }
    }

    private String getFieldsInputs(DomainClassDescription description, int nestingLevel) {
        StringBuilder res = new StringBuilder();
        String nestingSpace = "  ".repeat(nestingLevel);
        for (FieldDescription field : description.getFields()) {
            if (field.getFieldName().equals("id")) {
                continue;
            }
            res.append(String.format(
                    nestingSpace + "<tr>%n" +
                            nestingSpace + "  <td>%s</td>%n" +
                            nestingSpace + "  <td>%n" +
                            nestingSpace + "    <input type=\"text\" th:field=\"*{%s}\" name=\"%s\" />%n" +
                            nestingSpace + "  </td>%n" +
                            nestingSpace + "</tr>%n",
                    field.getFieldName(),
                    field.getFieldName(),
                    field.getFieldName()
            ));
        }
        return res.toString();
    }

    private void generateViewTemplate(DomainClassDescription domainClassDescription, Path projectRoot) throws IOException {
        Path file = generateTemplateFile(domainClassDescription, projectRoot, "/view.html");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
            writer.write(String.format(
                    "<!DOCTYPE html>%n" +
                            "<html xmlns:th=\"http://www.thymeleaf.org\">%n" +
                            "  <body>%n" +
                            "    <h1>View %s</h1>%n" +
                            "    <table th:object=\"${%s}\">%n" +
                            getTableRowsForAllFields(domainClassDescription, 3) +
                            "    </table>%n" +
                            "    <a th:href=\"@{/%ss/edit/{id}(id=${%s.id})}\">Edit %s</a> | %n" +
                            "    <form th:action=\"@{/%ss/delete/{id}(id=${%s.id})}\" method=\"post\" style=\"display:inline;\">%n" +
                            "      <button type=\"submit\">Delete %s</button>%n" +
                            "    </form>%n" +
                            "  </body>%n" +
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

    private String getTableRowsForAllFields(DomainClassDescription description, int nestingLevel) {
        StringBuilder res = new StringBuilder();
        String nestingSpace = "  ".repeat(nestingLevel);
        for (FieldDescription field : description.getFields()) {
            res.append(String.format(
                    nestingSpace + "<tr>%n" +
                            nestingSpace + "  <td><b>%s: </b></td>%n" +
                            nestingSpace + "  <td th:text=\"${%s.%s}\"></td>%n" +
                            nestingSpace + "</tr>%n",
                    field.getFieldName(),
                    description.getClassName().toLowerCase(),
                    field.getFieldName()
            ));
        }
        return res.toString();
    }

    private void generateEditTemplate(DomainClassDescription domainClassDescription, Path projectRoot) throws IOException {
        Path file = generateTemplateFile(domainClassDescription, projectRoot, "/edit.html");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
            writer.write(String.format(
                    "<!DOCTYPE html>%n" +
                            "<html xmlns:th=\"http://www.thymeleaf.org\">%n" +
                            "  <body>%n" +
                            "    <h1>Edit %s</h1>%n" +
                            "    <form th:action=\"@{/%s/edit/{id}(id=${%s.id})}\" th:object=\"${%s}\" method=\"POST\">%n" +
                            "      <table>%n" +
                            "%s" +
                            "      </table>%n" +
                            "      <input type=\"submit\" value=\"Submit Edit\" />%n" +
                            "    </form>%n" +
                            "  </body>%n" +
                            "</html>%n",
                    domainClassDescription.getClassName(),
                    plural(domainClassDescription.getClassName().toLowerCase()),
                    domainClassDescription.getClassName().toLowerCase(),
                    domainClassDescription.getClassName().toLowerCase(),
                    getFieldsInputs(domainClassDescription, 4)
            ));
        }
    }

    private void generateListTemplate(DomainClassDescription domainClassDescription, Path projectRoot) throws IOException {
        Path file = generateTemplateFile(domainClassDescription, projectRoot, "/list.html");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(file))) {
            writer.write(String.format(
                    "<!DOCTYPE html>%n" +
                            "<html xmlns:th=\"http://www.thymeleaf.org\">%n" +
                            "  <body>%n" +
                            "    <h1>List of %s</h1>%n" +
                            "    <table th:each=\"%s : ${%s}\">%n" +
                            getTableRowsForAllFields(domainClassDescription, 3) +
                            "      <tr>%n" +
                            "        <td>%n" +
                            "          <a th:href=\"@{/%s/{id}(id=${%s.id})}\">View</a>%n" +
                            "        </td>%n" +
                            "      </tr>%n" +
                            "    </table>%n" +
                            "    <a th:href=\"@{/%s/add}\">Add %s</a>%n" +
                            "  </body>%n" +
                            "</html>%n",
                    plural(domainClassDescription.getClassName()),
                    domainClassDescription.getClassName().toLowerCase(),
                    plural(domainClassDescription.getClassName().toLowerCase()),
                    plural(domainClassDescription.getClassName().toLowerCase()),
                    domainClassDescription.getClassName().toLowerCase(),
                    plural(domainClassDescription.getClassName().toLowerCase()),
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

