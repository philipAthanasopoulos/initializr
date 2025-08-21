package io.spring.initializr.generator.spring.code.java;

import io.spring.initializr.generator.language.*;
import io.spring.initializr.generator.language.java.JavaCompilationUnit;
import io.spring.initializr.generator.language.java.JavaFieldDeclaration;
import io.spring.initializr.generator.language.java.JavaMethodDeclaration;
import io.spring.initializr.generator.language.java.JavaTypeDeclaration;
import io.spring.initializr.generator.project.AssociationDescription;
import io.spring.initializr.generator.project.DomainClassDescription;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.contributor.ProjectContributor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.reflect.Modifier.*;

public class RestControllerSourceCodeContributor<T extends TypeDeclaration, C extends CompilationUnit<T>, S extends SourceCode<T, C>> implements ProjectContributor {

    public List<DomainClassDescription> getDomainClassDescriptions() {
        return domainClassDescriptions;
    }

    private List<DomainClassDescription> domainClassDescriptions = new ArrayList<>();
    private List<AssociationDescription> associationDescriptions = new ArrayList<>();
    private final SourceCodeWriter<S> sourceCodeWriter;
    private final Supplier<S> sourceFactory;
    private final ProjectDescription description;


    public RestControllerSourceCodeContributor(SourceCodeWriter<S> sourceCodeWriter, Supplier<S> sourceFactory, ProjectDescription description) {
        this.sourceCodeWriter = sourceCodeWriter;
        this.sourceFactory = sourceFactory;
        this.description = description;
        this.domainClassDescriptions = description.getDomainClassDescriptions();
        this.associationDescriptions = description.getAssotiationDescriptions();
    }

    @Override
    public void contribute(Path projectRoot) throws IOException {
        S sourceCode = this.sourceFactory.get();

        for (DomainClassDescription domainClassDescription : domainClassDescriptions) {
            if (domainClassDescription.isGenerateRestController()) {
                String domainClassName = domainClassDescription.getClassName();

                JavaCompilationUnit restControllerCompilationUnit = (JavaCompilationUnit) sourceCode.createCompilationUnit(this.description.getPackageName() + ".controllers.api", domainClassName + "Controller");
                JavaTypeDeclaration restControllerTypeDeclaration = restControllerCompilationUnit.createTypeDeclaration(domainClassName + "Controller");
                restControllerTypeDeclaration.modifiers(PUBLIC);
                restControllerTypeDeclaration.annotations().add(ClassName.of("org.springframework.web.bind.annotation.RestController"));
                restControllerTypeDeclaration.annotations().add(ClassName.of("org.springframework.web.bind.annotation.RequestMapping"),
                        (annotation) -> annotation.add("value", "/api/" + domainClassName.toLowerCase() + "s"));

                String domainClassServiceName = domainClassName.toLowerCase() + "Service";

                JavaFieldDeclaration serviceFieldDeclaration = addEntityServiceField(domainClassServiceName, domainClassName, restControllerTypeDeclaration);

                addAutowiredConstructor(restControllerTypeDeclaration, serviceFieldDeclaration, domainClassServiceName);
                addFindAllMethod(domainClassName, restControllerTypeDeclaration);
                addGetEntityByIdMethod(domainClassName, restControllerTypeDeclaration);
                addCreateEntityMethod(domainClassName, restControllerTypeDeclaration);
                addUpdateEntityMethod(domainClassName, restControllerTypeDeclaration);
                addDeleteEntityMethod(domainClassName, restControllerTypeDeclaration);

                List<AssociationDescription> associationsDescirptionsOfGivenDomain = this.associationDescriptions.stream()
                        .filter(association -> domainClassName.equals(association.getFirstClassName()) || domainClassName.equals(association.getSecondClassName()))
                        .toList();

                for (AssociationDescription associationDescription : associationsDescirptionsOfGivenDomain) {
                    //TODO
                }
            }


        }

        this.sourceCodeWriter.writeTo(
                this.description.getBuildSystem().getMainSource(projectRoot, this.description.getLanguage()),
                sourceCode);
    }

    private void addFindAllMethod(String domainClassName, JavaTypeDeclaration restControllerTypeDeclaration) {
        CodeBlock code = CodeBlock.ofStatement("return $LService.getAll$Ls()", domainClassName.toLowerCase(), domainClassName);
        JavaMethodDeclaration getEntityByIdMethodDeclaration = JavaMethodDeclaration
                .method("getAll" + domainClassName + "s")
                .modifiers(PUBLIC)
                .returning("java.util.List")
                .returnGenerics(domainClassName)
                .body(code);
        getEntityByIdMethodDeclaration.annotations().add(ClassName.of("org.springframework.web.bind.annotation.GetMapping"));
        restControllerTypeDeclaration.addMethodDeclaration(getEntityByIdMethodDeclaration);
    }

    private JavaFieldDeclaration addEntityServiceField(String domainClassServiceName, String domainClassName, JavaTypeDeclaration restControllerTypeDeclaration) {
        JavaFieldDeclaration serviceFieldDeclaration = JavaFieldDeclaration
                .field(domainClassServiceName)
                .modifiers(PRIVATE | FINAL)
                .returning(this.description.getPackageName() + ".services." + domainClassName + "Service");
        restControllerTypeDeclaration.addFieldDeclaration(serviceFieldDeclaration);
        return serviceFieldDeclaration;
    }

