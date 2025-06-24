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

public class FrontendControllerSourceCodeContributor<T extends TypeDeclaration, C extends CompilationUnit<T>, S extends SourceCode<T, C>> implements ProjectContributor {

    private List<DomainClassDescription> domainClassDescriptions = new ArrayList<>();
    private final SourceCodeWriter<S> sourceCodeWriter;
    private final Supplier<S> sourceFactory;
    private final ProjectDescription description;

    public FrontendControllerSourceCodeContributor(SourceCodeWriter<S> sourceCodeWriter, Supplier<S> sourceFactory, ProjectDescription description) {
        this.sourceCodeWriter = sourceCodeWriter;
        this.sourceFactory = sourceFactory;
        this.description = description;
        this.domainClassDescriptions = description.getDomainClassDescriptions();
    }

    @Override
    public void contribute(Path projectRoot) throws IOException {
        S sourceCode = this.sourceFactory.get();

        for (DomainClassDescription domainClassDescription : domainClassDescriptions) {
            if (domainClassDescription.isGenerateFrontendController())
                generateControllerClasses(domainClassDescription, sourceCode);
        }

        this.sourceCodeWriter.writeTo(
                this.description.getBuildSystem().getMainSource(projectRoot, this.description.getLanguage()),
                sourceCode);
    }

    private void generateControllerClasses(DomainClassDescription domainClassDescription, S sourceCode) {
        String domainClassName = domainClassDescription.getClassName();

        JavaCompilationUnit controllerCompilationUnit = (JavaCompilationUnit) sourceCode.createCompilationUnit(this.description.getPackageName() + ".controllers.web", domainClassDescription.getClassName() + "WebController");
        JavaTypeDeclaration controllerTypeDeclaration = controllerCompilationUnit.createTypeDeclaration(domainClassDescription.getClassName() + "WebController");
        controllerTypeDeclaration.modifiers(PUBLIC);
        controllerTypeDeclaration.annotations().add(ClassName.of("org.springframework.stereotype.Controller"));
        controllerTypeDeclaration.annotations().add(ClassName.of("org.springframework.web.bind.annotation.RequestMapping"),
                (annotation) -> annotation.add("value", "/" + domainClassName.toLowerCase() + "s"));
        String domainClassServiceName = domainClassName.toLowerCase() + "Service";

        JavaFieldDeclaration serviceFieldDeclaration = addEntityServiceField(domainClassServiceName, domainClassName, controllerTypeDeclaration);

        addAutowiredConstructor(controllerTypeDeclaration, serviceFieldDeclaration, domainClassServiceName);
        addListMethod(controllerTypeDeclaration, serviceFieldDeclaration, domainClassName);
        addAddMethod(controllerTypeDeclaration, domainClassName);
        addCreateMethod(controllerTypeDeclaration, serviceFieldDeclaration, domainClassName);
        addViewMethod(controllerTypeDeclaration, serviceFieldDeclaration, domainClassName);
        addEditMethod(controllerTypeDeclaration, serviceFieldDeclaration, domainClassName);
        addEditSubmitMethod(controllerTypeDeclaration, serviceFieldDeclaration, domainClassName);
        addDeleteMethod(controllerTypeDeclaration, serviceFieldDeclaration, domainClassName);
    }

    private void addCreateMethod(JavaTypeDeclaration controllerTypeDeclaration, JavaFieldDeclaration serviceFieldDeclaration, String domainClassName) {
        CodeBlock code = CodeBlock.builder()
                .addStatement("$L.save$L($L)", serviceFieldDeclaration.getName(), domainClassName, domainClassName.toLowerCase())
                .addStatement("return $S", "redirect:/" + domainClassName.toLowerCase() + "s")
                .build();
        JavaMethodDeclaration createMethod = JavaMethodDeclaration
                .method("create")
                .modifiers(PUBLIC)
                .parameters(
                        Parameter.builder(domainClassName.toLowerCase())
                                .type(this.description.getPackageName() + ".domain." + domainClassName)
                                .annotate(ClassName.of("org.springframework.web.bind.annotation.ModelAttribute"),
                                        (annotation) -> annotation.add("value", domainClassName.toLowerCase()))
                                .build()
                )

                .returning("java.lang.String")
                .body(code);

        createMethod.annotations().add(ClassName.of("org.springframework.web.bind.annotation.PostMapping"),
                (annotation) -> annotation.add("value", "/add"));
        controllerTypeDeclaration.addMethodDeclaration(createMethod);

    }

