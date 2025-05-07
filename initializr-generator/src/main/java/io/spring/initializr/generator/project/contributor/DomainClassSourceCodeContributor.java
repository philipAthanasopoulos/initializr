package io.spring.initializr.generator.project.contributor;

import io.spring.initializr.generator.language.*;
import io.spring.initializr.generator.language.java.JavaCompilationUnit;
import io.spring.initializr.generator.language.java.JavaFieldDeclaration;
import io.spring.initializr.generator.language.java.JavaMethodDeclaration;
import io.spring.initializr.generator.language.java.JavaTypeDeclaration;
import io.spring.initializr.generator.project.AssociationDescription;
import io.spring.initializr.generator.project.DomainClassDescription;
import io.spring.initializr.generator.project.FieldDescription;
import io.spring.initializr.generator.project.ProjectDescription;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.PUBLIC;

public class DomainClassSourceCodeContributor<T extends TypeDeclaration, C extends CompilationUnit<T>, S extends SourceCode<T, C>>
        implements ProjectContributor {

    private List<DomainClassDescription> domainClassDescriptions = new ArrayList<>();
    private final SourceCodeWriter<S> sourceCodeWriter;
    private final Supplier<S> sourceFactory;
    private final ProjectDescription description;


    public DomainClassSourceCodeContributor(SourceCodeWriter<S> sourceCodeWriter, Supplier<S> sourceFactory, ProjectDescription description) {
        this.sourceCodeWriter = sourceCodeWriter;
        this.sourceFactory = sourceFactory;
        this.description = description;
        this.domainClassDescriptions = description.getDomainClassDescriptions();
    }

    @Override
    public void contribute(Path projectRoot) throws IOException {
        S sourceCode = this.sourceFactory.get();

        for (DomainClassDescription domainClassDescription : domainClassDescriptions) {
            generateDomainClasses(domainClassDescription, sourceCode);
        }

        for (AssociationDescription associationDescription : this.description.getAssotiationDescriptions()) {
            generateAssociationFields(associationDescription, sourceCode);
        }

        this.sourceCodeWriter.writeTo(
                this.description.getBuildSystem().getMainSource(projectRoot, this.description.getLanguage()),
                sourceCode);
    }

    private void generateAssociationFields(AssociationDescription associationDescription, S sourceCode) {
        JavaCompilationUnit firstCompilationUnit = (JavaCompilationUnit) sourceCode.getCompilationUnits().stream()
                .filter(unit -> unit.getName().equals(associationDescription.getFirstClassName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Compilation unit not found"));
        JavaTypeDeclaration firstTypeDeclaration = firstCompilationUnit.getTypeDeclarations().get(0);

        JavaCompilationUnit secondCompilationUnit = (JavaCompilationUnit) sourceCode.getCompilationUnits().stream()
                .filter(unit -> unit.getName().equals(associationDescription.getSecondClassName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Compilation unit not found"));
        JavaTypeDeclaration secondTypeDeclaration = secondCompilationUnit.getTypeDeclarations().get(0);

        JavaFieldDeclaration firstField;
        JavaFieldDeclaration secondField;

        switch (associationDescription.getAssotiationType()) {
            case ONE_TO_ONE -> {
                firstField = JavaFieldDeclaration
                        .field(secondTypeDeclaration.getName().toLowerCase())
                        .modifiers(PRIVATE)
                        .returning(secondTypeDeclaration.getName());
                firstField.annotations().add(ClassName.of("jakarta.persistence.OneToOne"));
                firstField.annotations().add(ClassName.of("jakarta.persistence.JoinColumn"), builder -> builder.set("name",secondTypeDeclaration.getName().toLowerCase() + "_id"));
                firstTypeDeclaration.addFieldDeclaration(firstField);

                secondField = JavaFieldDeclaration
                        .field(firstTypeDeclaration.getName().toLowerCase())
                        .modifiers(PRIVATE)
                        .returning(firstTypeDeclaration.getName());
                secondField.annotations().add(ClassName.of("jakarta.persistence.OneToOne"), builder -> builder.set("mappedBy",firstTypeDeclaration.getName().toLowerCase()));
                secondTypeDeclaration.addFieldDeclaration(secondField);
            }
            case ONE_TO_MANY -> {
                firstField = JavaFieldDeclaration
                        .field(secondTypeDeclaration.getName().toLowerCase() + "s")
                        .returnGenerics(secondTypeDeclaration.getName())
                        .modifiers(PRIVATE)
                        .returning("java.util.List");

                firstField.annotations().add(ClassName.of("jakarta.persistence.OneToMany"), builder -> builder.set("mappedBy", firstTypeDeclaration.getName().toLowerCase()));
                firstTypeDeclaration.addFieldDeclaration(firstField);

                secondField = JavaFieldDeclaration
                        .field(firstTypeDeclaration.getName().toLowerCase())
                        .modifiers(PRIVATE)
                        .returning(firstTypeDeclaration.getName());
                secondField.annotations().add(ClassName.of("jakarta.persistence.ManyToOne"));
                secondField.annotations().add(ClassName.of("jakarta.persistence.JoinColumn"), builder -> builder.set("name", firstTypeDeclaration.getName().toLowerCase() + "_id"));
                secondTypeDeclaration.addFieldDeclaration(secondField);
            }
            case MANY_TO_ONE -> {
                firstField = JavaFieldDeclaration
                        .field(secondTypeDeclaration.getName().toLowerCase())
                        .modifiers(PRIVATE)
                        .returning(secondTypeDeclaration.getName());

                firstField.annotations().add(ClassName.of("jakarta.persistence.ManyToOne"));
                firstField.annotations().add(ClassName.of("jakarta.persistence.JoinColumn"), builder -> builder.set("name", secondTypeDeclaration.getName().toLowerCase() + "_id"));

                firstTypeDeclaration.addFieldDeclaration(firstField);

                secondField = JavaFieldDeclaration
                        .field(firstTypeDeclaration.getName().toLowerCase() + "s")
                        .returnGenerics(firstTypeDeclaration.getName())
                        .modifiers(PRIVATE)
                        .returning("java.util.List");
                secondField.annotations().add(ClassName.of("jakarta.persistence.OneToMany"), builder -> builder.set("mappedBy", secondTypeDeclaration.getName().toLowerCase()));

                secondTypeDeclaration.addFieldDeclaration(secondField);
            }
            case MANY_TO_MANY -> {
                firstField = JavaFieldDeclaration
                        .field(secondTypeDeclaration.getName().toLowerCase() + "s")
                        .returnGenerics(secondTypeDeclaration.getName())
                        .modifiers(PRIVATE)
                        .returning("java.util.List");
                firstField.annotations().add(ClassName.of("jakarta.persistence.ManyToMany"));
                firstTypeDeclaration.addFieldDeclaration(firstField);

                secondField = JavaFieldDeclaration
                        .field(firstTypeDeclaration.getName().toLowerCase() + "s")
                        .returnGenerics(firstTypeDeclaration.getName())
                        .modifiers(PRIVATE)
                        .returning("java.util.List");
                secondField.annotations().add(ClassName.of("jakarta.persistence.ManyToMany"));
                secondTypeDeclaration.addFieldDeclaration(secondField);
            }
        }
    }

    private void generateDomainClasses(DomainClassDescription domainClassDescription, S sourceCode) {
        JavaCompilationUnit domainClassCompilationUnit = (JavaCompilationUnit) sourceCode.createCompilationUnit(this.description.getPackageName() + ".domain", domainClassDescription.getClassName());
        JavaTypeDeclaration domainClassTypeDeclaration = domainClassCompilationUnit.createTypeDeclaration(domainClassDescription.getClassName());
        domainClassTypeDeclaration.modifiers(PUBLIC);
        domainClassTypeDeclaration.annotations().add(ClassName.of("jakarta.persistence.Entity"));
        domainClassTypeDeclaration.annotations().add(ClassName.of("jakarta.persistence.Table"),
                (annotation) -> annotation.add("name", domainClassDescription.getClassName().toLowerCase() + "s"));
        domainClassTypeDeclaration.annotations().add(ClassName.of("lombok.Data"));
        generateFields(domainClassDescription, domainClassTypeDeclaration);
        generateNoArgsConstructor(domainClassTypeDeclaration);
        generateGetters(domainClassDescription, domainClassTypeDeclaration);
        generateSetters(domainClassDescription, domainClassTypeDeclaration);
    }

    private void generateAllArgsConstructor(JavaTypeDeclaration domainClassTypeDeclaration) {
        JavaMethodDeclaration allArgsConstructor = JavaMethodDeclaration
                .method("")
                .modifiers(PUBLIC)
                .returning(domainClassTypeDeclaration.getName())
                .body(CodeBlock.of("  "));
        domainClassTypeDeclaration.addMethodDeclaration(allArgsConstructor);
    }

    private void generateNoArgsConstructor(JavaTypeDeclaration domainClassTypeDeclaration) {
        JavaMethodDeclaration noArgsConstructor = JavaMethodDeclaration
                .method("")
                .modifiers(PUBLIC)
                .returning(domainClassTypeDeclaration.getName())
                .body(CodeBlock.of(""));
        domainClassTypeDeclaration.addMethodDeclaration(noArgsConstructor);
    }

    private void generateSetters(DomainClassDescription domainClassDescription, JavaTypeDeclaration domainClassTypeDeclaration) {
        for (FieldDescription field : domainClassDescription.getFields()) {
            JavaMethodDeclaration fieldSetter = JavaMethodDeclaration
                    .method("set" + capitalize(field.getFieldName()))
                    .modifiers(PUBLIC)
                    .returning("void")
                    .parameters(Parameter.of(field.getFieldName(), field.getClassType()))
                    .body(CodeBlock.of("this." + field.getFieldName() + " = " + field.getFieldName() + ";"));

            domainClassTypeDeclaration.addMethodDeclaration(fieldSetter);
        }
    }

    private void generateGetters(DomainClassDescription domainClassDescription, JavaTypeDeclaration domainClassTypeDeclaration) {
        for (FieldDescription field : domainClassDescription.getFields()) {
            JavaMethodDeclaration fieldGetter = JavaMethodDeclaration
                    .method("get" + capitalize(field.getFieldName()))
                    .modifiers(PUBLIC)
                    .returning(field.getClassType())
                    .body(CodeBlock.of("return this." + field.getFieldName() + ";"));

            domainClassTypeDeclaration.addMethodDeclaration(fieldGetter);
        }
    }

    private void generateFields(DomainClassDescription domainClassDescription, JavaTypeDeclaration domainClassTypeDeclaration) {
        for (FieldDescription fieldDescription : domainClassDescription.getFields()) {
            JavaFieldDeclaration fieldDeclaration = JavaFieldDeclaration
                    .field(fieldDescription.getFieldName())
                    .modifiers(PRIVATE)
                    .returning(fieldDescription.getClassType());

            if (fieldDescription.getFieldName().equals("id")) {
                fieldDeclaration.annotations().add(ClassName.of("jakarta.persistence.Id"));
                fieldDeclaration.annotations().add(ClassName.of("jakarta.persistence.GeneratedValue"));
            }

            domainClassTypeDeclaration.addFieldDeclaration(fieldDeclaration);
        }
    }

    private String capitalize(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    @Override
    public int getOrder() {
        return ProjectContributor.super.getOrder();
    }

    public List<DomainClassDescription> getDomainClassDescriptions() {
        return domainClassDescriptions;
    }

    public void setDomainClassDescriptions(List<DomainClassDescription> domainClassDescriptions) {
        this.domainClassDescriptions = domainClassDescriptions;
    }
}
