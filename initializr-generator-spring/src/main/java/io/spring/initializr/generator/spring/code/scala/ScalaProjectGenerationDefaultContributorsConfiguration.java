package io.spring.initializr.generator.spring.code.scala;

import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnBuildSystem;
import io.spring.initializr.generator.condition.ConditionalOnPackaging;
import io.spring.initializr.generator.language.ClassName;
import io.spring.initializr.generator.language.CodeBlock;
import io.spring.initializr.generator.language.Parameter;
import io.spring.initializr.generator.language.scala.ScalaMethodDeclaration;
import io.spring.initializr.generator.language.scala.ScalaTypeDeclaration;
import io.spring.initializr.generator.packaging.war.WarPackaging;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import io.spring.initializr.generator.spring.code.MainApplicationTypeCustomizer;
import io.spring.initializr.generator.spring.code.ServletInitializerCustomizer;
import io.spring.initializr.generator.spring.code.TestApplicationTypeCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Modifier;

@Configuration
public class ScalaProjectGenerationDefaultContributorsConfiguration {

    @Bean
    MainApplicationTypeCustomizer<ScalaTypeDeclaration> mainMethodContributor() {
        return (typeDeclaration) -> typeDeclaration.addMethodDeclaration(ScalaMethodDeclaration.method("main")
                .modifiers(Modifier.PUBLIC | Modifier.STATIC)
                .returning("void")
                .parameters(Parameter.of("args", String[].class))
                .body(CodeBlock.ofStatement("$T.run($L, args)", "org.springframework.boot.SpringApplication",
                        typeDeclaration.getName())));
    }

    @Bean
    TestApplicationTypeCustomizer<ScalaTypeDeclaration> junitJupiterTestMethodContributor() {
        return (typeDeclaration) -> {
            ScalaMethodDeclaration method = ScalaMethodDeclaration.method("contextLoads")
                    .returning("void")
                    .body(CodeBlock.of(""));
            method.annotations().add(ClassName.of("org.junit.jupiter.api.Test"));
            typeDeclaration.addMethodDeclaration(method);
        };
    }

    @Bean
    BuildCustomizer<Build> scalaDependenciesConfigurer() {
        return new ScalaDependenciesConfigurer();
    }

    /**
     * Scala source code contributions for projects using war packaging.
     */
    @Configuration
    @ConditionalOnPackaging(WarPackaging.ID)
    static class WarPackagingConfiguration {

        @Bean
        ServletInitializerCustomizer<ScalaTypeDeclaration> javaServletInitializerCustomizer(
                ProjectDescription description) {
            return (typeDeclaration) -> {
                ScalaMethodDeclaration configure = ScalaMethodDeclaration.method("configure")
                        .modifiers(Modifier.PROTECTED)
                        .returning("org.springframework.boot.builder.SpringApplicationBuilder")
                        .parameters(
                                Parameter.of("application", "org.springframework.boot.builder.SpringApplicationBuilder"))
                        .body(CodeBlock.ofStatement("application.sources($L)", description.getApplicationName()));
                configure.annotations().add(ClassName.of(Override.class));
                typeDeclaration.addMethodDeclaration(configure);
            };
        }

    }

    /**
     * Configuration for Scala projects built with Maven.
     */
    @Configuration
    @ConditionalOnBuildSystem(MavenBuildSystem.ID)
    static class ScalaMavenProjectConfiguration {

        @Bean
        ScalaMavenBuildCustomizer scalaBuildCustomizer() {
            return new ScalaMavenBuildCustomizer();
        }

    }

}