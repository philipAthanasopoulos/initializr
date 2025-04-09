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
        System.out.println("Generated domain class contributor");
        this.sourceCodeWriter = sourceCodeWriter;
        this.sourceFactory = sourceFactory;
        this.description = description;
        this.domainClassDescriptions = description.getDomainClassDescriptions();
    }

    @Override
    public void contribute(Path projectRoot) throws IOException {
        System.out.println("Contributing domain classes");
        S sourceCode = this.sourceFactory.get();

        //generate domain classes
        for (DomainClassDescription domainClassDescription : domainClassDescriptions) {
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

        System.out.println(this.description.getAssotiationDescriptions().size());
        //add association fields
        for (AssociationDescription assotiationDescription : this.description.getAssotiationDescriptions()) {
            JavaCompilationUnit firstCompilationUnit = (JavaCompilationUnit) sourceCode.getCompilationUnits().stream()
                    .filter(unit -> unit.getName().equals(assotiationDescription.getFirstClassName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Compilation unit not found"));
            JavaTypeDeclaration firstTypeDeclaration = firstCompilationUnit.getTypeDeclarations().get(0);

            JavaCompilationUnit secondCompilationUnit = (JavaCompilationUnit) sourceCode.getCompilationUnits().stream()
                    .filter(unit -> unit.getName().equals(assotiationDescription.getSecondClassName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Compilation unit not found"));
            JavaTypeDeclaration secondTypeDeclaration = secondCompilationUnit.getTypeDeclarations().get(0);

            switch (assotiationDescription.getAssotiationType()) {
                case ONE_TO_ONE -> {
                    JavaFieldDeclaration firstField = JavaFieldDeclaration
                            .field(secondTypeDeclaration.getName().toLowerCase())
                            .returning(secondTypeDeclaration.getName());
                    firstField.annotations().add(ClassName.of("jakarta.persistence.OneToOne"));
                    firstTypeDeclaration.addFieldDeclaration(firstField);

                    JavaFieldDeclaration secondField = JavaFieldDeclaration
                            .field(firstTypeDeclaration.getName().toLowerCase())
                            .returning(firstTypeDeclaration.getName());
                    secondField.annotations().add(ClassName.of("jakarta.persistence.OneToOne"));
                    secondTypeDeclaration.addFieldDeclaration(secondField);
                }
                case ONE_TO_MANY -> {
                    JavaFieldDeclaration firstField = JavaFieldDeclaration
                            .field(secondTypeDeclaration.getName().toLowerCase() + "s")
                            .returnGenerics(secondTypeDeclaration.getName())
                            .returning("java.util.List");

                    firstField.annotations().add(ClassName.of("jakarta.persistence.OneToMany"));
                    firstTypeDeclaration.addFieldDeclaration(firstField);

                    JavaFieldDeclaration secondField = JavaFieldDeclaration
                            .field(firstTypeDeclaration.getName().toLowerCase())
                            .returning(firstTypeDeclaration.getName());
                    secondField.annotations().add(ClassName.of("jakarta.persistence.OneToOne"));
                    secondTypeDeclaration.addFieldDeclaration(secondField);
                }
                case MANY_TO_ONE -> {
                    JavaFieldDeclaration firstField = JavaFieldDeclaration
                            .field(secondTypeDeclaration.getName().toLowerCase())
                            .returning(secondTypeDeclaration.getName());

                    firstField.annotations().add(ClassName.of("jakarta.persistence.ManyToOne"));
                    firstTypeDeclaration.addFieldDeclaration(firstField);

                    JavaFieldDeclaration secondField = JavaFieldDeclaration
                            .field(firstTypeDeclaration.getName().toLowerCase() + "s")
                            .returnGenerics(firstTypeDeclaration.getName())
                            .returning("java.util.List");
                    secondField.annotations().add(ClassName.of("jakarta.persistence.OneToMany"));
                    secondTypeDeclaration.addFieldDeclaration(secondField);
                }
                case MANY_TO_MANY -> {
                    JavaFieldDeclaration firstField = JavaFieldDeclaration
                            .field(secondTypeDeclaration.getName().toLowerCase() + "s")
                            .returnGenerics(secondTypeDeclaration.getName())
                            .returning("java.util.List");
                    firstField.annotations().add(ClassName.of("jakarta.persistence.ManyToMany"));
                    firstTypeDeclaration.addFieldDeclaration(firstField);

                    JavaFieldDeclaration secondField = JavaFieldDeclaration
                            .field(firstTypeDeclaration.getName().toLowerCase() + "s")
                            .returnGenerics(firstTypeDeclaration.getName())
                            .returning("java.util.List");
                    secondField.annotations().add(ClassName.of("jakarta.persistence.ManyToMany"));
                    secondTypeDeclaration.addFieldDeclaration(secondField);
                }
            }
        }


        this.sourceCodeWriter.writeTo(
                this.description.getBuildSystem().getMainSource(projectRoot, this.description.getLanguage()),
                sourceCode);
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
