package io.spring.initializr.generator.project.contributor;

import io.spring.initializr.generator.language.*;
import io.spring.initializr.generator.language.java.JavaCompilationUnit;
import io.spring.initializr.generator.language.java.JavaFieldDeclaration;
import io.spring.initializr.generator.language.java.JavaMethodDeclaration;
import io.spring.initializr.generator.language.java.JavaTypeDeclaration;
import io.spring.initializr.generator.project.DomainClassDescription;
import io.spring.initializr.generator.project.ProjectDescription;

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

    public void setDomainClassDescriptions(List<DomainClassDescription> domainClassDescriptions) {
        this.domainClassDescriptions = domainClassDescriptions;
    }

    private List<DomainClassDescription> domainClassDescriptions = new ArrayList<>();
    private final SourceCodeWriter<S> sourceCodeWriter;
    private final Supplier<S> sourceFactory;
    private final ProjectDescription description;


    public RestControllerSourceCodeContributor(SourceCodeWriter<S> sourceCodeWriter, Supplier<S> sourceFactory, ProjectDescription description) {
        this.sourceCodeWriter = sourceCodeWriter;
        this.sourceFactory = sourceFactory;
        this.description = description;
        this.domainClassDescriptions = description.getDomainClassDescriptions();
    }

    @Override
    public void contribute(Path projectRoot) throws IOException {
        S sourceCode = this.sourceFactory.get();

        for (DomainClassDescription domainClassDescription : domainClassDescriptions) {
            String domainClassName = domainClassDescription.getClassName();

            JavaCompilationUnit restControllerCompilationUnit = (JavaCompilationUnit) sourceCode.createCompilationUnit(this.description.getPackageName() + ".controllers", domainClassName + "Controller");
            JavaTypeDeclaration restControllerTypeDeclaration = restControllerCompilationUnit.createTypeDeclaration(domainClassName + "Controller");
            restControllerTypeDeclaration.modifiers(PUBLIC);
            restControllerTypeDeclaration.annotations().add(ClassName.of("org.springframework.web.bind.annotation.RestController"));
            restControllerTypeDeclaration.annotations().add(ClassName.of("org.springframework.web.bind.annotation.RequestMapping"),
                    (annotation) -> annotation.add("value", "/" + domainClassName.toLowerCase() + "s"));

            String domainClassServiceName = domainClassName.toLowerCase() + "Service";

            JavaFieldDeclaration serviceFieldDeclaration = addEntityServiceField(domainClassServiceName, domainClassName, restControllerTypeDeclaration);

            addAutowiredConstructor(restControllerTypeDeclaration, serviceFieldDeclaration, domainClassServiceName);

            addGetEntityByIdMethod(domainClassName, restControllerTypeDeclaration);

            addCreateEntityMethod(domainClassName, restControllerTypeDeclaration);

            addDeleteEntityMethod(domainClassName, restControllerTypeDeclaration);
        }

        this.sourceCodeWriter.writeTo(
                this.description.getBuildSystem().getMainSource(projectRoot, this.description.getLanguage()),
                sourceCode);
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
        JavaMethodDeclaration autowiredConstructorDeclaration = JavaMethodDeclaration
                .method("")
                .modifiers(PUBLIC)
                .returning(restControllerTypeDeclaration.getName())
                .parameters(Parameter.of(serviceFieldDeclaration.getName(), serviceFieldDeclaration.getReturnType()))
                .body(CodeBlock.ofStatement("this." + domainClassServiceName + " = " + serviceFieldDeclaration.getName()));

        restControllerTypeDeclaration.addMethodDeclaration(autowiredConstructorDeclaration);
    }

    private void addGetEntityByIdMethod(String domainClassName, JavaTypeDeclaration restControllerTypeDeclaration) {
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
                .body(CodeBlock.ofStatement("return " + domainClassName.toLowerCase() + "Service.get" + domainClassName + "ById(id)"));
        getEntityByIdMethodDeclaration.annotations().add(ClassName.of("org.springframework.web.bind.annotation.GetMapping"),
                (annotation) -> annotation.add("value", "/{id}"));
        restControllerTypeDeclaration.addMethodDeclaration(getEntityByIdMethodDeclaration);
    }

    private void addCreateEntityMethod(String domainClassName, JavaTypeDeclaration restControllerTypeDeclaration) {
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
                .body(CodeBlock.ofStatement("return " + domainClassName.toLowerCase() + "Service.save" + domainClassName + "(" + domainClassName.toLowerCase() + ")"));
        getEntityByIdMethodDeclaration.annotations().add(ClassName.of("org.springframework.web.bind.annotation.PostMapping"));
        restControllerTypeDeclaration.addMethodDeclaration(getEntityByIdMethodDeclaration);
    }

    private void addDeleteEntityMethod(String domainClassName, JavaTypeDeclaration restControllerTypeDeclaration) {
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
                .body(CodeBlock.ofStatement(domainClassName.toLowerCase() + "Service.delete" + domainClassName  + "ById(id)"));
        getEntityByIdMethodDeclaration.annotations().add(ClassName.of("org.springframework.web.bind.annotation.DeleteMapping"),
                (annotation) -> annotation.add("value", "/{id}"));
        restControllerTypeDeclaration.addMethodDeclaration(getEntityByIdMethodDeclaration);
    }

    @Override
    public int getOrder() {
        return ProjectContributor.super.getOrder();
    }

}
