package io.spring.initializr.generator.spring.code.java;

import io.spring.initializr.generator.language.*;
import io.spring.initializr.generator.language.java.JavaCompilationUnit;
import io.spring.initializr.generator.language.java.JavaFieldDeclaration;
import io.spring.initializr.generator.language.java.JavaMethodDeclaration;
import io.spring.initializr.generator.language.java.JavaTypeDeclaration;
import io.spring.initializr.generator.project.DomainClassDescription;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.contributor.ProjectContributor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.reflect.Modifier.*;
import static org.atteo.evo.inflector.English.plural;

public class McpServiceSourceCodeContributor<T extends TypeDeclaration, C extends CompilationUnit<T>, S extends SourceCode<T, C>> implements ProjectContributor {
    private List<DomainClassDescription> domainClassDescriptions = new ArrayList<>();
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
//            addCreateEntityMethod(domainClassName, mcpServiceTypeDeclaration);
//            addUpdateEntityMethod(domainClassDescription, mcpServiceTypeDeclaration);
//            addDeleteEntityMethod(domainClassDescription, mcpServiceTypeDeclaration);
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
        CodeBlock code = CodeBlock.ofStatement("return $LService.getAll$Ls().toString();", domainClassName.toLowerCase(), domainClassName);
        JavaMethodDeclaration getEntityByIdMethodDeclaration = JavaMethodDeclaration
                .method("getAll" + plural(domainClassName))
                .modifiers(PUBLIC)
                .returning("String")
                .body(code);
        getEntityByIdMethodDeclaration.annotations().add(ClassName.of("org.springaicommunity.mcp.annotation.McpTool"), (annotations) -> annotations.add("name","get-" + plural(domainClassName.toLowerCase())).add("description", "returns all " + plural(domainClassName.toLowerCase()) + " in the database"));
        mcpServiceTypeDeclaration.addMethodDeclaration(getEntityByIdMethodDeclaration);
    }
    private void addGetEntityByIdMethod(DomainClassDescription domainClassDescription, JavaTypeDeclaration restControllerTypeDeclaration) {
        String domainClassName = domainClassDescription.getClassName();
        CodeBlock code = CodeBlock.ofStatement("return $LService.get$LById(id).toString();", domainClassName.toLowerCase(), domainClassName);
        JavaMethodDeclaration getEntityByIdMethodDeclaration = JavaMethodDeclaration
                .method("get" + domainClassName + "ById")
                .modifiers(PUBLIC)
                .returning("String")
                .parameters(
                        Parameter.builder("id")
                                .type(domainClassDescription.getPrimaryKeyField().getClassType())
                                .annotate(ClassName.of("org.springaicommunity.mcp.annotation.McpToolParam"),
                                        (annotations) -> annotations.add("description", domainClassName.toLowerCase() + "'s " + domainClassDescription.getPrimaryKeyField().getFieldName()))
                                .build()
                )
                .body(code);
        getEntityByIdMethodDeclaration.annotations().add(ClassName.of("org.springaicommunity.mcp.annotation.McpTool"), (annotations) -> annotations.add("name","get-" + domainClassName.toLowerCase() + "-by-id").add("description", "returns a " + domainClassName.toLowerCase() + " by Id"));
        restControllerTypeDeclaration.addMethodDeclaration(getEntityByIdMethodDeclaration);
    }
}
