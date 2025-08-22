package io.spring.initializr.generator.spring.code.java;

import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.language.java.JavaLanguage;
import io.spring.initializr.generator.packaging.war.WarPackaging;
import io.spring.initializr.generator.project.DomainClassDescription;
import io.spring.initializr.generator.project.FieldDescription;
import io.spring.initializr.generator.project.MutableProjectDescription;
import io.spring.initializr.generator.spring.code.SourceCodeProjectGenerationConfiguration;
import io.spring.initializr.generator.test.project.ProjectAssetTester;
import io.spring.initializr.generator.test.project.ProjectStructure;
import io.spring.initializr.generator.version.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ThymeleafTemplateContributorTests {
    private ProjectAssetTester projectTester;

    @BeforeEach
    void setup(@TempDir Path directory) {
        FieldDescription idField = new FieldDescription("id", "java.lang.Long");
        FieldDescription firstName = new FieldDescription("firstName", "java.lang.String");
        DomainClassDescription domainClass = new DomainClassDescription();
        domainClass.setClassName("User");
        domainClass.setFields(List.of(idField, firstName));
        domainClass.setGenerateFrontendController(true);

        this.projectTester = new ProjectAssetTester().withIndentingWriterFactory()
                .withConfiguration(SourceCodeProjectGenerationConfiguration.class, JavaProjectGenerationConfiguration.class)
                .withDirectory(directory)
                .withDescriptionCustomizer((description) -> {
                    description.setDomainClassDescriptions(List.of(domainClass));
                    description.setLanguage(new JavaLanguage());
                    if (description.getPlatformVersion() == null) {
                        description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
                    }
                    description.setBuildSystem(new MavenBuildSystem());
                });
    }

    @Test
    void restControllerClassFileIsContributed() {
        MutableProjectDescription description = new MutableProjectDescription();
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).containsFiles("src/main/resources/templates/user/add.html");
        assertThat(project).containsFiles("src/main/resources/templates/user/edit.html");
        assertThat(project).containsFiles("src/main/resources/templates/user/list.html");
        assertThat(project).containsFiles("src/main/resources/templates/user/view.html");
    }

    @Test
    void addTemplateCodeIsContributed() {
        MutableProjectDescription description = new MutableProjectDescription();
        description.setPackaging(new WarPackaging());
        description.setApplicationName("MyDemoApplication");
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).textFile("src/main/resources/templates/user/add.html")
            .containsExactly(
                "<!DOCTYPE html>",
                "<html xmlns:th=\"http://www.thymeleaf.org\">",
                "  <body>",
                "    <h1>Add User</h1>",
                "    <form th:action=\"@{/users/add}\" th:object=\"${user}\" method=\"POST\">",
                "      <table>",
                "        <tr>",
                "          <td>firstName</td>",
                "          <td>",
                "            <input type=\"text\" th:field=\"*{firstName}\" name=\"firstName\" />",
                "          </td>",
                "        </tr>",
                "      </table>",
                "      <input type=\"submit\" value=\"Create\" />",
                "    </form>",
                "  </body>",
                "</html>"
            );
    }

    @Test
    void editTemplateCodeIsContributed() {
        MutableProjectDescription description = new MutableProjectDescription();
        description.setPackaging(new WarPackaging());
        description.setApplicationName("MyDemoApplication");
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).textFile("src/main/resources/templates/user/edit.html")
                .containsExactly(
                    "<!DOCTYPE html>",
                    "<html xmlns:th=\"http://www.thymeleaf.org\">",
                    "  <body>",
                    "    <h1>Edit User</h1>",
                    "    <form th:action=\"@{/users/edit/{id}(id=${user.id})}\" th:object=\"${user}\" method=\"POST\">",
                    "      <table>",
                    "        <tr>",
                    "          <td>firstName</td>",
                    "          <td>",
                    "            <input type=\"text\" th:field=\"*{firstName}\" name=\"firstName\" />",
                    "          </td>",
                    "        </tr>",
                    "      </table>",
                    "      <input type=\"submit\" value=\"Submit Edit\" />",
                    "    </form>",
                    "  </body>",
                    "</html>"
                );
    }

    @Test
    void listTemplateCodeIsContributed() {
        MutableProjectDescription description = new MutableProjectDescription();
        description.setPackaging(new WarPackaging());
        description.setApplicationName("MyDemoApplication");
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).textFile("src/main/resources/templates/user/list.html")
                .containsExactly(
                "<!DOCTYPE html>",
                    "<html xmlns:th=\"http://www.thymeleaf.org\">",
                    "  <body>",
                    "    <h1>List of Users</h1>",
                    "    <div>",
                    "      <table>",
                    "        <tr th:each=\"user : ${users}\">",
                    "          <tr>",
                    "            <td><b>id: </b></td>",
                    "            <td th:text=\"${user.id}\"></td>",
                    "          </tr>",
                    "          <tr>",
                    "            <td><b>firstName: </b></td>",
                    "            <td th:text=\"${user.firstName}\"></td>",
                    "          </tr>",
                    "          <tr>",
                    "            <td>",
                    "              <a th:href=\"@{/users/{id}(id=${user.id})}\">View</a>",
                    "            </td>",
                    "          </tr>",
                    "        </tr>",
                    "      </table>",
                    "    </div>",
                    "    <a th:href=\"@{/users/add}\">Add User</a>",
                    "  </body>",
                    "</html>"
                );
    }

    @Test
    void viewTemplateCodeIsContributed() {
        MutableProjectDescription description = new MutableProjectDescription();
        description.setPackaging(new WarPackaging());
        description.setApplicationName("MyDemoApplication");
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).textFile("src/main/resources/templates/user/view.html")
                .containsExactly(//TODO
                );
    }
}
