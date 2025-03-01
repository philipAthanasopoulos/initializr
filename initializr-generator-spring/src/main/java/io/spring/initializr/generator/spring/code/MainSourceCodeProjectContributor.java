/*
 * Copyright 2012-2023 the original author or authors.
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

package io.spring.initializr.generator.spring.code;

import io.spring.initializr.generator.language.*;
import io.spring.initializr.generator.language.java.JavaCompilationUnit;
import io.spring.initializr.generator.language.java.JavaFieldDeclaration;
import io.spring.initializr.generator.language.java.JavaTypeDeclaration;
import io.spring.initializr.generator.language.kotlin.KotlinCompilationUnit;
import io.spring.initializr.generator.language.kotlin.KotlinModifier;
import io.spring.initializr.generator.language.kotlin.KotlinPropertyDeclaration;
import io.spring.initializr.generator.language.kotlin.KotlinTypeDeclaration;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.contributor.ProjectContributor;
import io.spring.initializr.generator.spring.util.LambdaSafe;
import org.springframework.beans.factory.ObjectProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.reflect.Modifier.*;

/**
 * {@link ProjectContributor} for the application's main source code.
 *
 * @param <T> language-specific type declaration
 * @param <C> language-specific compilation unit
 * @param <S> language-specific source code
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 */
public class MainSourceCodeProjectContributor<T extends TypeDeclaration, C extends CompilationUnit<T>, S extends SourceCode<T, C>>
        implements ProjectContributor {

    private final ProjectDescription description;

    private final Supplier<S> sourceFactory;

    private final SourceCodeWriter<S> sourceWriter;

    private final ObjectProvider<MainApplicationTypeCustomizer<? extends TypeDeclaration>> mainTypeCustomizers;

    private final ObjectProvider<MainCompilationUnitCustomizer<?, ?>> mainCompilationUnitCustomizers;

    private final ObjectProvider<MainSourceCodeCustomizer<?, ?, ?>> mainSourceCodeCustomizers;

    public MainSourceCodeProjectContributor(ProjectDescription description, Supplier<S> sourceFactory,
                                            SourceCodeWriter<S> sourceWriter, ObjectProvider<MainApplicationTypeCustomizer<?>> mainTypeCustomizers,
                                            ObjectProvider<MainCompilationUnitCustomizer<?, ?>> mainCompilationUnitCustomizers,
                                            ObjectProvider<MainSourceCodeCustomizer<?, ?, ?>> mainSourceCodeCustomizers) {
        this.description = description;
        this.sourceFactory = sourceFactory;
        this.sourceWriter = sourceWriter;
        this.mainTypeCustomizers = mainTypeCustomizers;
        this.mainCompilationUnitCustomizers = mainCompilationUnitCustomizers;
        this.mainSourceCodeCustomizers = mainSourceCodeCustomizers;
    }

    @Override
    public void contribute(Path projectRoot) throws IOException {
        S sourceCode = this.sourceFactory.get();
        String applicationName = this.description.getApplicationName();
        C compilationUnit = sourceCode.createCompilationUnit(this.description.getPackageName(), applicationName);
        T mainApplicationType = compilationUnit.createTypeDeclaration(applicationName);

        //User class
//        JavaCompilationUnit userCompilationUnit = (JavaCompilationUnit) sourceCode.createCompilationUnit("com.example.model", "User");
//        JavaTypeDeclaration userClass = userCompilationUnit.createTypeDeclaration("User");
//        userClass.modifiers(PUBLIC);
//        userClass.annotations().add(ClassName.of("javax.persistence.Entity"));
//        userClass.annotations().add(ClassName.of("lombok.Data"));
//        JavaFieldDeclaration firstNameField = JavaFieldDeclaration.field("id")
//                .modifiers(PRIVATE)
//                .returning("java.lang.Long");
//        firstNameField.annotations().add(ClassName.of("javax.persistence.Id"));
//        JavaFieldDeclaration lastName = JavaFieldDeclaration.field("lastName")
//                .returning("java.lang.String");
////        userClass.addFieldDeclaration(firstNameField);
////        userClass.addFieldDeclaration(lastName);
//
////        //User Repository
//        JavaCompilationUnit userRepositoryCompilationUnit = (JavaCompilationUnit) sourceCode.createCompilationUnit("com.example.repositories", "UserRepository");
//        JavaTypeDeclaration userRepositoryTypeDeclaration = userRepositoryCompilationUnit.createTypeDeclaration("UserRepository");
//        userRepositoryTypeDeclaration.annotations().add(ClassName.of("org.springframework.stereotype.Repository"));
//        userRepositoryTypeDeclaration.extend("org.springframework.data.jpa.repository.JpaRepository");
//        userRepositoryTypeDeclaration.modifiers(PUBLIC | INTERFACE);
//
////        //User Service
//        JavaCompilationUnit userServiceCompilationUnit = (JavaCompilationUnit) sourceCode.createCompilationUnit("com.example.services", "UserService");
//        JavaTypeDeclaration userServiceClass = userServiceCompilationUnit.createTypeDeclaration("UserService");
//        userServiceClass.modifiers(PUBLIC);
//        userServiceClass.annotations().add(ClassName.of("org.springframework.stereotype.Service"));
//        JavaFieldDeclaration userRepositoryField = JavaFieldDeclaration
//                .field("userRepository")
//                .modifiers(PRIVATE | FINAL)
//                .returning("com.example.repositories.UserRepository");
//        userServiceClass.addFieldDeclaration(userRepositoryField);
//
//        //        //User Controller
//        JavaCompilationUnit userControllerCompilationUnit = (JavaCompilationUnit) sourceCode.createCompilationUnit("com.example.controllers", "UserController");
//        JavaTypeDeclaration userControllerClass = userControllerCompilationUnit.createTypeDeclaration("UserController");
//        userControllerClass.modifiers(PUBLIC);
//        userControllerClass.annotations().add(ClassName.of("org.springframework.stereotype.Controller"));
//        JavaFieldDeclaration userServiceFieldDeclaration = JavaFieldDeclaration
//                .field("userService")
//                .modifiers(PRIVATE | FINAL)
//                .returning("com.example.services.UserService");
//        userControllerClass.addFieldDeclaration(userServiceFieldDeclaration);


        //KOTLIN
        KotlinCompilationUnit kotlinCompilationUnit = (KotlinCompilationUnit) sourceCode.createCompilationUnit("com.example.controllers", "KotlinUserController");
        KotlinTypeDeclaration userControllerClassKotlin = kotlinCompilationUnit.createTypeDeclaration("UserControllerKotlin");
        userControllerClassKotlin.modifiers(KotlinModifier.PUBLIC);
        userControllerClassKotlin.annotations().add(ClassName.of("org.springframework.stereotype.Controller"));

        customizeMainApplicationType(mainApplicationType);
        customizeMainCompilationUnit(compilationUnit);
        customizeMainSourceCode(sourceCode);
        this.sourceWriter.writeTo(
                this.description.getBuildSystem().getMainSource(projectRoot, this.description.getLanguage()),
                sourceCode);
    }

    @SuppressWarnings("unchecked")
    private void customizeMainApplicationType(T mainApplicationType) {
        List<MainApplicationTypeCustomizer<?>> customizers = this.mainTypeCustomizers.orderedStream()
                .collect(Collectors.toList());
        LambdaSafe.callbacks(MainApplicationTypeCustomizer.class, customizers, mainApplicationType)
                .invoke((customizer) -> customizer.customize(mainApplicationType));
    }

    @SuppressWarnings("unchecked")
    private void customizeMainCompilationUnit(C compilationUnit) {
        List<MainCompilationUnitCustomizer<?, ?>> customizers = this.mainCompilationUnitCustomizers.orderedStream()
                .collect(Collectors.toList());
        LambdaSafe.callbacks(MainCompilationUnitCustomizer.class, customizers, compilationUnit)
                .invoke((customizer) -> customizer.customize(compilationUnit));
    }

    @SuppressWarnings("unchecked")
    private void customizeMainSourceCode(S sourceCode) {
        List<MainSourceCodeCustomizer<?, ?, ?>> customizers = this.mainSourceCodeCustomizers.orderedStream()
                .collect(Collectors.toList());
        LambdaSafe.callbacks(MainSourceCodeCustomizer.class, customizers, sourceCode)
                .invoke((customizer) -> customizer.customize(sourceCode));
    }

}
