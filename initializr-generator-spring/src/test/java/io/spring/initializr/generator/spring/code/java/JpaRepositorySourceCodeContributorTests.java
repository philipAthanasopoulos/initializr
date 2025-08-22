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

public class JpaRepositorySourceCodeContributorTests {

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
    void repositoryClassFileIsContributed() {
        MutableProjectDescription description = new MutableProjectDescription();
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).containsFiles("src/main/java/com/example/demo/repositories/UserRepository.java");
    }

    @Test
    void repositoryCodeIsContributedCorrectly() {
        MutableProjectDescription description = new MutableProjectDescription();
        description.setPackaging(new WarPackaging());
        description.setApplicationName("MyDemoApplication");
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).textFile("src/main/java/com/example/demo/repositories/UserRepository.java")
                .containsExactly(
                        "package com.example.demo.repositories;",
                        "",
                        "import com.example.demo.domain.User;",
                        "import org.springframework.data.jpa.repository.JpaRepository;",
                        "import org.springframework.stereotype.Repository;",
                        "",
                        "@Repository",
                        "public interface UserRepository extends JpaRepository<User, Long> {",
                        "}"
                );
    }
}
