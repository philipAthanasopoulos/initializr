package io.spring.initializr.generator.spring.code.java;

import io.spring.initializr.generator.language.*;
import io.spring.initializr.generator.language.java.JavaCompilationUnit;
import io.spring.initializr.generator.language.java.JavaFieldDeclaration;
import io.spring.initializr.generator.language.java.JavaMethodDeclaration;
import io.spring.initializr.generator.language.java.JavaTypeDeclaration;
import io.spring.initializr.generator.project.DomainClassDescription;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.contributor.ProjectContributor;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.reflect.Modifier.*;
import static org.atteo.evo.inflector.English.plural;

public class McpServiceSourceCodeContributor<T extends TypeDeclaration, C extends CompilationUnit<T>, S extends SourceCode<T, C>> implements ProjectContributor {
    private final List<DomainClassDescription> domainClassDescriptions;
    private final SourceCodeWriter<S> sourceCodeWriter;
    private final Supplier<S> sourceFactory;
    private final ProjectDescription description;

    public McpServiceSourceCodeContributor(SourceCodeWriter<S> sourceCodeWriter, Supplier<S> sourceFactory, ProjectDescription description) {
        this.sourceCodeWriter = sourceCodeWriter;
        this.sourceFactory = sourceFactory;
        this.description = description;
        this.domainClassDescriptions = description.getDomainClassDescriptions();
    }


    @Override
    public void contribute(Path projectRoot) throws IOException {
        S sourceCode = this.sourceFactory.get();

        for (DomainClassDescription domainClassDescription : domainClassDescriptions) {
            JavaCompilationUnit mcpServiceCompilationUnit = (JavaCompilationUnit) sourceCode.
                    createCompilationUnit(this.description.getPackageName() + ".mcpServices", domainClassDescription.getClassName() + "McpService");
            JavaTypeDeclaration mcpServiceTypeDeclaration = mcpServiceCompilationUnit.createTypeDeclaration(domainClassDescription.getClassName() + "McpService");
            mcpServiceTypeDeclaration.modifiers(PUBLIC);
            mcpServiceTypeDeclaration.annotations().add(ClassName.of("org.springframework.stereotype.Component"));

            String domainClassName = domainClassDescription.getClassName();
            String domainClassServiceName = domainClassName.toLowerCase() + "Service";
            JavaFieldDeclaration serviceFieldDeclaration = addEntityServiceField(domainClassServiceName, domainClassName, mcpServiceTypeDeclaration);

            addAutowiredConstructor(mcpServiceTypeDeclaration, serviceFieldDeclaration, domainClassServiceName);
            addFindAllMethod(domainClassName, mcpServiceTypeDeclaration);
            addGetEntityByIdMethod(domainClassDescription, mcpServiceTypeDeclaration);
            addCreateEntityMethod(domainClassDescription, mcpServiceTypeDeclaration);
//            addUpdateEntityMethod(domainClassDescription, mcpServiceTypeDeclaration);
            addDeleteEntityByIdMethod(domainClassDescription, mcpServiceTypeDeclaration);
        }

        this.sourceCodeWriter.writeTo(
                this.description.getBuildSystem().getMainSource(projectRoot, this.description.getLanguage()),
                sourceCode);
    }

    private JavaFieldDeclaration addEntityServiceField(String domainClassServiceName, String domainClassName, JavaTypeDeclaration mcpServiceTypeDeclaration) {
        JavaFieldDeclaration serviceFieldDeclaration = JavaFieldDeclaration
                .field(domainClassServiceName)
                .modifiers(PRIVATE | FINAL)
                .returning(this.description.getPackageName() + ".services." + domainClassName + "Service");
        mcpServiceTypeDeclaration.addFieldDeclaration(serviceFieldDeclaration);
        return serviceFieldDeclaration;
    }

    private void addAutowiredConstructor(JavaTypeDeclaration mcpServiceTypeDeclaration, JavaFieldDeclaration serviceFieldDeclaration, String domainClassServiceName) {
        CodeBlock code = CodeBlock.ofStatement("this.$L = $L", domainClassServiceName, serviceFieldDeclaration.getName());
        JavaMethodDeclaration autowiredConstructorDeclaration = JavaMethodDeclaration
                .method("")
                .modifiers(PUBLIC)
                .returning(mcpServiceTypeDeclaration.getName())
                .parameters(Parameter.of(serviceFieldDeclaration.getName(), serviceFieldDeclaration.getReturnType()))
                .body(code);

        mcpServiceTypeDeclaration.addMethodDeclaration(autowiredConstructorDeclaration);
    }

    private void addFindAllMethod(String domainClassName, JavaTypeDeclaration mcpServiceTypeDeclaration) {
        CodeBlock code = CodeBlock.ofStatement("return $LService.getAll$Ls().toString()", domainClassName.toLowerCase(), domainClassName);
        JavaMethodDeclaration getEntityByIdMethodDeclaration = JavaMethodDeclaration
                .method("getAll" + plural(domainClassName))
                .modifiers(PUBLIC)
                .returning("String")
                .body(code);
        getEntityByIdMethodDeclaration.annotations().add(ClassName.of("org.springframework.ai.mcp.annotation.McpTool"), (annotations) -> annotations.add("name", "get-" + plural(domainClassName.toLowerCase())).add("description", "returns all " + plural(domainClassName.toLowerCase()) + " in the database"));
        mcpServiceTypeDeclaration.addMethodDeclaration(getEntityByIdMethodDeclaration);
    }

