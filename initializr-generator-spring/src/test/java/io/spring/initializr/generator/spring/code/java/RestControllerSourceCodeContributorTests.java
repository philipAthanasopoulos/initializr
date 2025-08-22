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

public class RestControllerSourceCodeContributorTests {
    private ProjectAssetTester projectTester;

    @BeforeEach
    void setup(@TempDir Path directory) {
        FieldDescription idField = new FieldDescription("id", "java.lang.Long");
        FieldDescription firstName = new FieldDescription("firstName", "java.lang.String");
        DomainClassDescription domainClass = new DomainClassDescription();
        domainClass.setClassName("User");
        domainClass.setFields(List.of(idField, firstName));
        domainClass.setGenerateRestController(true);

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
        assertThat(project).containsFiles("src/main/java/com/example/demo/controllers/api/UserController.java");
    }

    @Test
    void restControllerCodeIsContributedCorrectly() {
        MutableProjectDescription description = new MutableProjectDescription();
        description.setPackaging(new WarPackaging());
        description.setApplicationName("MyDemoApplication");
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).textFile("src/main/java/com/example/demo/controllers/api/UserController.java")
                .containsExactly(
                        "package com.example.demo.controllers.api;",
                        "",
                        "import com.example.demo.domain.User;",
                        "import com.example.demo.services.UserService;",
                        "import java.util.List;",
                        "import org.springframework.web.bind.annotation.DeleteMapping;",
                        "import org.springframework.web.bind.annotation.GetMapping;",
                        "import org.springframework.web.bind.annotation.PathVariable;",
                        "import org.springframework.web.bind.annotation.PostMapping;",
                        "import org.springframework.web.bind.annotation.PutMapping;",
                        "import org.springframework.web.bind.annotation.RequestBody;",
                        "import org.springframework.web.bind.annotation.RequestMapping;",
                        "import org.springframework.web.bind.annotation.RestController;",
                        "",
                        "@RestController",
                        "@RequestMapping(\"/api/users\")",
                        "public class UserController {",
                        "",
                        "    private final UserService userService;",
                        "",
                        "    public UserController(UserService userService) {",
                        "        this.userService = userService;",
                        "    }",
                        "",
                        "    @GetMapping",
                        "    public List<User> getAllUsers() {",
                        "        return userService.getAllUsers();",
                        "    }",
                        "",
                        "    @GetMapping(\"/{id}\")",
                        "    public User getUserById(@PathVariable Long id) {",
                        "        return userService.getUserById(id);",
                        "    }",
                        "",
                        "    @PostMapping",
                        "    public User createUser(@RequestBody User user) {",
                        "        return userService.saveUser(user);",
                        "    }",
                        "",
                        "    @PutMapping(\"/{id}\")",
                        "    public User updateUser(@RequestBody User user, @PathVariable Long id) {",
                        "        return userService.updateUser(user, id);",
                        "    }",
                        "",
                        "    @DeleteMapping(\"/{id}\")",
                        "    public void deleteUserById(@PathVariable Long id) {",
                        "        userService.deleteUserById(id);",
                        "    }",
                        "}"
                );
    }
}