    private void addAddMethod(JavaTypeDeclaration controllerTypeDeclaration, String domainClassName) {
        CodeBlock code = CodeBlock.builder()
                .addStatement("return $S", domainClassName.toLowerCase() + "/add")
                .build();

        JavaMethodDeclaration listMethod = JavaMethodDeclaration
                .method("add")
                .modifiers(PUBLIC)
                .parameters(
                        Parameter.builder(domainClassName.toLowerCase())
                                .type(this.description.getPackageName() + ".domain." + domainClassName)
                                .annotate(ClassName.of("org.springframework.web.bind.annotation.ModelAttribute"),
                                        (annotation) -> annotation.add("value", domainClassName.toLowerCase()))
                                .build()
                )
                .returning("java.lang.String")
                .body(code);

        listMethod.annotations().add(ClassName.of("org.springframework.web.bind.annotation.GetMapping"),
                (annotation) -> annotation.add("value", "/add"));
        controllerTypeDeclaration.addMethodDeclaration(listMethod);
    }

    private void addListMethod(JavaTypeDeclaration controllerTypeDeclaration, JavaFieldDeclaration serviceFieldDeclaration, String domainClassName) {
        CodeBlock code = CodeBlock.builder()
                .addStatement("model.addAttribute($S, $L.getAll$Ls())", domainClassName.toLowerCase() + "s", serviceFieldDeclaration.getName(), domainClassName)
                .addStatement("return $S", domainClassName.toLowerCase() + "/list")
                .build();

        JavaMethodDeclaration listMethod = JavaMethodDeclaration
                .method("list")
                .modifiers(PUBLIC)
                .parameters(
                        Parameter.of("model", "org.springframework.ui.Model")
                )
                .returning("java.lang.String")
                .body(code);

        listMethod.annotations().add(ClassName.of("org.springframework.web.bind.annotation.GetMapping"));
        controllerTypeDeclaration.addMethodDeclaration(listMethod);
    }

    private void addEditMethod(JavaTypeDeclaration controllerTypeDeclaration, JavaFieldDeclaration serviceFieldDeclaration, String domainClassName) {
        CodeBlock code = CodeBlock.builder()
                .addStatement("model.addAttribute($S, $L.get$LById(id))", domainClassName.toLowerCase(), serviceFieldDeclaration.getName(), domainClassName)
                .addStatement("return $S", domainClassName.toLowerCase() + "/edit")
                .build();

        JavaMethodDeclaration listMethod = JavaMethodDeclaration
                .method("edit")
                .modifiers(PUBLIC)
                .parameters(
                        Parameter.of("model", "org.springframework.ui.Model"),
                        Parameter.builder("id")
                                .type("java.lang.Long")
                                .annotate(ClassName.of("org.springframework.web.bind.annotation.PathVariable"))
                                .build()

                )
                .returning("java.lang.String")
                .body(code);

        listMethod.annotations().add(ClassName.of("org.springframework.web.bind.annotation.GetMapping"),
                (annotation) -> annotation.add("value", "/edit/{id}"));
        controllerTypeDeclaration.addMethodDeclaration(listMethod);
    }

    private void addEditSubmitMethod(JavaTypeDeclaration controllerTypeDeclaration, JavaFieldDeclaration serviceFieldDeclaration, String domainClassName) {
        CodeBlock code = CodeBlock.builder()
                .addStatement("$L.update$L($L, id)", serviceFieldDeclaration.getName(), domainClassName, domainClassName.toLowerCase())
                .addStatement("return $S", "redirect:/" + domainClassName.toLowerCase() + "s")
                .build();
        JavaMethodDeclaration createMethod = JavaMethodDeclaration
                .method("submitEdit")
                .modifiers(PUBLIC)
                .parameters(
                        Parameter.builder(domainClassName.toLowerCase())
                                .type(this.description.getPackageName() + ".domain." + domainClassName)
                                .annotate(ClassName.of("org.springframework.web.bind.annotation.ModelAttribute"),
                                        (annotation) -> annotation.add("value", domainClassName.toLowerCase()))
                                .build(),
                        Parameter.builder("id")
                                .type("java.lang.Long")
                                .annotate(ClassName.of("org.springframework.web.bind.annotation.PathVariable"))
                                .build()
                )

                .returning("java.lang.String")
                .body(code);

        createMethod.annotations().add(ClassName.of("org.springframework.web.bind.annotation.PostMapping"),
                (annotation) -> annotation.add("value", "/edit/{id}"));
        controllerTypeDeclaration.addMethodDeclaration(createMethod);

    }