    private void addGetEntityByIdMethod(DomainClassDescription domainClassDescription, JavaTypeDeclaration restControllerTypeDeclaration) {
        String domainClassName = domainClassDescription.getClassName();
        CodeBlock code = CodeBlock.ofStatement("return $LService.get$LById(id).toString()", domainClassName.toLowerCase(), domainClassName);
        JavaMethodDeclaration getEntityByIdMethodDeclaration = JavaMethodDeclaration
                .method("get" + domainClassName + "ById")
                .modifiers(PUBLIC)
                .returning("String")
                .parameters(
                        Parameter.builder("id")
                                .type(domainClassDescription.getPrimaryKeyField().getClassType())
                                .annotate(ClassName.of("org.springframework.ai.mcp.annotation.McpToolParam"),
                                        (annotations) -> annotations.add("description", domainClassName.toLowerCase() + "'s " + domainClassDescription.getPrimaryKeyField().getFieldName()))
                                .build()
                )
                .body(code);
        getEntityByIdMethodDeclaration.annotations().add(ClassName.of("org.springframework.ai.mcp.annotation.McpTool"), (annotations) -> annotations.add("name", "get-" + domainClassName.toLowerCase() + "-by-id").add("description", "returns a " + domainClassName.toLowerCase() + " by Id"));
        restControllerTypeDeclaration.addMethodDeclaration(getEntityByIdMethodDeclaration);
    }

    private void addDeleteEntityByIdMethod(DomainClassDescription domainClassDescription, JavaTypeDeclaration mcpServiceTypeDeclaration) {
        String domainClassName = domainClassDescription.getClassName();
        CodeBlock code = CodeBlock.of(
                """
                        Optional<$L> $L = Optional.ofNullable($LService.get$LById(id));
                        if ($L.isPresent()) {
                            if ($L.get().get$L().equals($L)) {
                                $LService.delete$LById(id);
                                return \"$L deleted successfully\";
                            } else {
                                return \"$L details do not match\";
                            }
                        } else {
                            return \"$L not found\";
                        }
                        """,
                domainClassName,
                "optional" + domainClassName,
                domainClassName.toLowerCase(),
                domainClassName,
                "optional" + domainClassName,
                "optional" + domainClassName,
                StringUtils.capitalize(domainClassDescription.getPrimaryKeyField().getFieldName()),
                domainClassDescription.getPrimaryKeyField().getFieldName(),
                domainClassName.toLowerCase(),
                domainClassName,
                domainClassName,
                domainClassName,
                domainClassName);
        JavaMethodDeclaration deleteEntityMethodDeclaration = JavaMethodDeclaration
                .method("delete" + domainClassDescription.getClassName() + "ById")
                .modifiers(PUBLIC)
                .returning("String")
                .parameters(
                        Parameter.builder("id")
                                .type(domainClassDescription.getPrimaryKeyField().getClassType())
                                .annotate(ClassName.of("org.springframework.ai.mcp.annotation.McpToolParam"),
                                        (annotations) -> annotations.add("description", domainClassDescription.getClassName().toLowerCase() + "'s " + domainClassDescription.getPrimaryKeyField().getFieldName()))
                                .build()
                )
                .body(code);
        deleteEntityMethodDeclaration.annotations().add(ClassName
                .of("org.springframework.ai.mcp.annotation.McpTool"), (annotations) -> annotations.add("name", "delete-" + domainClassDescription.getClassName().toLowerCase() + "-by-id").add("description", "deletes a " + domainClassDescription.getClassName().toLowerCase() + " by id"));
        mcpServiceTypeDeclaration.addMethodDeclaration(deleteEntityMethodDeclaration);
    }

    private void addCreateEntityMethod(DomainClassDescription domainClassDescription, JavaTypeDeclaration mcpServiceTypeDeclaration) {
        String domainClassName = domainClassDescription.getClassName();

        String entityArgumentsSetCodeBlock = domainClassDescription.getFields().stream()
                .skip(1)
                .map(field -> domainClassName.toLowerCase() + ".set" + StringUtils.capitalize(field.getFieldName()) + "(" + field.getFieldName() + ");")
                .reduce("", (a, b) -> a + b + "\n");


        CodeBlock code = CodeBlock.of("""
                        $L $L = new $L();
                        $L
                        $LService.save$L($L);
                        return "$L created successfully";
                        """,
                domainClassName,
                domainClassName.toLowerCase(),
                domainClassName,
                entityArgumentsSetCodeBlock,
                domainClassName.toLowerCase(),
                domainClassName,
                domainClassName.toLowerCase(),
                domainClassName);

        JavaMethodDeclaration createEntityMethodDeclaration = JavaMethodDeclaration
                .method("create" + domainClassName)
                .modifiers(PUBLIC)
                .returning("String")
                .parameters(domainClassDescription.getFields().stream()
                        .skip(1)
                        .map(
                                field -> Parameter.builder(field.getFieldName())
                                        .type(field.getClassType())
                                        .annotate(ClassName.of("org.springframework.ai.mcp.annotation.McpToolParam"))
                                        .build()
                        )
                        .toArray(Parameter[]::new))

                .body(code);


        createEntityMethodDeclaration.annotations()
                .add(ClassName.of("org.springframework.ai.mcp.annotation.McpTool"),
                        (annotations) -> annotations
                                .add("name", "create-" + domainClassDescription.getClassName().toLowerCase())
                                .add("description", "creates a " + domainClassDescription.getClassName().toLowerCase())
                );

        mcpServiceTypeDeclaration.addMethodDeclaration(createEntityMethodDeclaration);
    }
}
