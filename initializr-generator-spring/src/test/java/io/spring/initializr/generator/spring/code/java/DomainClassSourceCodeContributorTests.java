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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainClassSourceCodeContributorTests {

    private ProjectAssetTester projectTester;

    @BeforeEach
    void setup(@TempDir Path directory) {
        FieldDescription idField = new FieldDescription("id", "java.lang.Long");
        FieldDescription firstName = new FieldDescription("firstName", "java.lang.String");
        DomainClassDescription domainClass = new DomainClassDescription();
        domainClass.setClassName("User");
        domainClass.setFields(List.of(idField, firstName));

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
    void mainClassFileIsContributed() {
        MutableProjectDescription description = new MutableProjectDescription();
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).containsFiles("src/main/java/com/example/demo/domain/User.java");
    }

    @Test
    void classFileCodeIsContributedCorrectly() {
        MutableProjectDescription description = new MutableProjectDescription();
        description.setPackaging(new WarPackaging());
        description.setApplicationName("MyDemoApplication");
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).textFile("src/main/java/com/example/demo/domain/User.java")
                .containsExactly(
                        "package com.example.demo.domain;",
                        "",
                        "import jakarta.persistence.Entity;",
                        "import jakarta.persistence.GeneratedValue;",
                        "import jakarta.persistence.Id;",
                        "import jakarta.persistence.Table;",
                        "",
                        "@Entity",
                        "@Table(name = \"users\")",
                        "public class User {",
                        "",
                        "    @Id",
                        "    @GeneratedValue",
                        "    private Long id;",
                        "",
                        "    private String firstName;",
                        "",
                        "    public User() {",
                        "    }",
                        "",
                        "    public Long getId() {",
                        "        return this.id;",
                        "    }",
                        "",
                        "    public String getFirstName() {",
                        "        return this.firstName;",
                        "    }",
                        "",
                        "    public void setId(Long id) {",
                        "        this.id = id;",
                        "    }",
                        "",
                        "    public void setFirstName(String firstName) {",
                        "        this.firstName = firstName;",
                        "    }",
                        "}"
                );
    }

    @Nested
    class TestWithLombok {
        private ProjectAssetTester projectTester;

        @BeforeEach
        void setupWithLombok(@TempDir Path directory) {
            FieldDescription idField = new FieldDescription("id", "java.lang.Long");
            FieldDescription firstName = new FieldDescription("firstName", "java.lang.String");
            DomainClassDescription domainClass = new DomainClassDescription();
            domainClass.setClassName("User");
            domainClass.setFields(List.of(idField, firstName));
            domainClass.setUseLombok(true);

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
        void classFileCodeIsContributedCorrectlyWhenLombokIsPresent() {
            MutableProjectDescription description = new MutableProjectDescription();
            description.setPackaging(new WarPackaging());
            description.setApplicationName("MyDemoApplication");
            ProjectStructure project = this.projectTester.generate(description);
            assertThat(project).textFile("src/main/java/com/example/demo/domain/User.java")
                    .containsExactly(
                            "package com.example.demo.domain;",
                            "",
                            "import jakarta.persistence.Entity;",
                            "import jakarta.persistence.GeneratedValue;",
                            "import jakarta.persistence.Id;",
                            "import jakarta.persistence.Table;",
                            "import lombok.Data;",
                            "",
                            "@Entity",
                            "@Table(name = \"users\")",
                            "@Data",
                            "public class User {",
                            "",
                            "    @Id",
                            "    @GeneratedValue",
                            "    private Long id;",
                            "",
                            "    private String firstName;",
                            "}"
                    );
        }
    }
}