    private void addViewMethod(JavaTypeDeclaration controllerTypeDeclaration, JavaFieldDeclaration serviceFieldDeclaration, String domainClassName) {
        CodeBlock code = CodeBlock.builder()
                .addStatement("model.addAttribute($S, $L.get$LById(id))", domainClassName.toLowerCase(), serviceFieldDeclaration.getName(), domainClassName)
                .addStatement("return $S", domainClassName.toLowerCase() + "/view")
                .build();

        JavaMethodDeclaration listMethod = JavaMethodDeclaration
                .method("view")
                .modifiers(PUBLIC)
                .parameters(
                        Parameter.of("model", "org.springframework.ui.Model"),
                        Parameter.builder("id")
                                .type("java.lang.Long")
                                .annotate(ClassName.of("org.springframework.web.bind.annotation.PathVariable"))
                                .build()

                )
                .returning("java.lang.String")
                .body(code);

        listMethod.annotations().add(ClassName.of("org.springframework.web.bind.annotation.GetMapping"),
                (annotation) -> annotation.add("value", "/{id}"));
        controllerTypeDeclaration.addMethodDeclaration(listMethod);
    }

    private void addDeleteMethod(JavaTypeDeclaration controllerTypeDeclaration, JavaFieldDeclaration serviceFieldDeclaration, String domainClassName) {
        CodeBlock code = CodeBlock.builder()
                .addStatement("$L.delete$LById(id)", serviceFieldDeclaration.getName(), domainClassName)
                .addStatement("return \"redirect:/$Ls\"", domainClassName.toLowerCase())
                .build();

        JavaMethodDeclaration listMethod = JavaMethodDeclaration
                .method("delete")
                .modifiers(PUBLIC)
                .parameters(
                        Parameter.of("model", "org.springframework.ui.Model"),
                        Parameter.builder("id")
                                .type("java.lang.Long")
                                .annotate(ClassName.of("org.springframework.web.bind.annotation.PathVariable"))
                                .build()

                )
                .returning("java.lang.String")
                .body(code);

        listMethod.annotations().add(ClassName.of("org.springframework.web.bind.annotation.PostMapping"),
                (annotation) -> annotation.add("value", "/delete/{id}"));
        controllerTypeDeclaration.addMethodDeclaration(listMethod);
    }

    private void addAutowiredConstructor(JavaTypeDeclaration restControllerTypeDeclaration, JavaFieldDeclaration serviceFieldDeclaration, String domainClassServiceName) {
        CodeBlock code = CodeBlock.builder()
                .addStatement("this.$L = $L", domainClassServiceName, serviceFieldDeclaration.getName())
                .build();
        JavaMethodDeclaration autowiredConstructorDeclaration = JavaMethodDeclaration
                .method("")
                .modifiers(PUBLIC)
                .returning(restControllerTypeDeclaration.getName())
                .parameters(Parameter.of(serviceFieldDeclaration.getName(), serviceFieldDeclaration.getReturnType()))
                .body(code);

        restControllerTypeDeclaration.addMethodDeclaration(autowiredConstructorDeclaration);
    }

    private JavaFieldDeclaration addEntityServiceField(String domainClassServiceName, String domainClassName, JavaTypeDeclaration restControllerTypeDeclaration) {
        JavaFieldDeclaration serviceFieldDeclaration = JavaFieldDeclaration
                .field(domainClassServiceName)
                .modifiers(PRIVATE | FINAL)
                .returning(this.description.getPackageName() + ".services." + domainClassName + "Service");
        restControllerTypeDeclaration.addFieldDeclaration(serviceFieldDeclaration);
        return serviceFieldDeclaration;
    }


    @Override
    public int getOrder() {
        return ProjectContributor.super.getOrder();
    }
}