    private void addAutowiredConstructor(JavaTypeDeclaration restControllerTypeDeclaration, JavaFieldDeclaration serviceFieldDeclaration, String domainClassServiceName) {
        CodeBlock code = CodeBlock.ofStatement("this.$L = $L", domainClassServiceName, serviceFieldDeclaration.getName());
        JavaMethodDeclaration autowiredConstructorDeclaration = JavaMethodDeclaration
                .method("")
                .modifiers(PUBLIC)
                .returning(restControllerTypeDeclaration.getName())
                .parameters(Parameter.of(serviceFieldDeclaration.getName(), serviceFieldDeclaration.getReturnType()))
                .body(code);

        restControllerTypeDeclaration.addMethodDeclaration(autowiredConstructorDeclaration);
    }

    private void addGetEntityByIdMethod(String domainClassName, JavaTypeDeclaration restControllerTypeDeclaration) {
        CodeBlock code = CodeBlock.ofStatement("return $LService.get$LById(id)", domainClassName.toLowerCase(), domainClassName);
        JavaMethodDeclaration getEntityByIdMethodDeclaration = JavaMethodDeclaration
                .method("get" + domainClassName + "ById")
                .modifiers(PUBLIC)
                .returning(this.description.getPackageName() + ".domain." + domainClassName)
                .parameters(
                        Parameter.builder("id")
                                .type("java.lang.Long")
                                .annotate(ClassName.of("org.springframework.web.bind.annotation.PathVariable"))
                                .build()
                )
                .body(code);
        getEntityByIdMethodDeclaration.annotations().add(ClassName.of("org.springframework.web.bind.annotation.GetMapping"),
                (annotation) -> annotation.add("value", "/{id}"));
        restControllerTypeDeclaration.addMethodDeclaration(getEntityByIdMethodDeclaration);
    }

    private void addCreateEntityMethod(String domainClassName, JavaTypeDeclaration restControllerTypeDeclaration) {
        CodeBlock code = CodeBlock.ofStatement("return $LService.save$L($L)", domainClassName.toLowerCase(), domainClassName, domainClassName.toLowerCase());
        JavaMethodDeclaration getEntityByIdMethodDeclaration = JavaMethodDeclaration
                .method("create" + domainClassName)
                .modifiers(PUBLIC)
                .returning(this.description.getPackageName() + ".domain." + domainClassName)
                .parameters(
                        Parameter.builder(domainClassName.toLowerCase())
                                .type(this.description.getPackageName() + ".domain." + domainClassName)
                                .annotate(ClassName.of("org.springframework.web.bind.annotation.RequestBody"))
                                .build()
                )
                .body(code);
        getEntityByIdMethodDeclaration.annotations().add(ClassName.of("org.springframework.web.bind.annotation.PostMapping"));
        restControllerTypeDeclaration.addMethodDeclaration(getEntityByIdMethodDeclaration);
    }

    private void addUpdateEntityMethod(String domainClassName, JavaTypeDeclaration restControllerTypeDeclaration) {
        CodeBlock code = CodeBlock.ofStatement("return $LService.update$L($L, id)", domainClassName.toLowerCase(), domainClassName, domainClassName.toLowerCase());
        JavaMethodDeclaration getEntityByIdMethodDeclaration = JavaMethodDeclaration
                .method("update" + domainClassName)
                .modifiers(PUBLIC)
                .returning(this.description.getPackageName() + ".domain." + domainClassName)
                .parameters(
                        Parameter.builder(domainClassName.toLowerCase())
                                .type(this.description.getPackageName() + ".domain." + domainClassName)
                                .annotate(ClassName.of("org.springframework.web.bind.annotation.RequestBody"))
                                .build(),

                        Parameter.builder("id")
                                .type("java.lang.Long")
                                .annotate(ClassName.of("org.springframework.web.bind.annotation.PathVariable"))
                                .build()
                )
                .body(code);
        getEntityByIdMethodDeclaration.annotations().add(ClassName.of("org.springframework.web.bind.annotation.PutMapping"),
                (annotation) -> annotation.add("value", "/{id}"));
        restControllerTypeDeclaration.addMethodDeclaration(getEntityByIdMethodDeclaration);
    }

    private void addDeleteEntityMethod(String domainClassName, JavaTypeDeclaration restControllerTypeDeclaration) {
        CodeBlock code = CodeBlock.ofStatement("$LService.delete$LById(id)", domainClassName.toLowerCase(), domainClassName);
        JavaMethodDeclaration getEntityByIdMethodDeclaration = JavaMethodDeclaration
                .method("delete" + domainClassName + "ById")
                .modifiers(PUBLIC)
                .returning("void")
                .parameters(
                        Parameter.builder("id")
                                .type("java.lang.Long")
                                .annotate(ClassName.of("org.springframework.web.bind.annotation.PathVariable"))
                                .build()
                )
                .body(code);
        getEntityByIdMethodDeclaration.annotations().add(ClassName.of("org.springframework.web.bind.annotation.DeleteMapping"),
                (annotation) -> annotation.add("value", "/{id}"));
        restControllerTypeDeclaration.addMethodDeclaration(getEntityByIdMethodDeclaration);
    }

    @Override
    public int getOrder() {
        return ProjectContributor.super.getOrder();
    }

}
