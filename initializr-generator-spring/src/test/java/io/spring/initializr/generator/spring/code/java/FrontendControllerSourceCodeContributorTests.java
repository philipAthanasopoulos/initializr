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

public class FrontendControllerSourceCodeContributorTests {
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
    void frontendControllerClassFileIsContributed() {
        MutableProjectDescription description = new MutableProjectDescription();
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).containsFiles("src/main/java/com/example/demo/controllers/web/UserWebController.java");
    }

    @Test
    void frontendControllerCodeIsContributedCorrectly() {
        MutableProjectDescription description = new MutableProjectDescription();
        description.setPackaging(new WarPackaging());
        description.setApplicationName("MyDemoApplication");
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).textFile("src/main/java/com/example/demo/controllers/web/UserWebController.java")
        .containsExactly(
                "package com.example.demo.controllers.web;",
                "",
                "import com.example.demo.domain.User;",
                "import com.example.demo.services.UserService;",
                "import org.springframework.stereotype.Controller;",
                "import org.springframework.ui.Model;",
                "import org.springframework.web.bind.annotation.GetMapping;",
                "import org.springframework.web.bind.annotation.ModelAttribute;",
                "import org.springframework.web.bind.annotation.PathVariable;",
                "import org.springframework.web.bind.annotation.PostMapping;",
                "import org.springframework.web.bind.annotation.RequestMapping;",
                "",
                "@Controller",
                "@RequestMapping(\"/users\")",
                "public class UserWebController {",
                "",
                "    private final UserService userService;",
                "",
                "    public UserWebController(UserService userService) {",
                "        this.userService = userService;",
                "    }",
                "",
                "    @GetMapping",
                "    public String list(Model model) {",
                "        model.addAttribute(\"users\", userService.getAllUsers());",
                "        return \"user/list\";",
                "    }",
                "",
                "    @GetMapping(\"/add\")",
                "    public String add(@ModelAttribute(\"user\") User user) {",
                "        return \"user/add\";",
                "    }",
                "",
                "    @PostMapping(\"/add\")",
                "    public String create(@ModelAttribute(\"user\") User user) {",
                "        userService.saveUser(user);",
                "        return \"redirect:/users\";",
                "    }",
                "",
                "    @GetMapping(\"/{id}\")",
                "    public String view(Model model, @PathVariable Long id) {",
                "        model.addAttribute(\"user\", userService.getUserById(id));",
                "        return \"user/view\";",
                "    }",
                "",
                "    @GetMapping(\"/edit/{id}\")",
                "    public String edit(Model model, @PathVariable Long id) {",
                "        model.addAttribute(\"user\", userService.getUserById(id));",
                "        return \"user/edit\";",
                "    }",
                "",
                "    @PostMapping(\"/edit/{id}\")",
                "    public String submitEdit(@ModelAttribute(\"user\") User user, @PathVariable Long id) {",
                "        userService.updateUser(user, id);",
                "        return \"redirect:/users\";",
                "    }",
                "",
                "    @PostMapping(\"/delete/{id}\")",
                "    public String delete(Model model, @PathVariable Long id) {",
                "        userService.deleteUserById(id);",
                "        return \"redirect:/users\";",
                "    }",
                "}"
        );
    }
}
