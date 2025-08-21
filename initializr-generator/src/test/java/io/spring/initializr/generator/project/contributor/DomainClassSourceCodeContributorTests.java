package io.spring.initializr.generator.project.contributor;

import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.language.java.JavaLanguage;
import io.spring.initializr.generator.packaging.war.WarPackaging;
import io.spring.initializr.generator.project.MutableProjectDescription;
import io.spring.initializr.generator.spring.code.SourceCodeProjectGenerationConfiguration;
import io.spring.initializr.generator.spring.code.java.JavaProjectGenerationConfiguration;
import io.spring.initializr.generator.test.project.ProjectAssetTester;
import io.spring.initializr.generator.test.project.ProjectStructure;
import io.spring.initializr.generator.version.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DomainClassSourceCodeContributorTests {

    private ProjectAssetTester projectTester;

    @BeforeEach
    void setup(@TempDir Path directory) {
        this.projectTester = new ProjectAssetTester().withIndentingWriterFactory()
                .withConfiguration(SourceCodeProjectGenerationConfiguration.class, JavaProjectGenerationConfiguration.class)
                .withDirectory(directory)
                .withDescriptionCustomizer((description) -> {
                    description.setLanguage(new JavaLanguage());
                    if (description.getPlatformVersion() == null) {
                        description.setPlatformVersion(Version.parse("2.1.0.RELEASE"));
                    }
                    description.setBuildSystem(new MavenBuildSystem());
                });
    }

    @Test
    void classFileIsContributed() {
        MutableProjectDescription description = new MutableProjectDescription();
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).containsFiles("src/main/java/com/example/domain/");
    }

    @Test
    void testClassIsContributedWithJUnit5() {
        MutableProjectDescription description = new MutableProjectDescription();
        description.setPlatformVersion(Version.parse("2.2.0.RELEASE"));
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).textFile("src/test/java/com/example/demo/DemoApplicationTests.java")
                .containsExactly("package com.example.demo;", "", "import org.junit.jupiter.api.Test;",
                        "import org.springframework.boot.test.context.SpringBootTest;", "", "@SpringBootTest",
                        "class DemoApplicationTests {", "", "    @Test", "    void contextLoads() {", "    }", "", "}");
    }

    @Test
    void servletInitializerIsContributedWhenGeneratingProjectThatUsesWarPackaging() {
        MutableProjectDescription description = new MutableProjectDescription();
        description.setPackaging(new WarPackaging());
        description.setApplicationName("MyDemoApplication");
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).textFile("src/main/java/com/example/demo/ServletInitializer.java")
                .containsExactly("package com.example.demo;", "",
                        "import org.springframework.boot.builder.SpringApplicationBuilder;",
                        "import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;", "",
                        "public class ServletInitializer extends SpringBootServletInitializer {", "", "    @Override",
                        "    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {",
                        "        return application.sources(MyDemoApplication.class);", "    }", "", "}");
    }

    @Test
    void customPackageNameIsUsedWhenGeneratingProject() {
        MutableProjectDescription description = new MutableProjectDescription();
        description.setPackageName("com.example.foo");
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).containsFiles("src/main/java/com/example/foo/DemoApplication.java",
                "src/test/java/com/example/foo/DemoApplicationTests.java");
    }

    @Test
    void customApplicationNameIsUsedWhenGeneratingProject() {
        MutableProjectDescription description = new MutableProjectDescription();
        description.setApplicationName("MyApplication");
        ProjectStructure project = this.projectTester.generate(description);
        assertThat(project).containsFiles("src/main/java/com/example/demo/MyApplication.java",
                "src/test/java/com/example/demo/MyApplicationTests.java");
    }
}