/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.initializr.generator.spring.code.java;

import io.spring.initializr.generator.condition.ConditionalOnLanguage;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.language.java.*;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.project.contributor.DomainClassSourceCodeContributor;
import io.spring.initializr.generator.project.contributor.RepositorySourceCodeContributor;
import io.spring.initializr.generator.project.contributor.RestControllerSourceCodeContributor;
import io.spring.initializr.generator.project.contributor.ServiceSourceCodeContributor;
import io.spring.initializr.generator.spring.code.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Configuration for contributions specific to the generation of a project that will use
 * Java as its language.
 *
 * @author Andy Wilkinson
 */
@ProjectGenerationConfiguration
@ConditionalOnLanguage(JavaLanguage.ID)
@Import(JavaProjectGenerationDefaultContributorsConfiguration.class)
public class JavaProjectGenerationConfiguration {

    private final ProjectDescription description;

    public JavaProjectGenerationConfiguration(ProjectDescription description) {
        this.description = description;
    }

    @Bean
    JavaSourceCodeWriter javaSourceCodeWriter(IndentingWriterFactory indentingWriterFactory) {
        return new JavaSourceCodeWriter(indentingWriterFactory);
    }

    @Bean
    MainSourceCodeProjectContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> mainJavaSourceCodeProjectContributor(
            ObjectProvider<MainApplicationTypeCustomizer<?>> mainApplicationTypeCustomizers,
            ObjectProvider<MainCompilationUnitCustomizer<?, ?>> mainCompilationUnitCustomizers,
            ObjectProvider<MainSourceCodeCustomizer<?, ?, ?>> mainSourceCodeCustomizers,
            JavaSourceCodeWriter javaSourceCodeWriter) {
        return new MainSourceCodeProjectContributor<>(this.description, JavaSourceCode::new, javaSourceCodeWriter,
                mainApplicationTypeCustomizers, mainCompilationUnitCustomizers, mainSourceCodeCustomizers);
    }

    @Bean
    DomainClassSourceCodeContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> domainClassSourceCodeContributor(
            JavaSourceCodeWriter javaSourceCodeWriter) {
        DomainClassSourceCodeContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> domainClassSourceCodeContributor = new DomainClassSourceCodeContributor<>(
                javaSourceCodeWriter,
                JavaSourceCode::new,
                this.description
        );
        domainClassSourceCodeContributor.setDomainClassDescriptions(this.description.getDomainClassDescriptions());
        return domainClassSourceCodeContributor;
    }

    @Bean
    RestControllerSourceCodeContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> restControllerSourceCodeContributor(
            JavaSourceCodeWriter javaSourceCodeWriter) {
        RestControllerSourceCodeContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> restControllerSourceCodeContributor = new RestControllerSourceCodeContributor<>(
                javaSourceCodeWriter,
                JavaSourceCode::new,
                this.description
        );
        return restControllerSourceCodeContributor;
    }

    @Bean
    ServiceSourceCodeContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> serviceSourceCodeContributor(
            JavaSourceCodeWriter javaSourceCodeWriter) {
        ServiceSourceCodeContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> serviceSourceCodeContributor = new ServiceSourceCodeContributor<>(
                javaSourceCodeWriter,
                JavaSourceCode::new,
                this.description
        );
        return serviceSourceCodeContributor;
    }

    @Bean
    RepositorySourceCodeContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> repositorySourceCodeContributor(
            JavaSourceCodeWriter javaSourceCodeWriter) {
        RepositorySourceCodeContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> restControllerSourceCodeContributor = new RepositorySourceCodeContributor<>(
                javaSourceCodeWriter,
                JavaSourceCode::new,
                this.description
        );
        return restControllerSourceCodeContributor;
    }

    @Bean
    TestSourceCodeProjectContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> testJavaSourceCodeProjectContributor(
            ObjectProvider<TestApplicationTypeCustomizer<?>> testApplicationTypeCustomizers,
            ObjectProvider<TestSourceCodeCustomizer<?, ?, ?>> testSourceCodeCustomizers,
            JavaSourceCodeWriter javaSourceCodeWriter) {
        return new TestSourceCodeProjectContributor<>(this.description, JavaSourceCode::new, javaSourceCodeWriter,
                testApplicationTypeCustomizers, testSourceCodeCustomizers);
    }

}
