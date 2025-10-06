package io.spring.initializr.generator.spring.code.java;

import io.spring.initializr.generator.language.*;
import io.spring.initializr.generator.language.java.JavaCompilationUnit;
import io.spring.initializr.generator.language.java.JavaFieldDeclaration;
import io.spring.initializr.generator.language.java.JavaMethodDeclaration;
import io.spring.initializr.generator.language.java.JavaTypeDeclaration;
import io.spring.initializr.generator.project.AssociationDescription;
import io.spring.initializr.generator.project.DomainClassDescription;
import io.spring.initializr.generator.project.FieldDescription;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.contributor.ProjectContributor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.reflect.Modifier.*;

public class ServiceSourceCodeContributor<T extends TypeDeclaration, C extends CompilationUnit<T>, S extends SourceCode<T, C>> implements ProjectContributor {

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


    public ServiceSourceCodeContributor(SourceCodeWriter<S> sourceCodeWriter, Supplier<S> sourceFactory, ProjectDescription description) {
        this.sourceCodeWriter = sourceCodeWriter;
        this.sourceFactory = sourceFactory;
        this.description = description;
        this.domainClassDescriptions = description.getDomainClassDescriptions();
    }

    @Override
    public void contribute(Path projectRoot) throws IOException {
        S sourceCode = this.sourceFactory.get();

        for (DomainClassDescription domainClassDescription : domainClassDescriptions) {
            if (domainClassDescription.isGenerateFrontendController() || domainClassDescription.isGenerateRestController()) {
                JavaCompilationUnit serviceCompilationUnit = (JavaCompilationUnit) sourceCode.
                        createCompilationUnit(this.description.getPackageName() + ".services", domainClassDescription.getClassName() + "Service");
                JavaTypeDeclaration serviceTypeDeclaration = serviceCompilationUnit.createTypeDeclaration(domainClassDescription.getClassName() + "Service");
                serviceTypeDeclaration.modifiers(PUBLIC);
                serviceTypeDeclaration.annotations().add(ClassName.of("org.springframework.stereotype.Service"));

                JavaFieldDeclaration repositoryFieldDeclaration = createRepositoryFieldDeclaration(domainClassDescription, serviceTypeDeclaration);

                addAutowiredConstructor(serviceTypeDeclaration, repositoryFieldDeclaration);
                addFindAllMethod(domainClassDescription, repositoryFieldDeclaration, serviceTypeDeclaration);
                addGetByIdMethod(domainClassDescription, repositoryFieldDeclaration, serviceTypeDeclaration);
                addSaveMethod(domainClassDescription, repositoryFieldDeclaration, serviceTypeDeclaration);
                addPutMethod(domainClassDescription, repositoryFieldDeclaration, serviceTypeDeclaration);
                addDeleteByIdMethod(domainClassDescription, repositoryFieldDeclaration, serviceTypeDeclaration);
            }
        }

        this.sourceCodeWriter.writeTo(
                this.description.getBuildSystem().getMainSource(projectRoot, this.description.getLanguage()),
                sourceCode);
    }

    private void addFindAllMethod(DomainClassDescription domainClassDescription, JavaFieldDeclaration repositoryFieldDeclaration, JavaTypeDeclaration serviceTypeDeclaration) {
        CodeBlock code = CodeBlock.ofStatement("return $L.findAll()", repositoryFieldDeclaration.getName());
        JavaMethodDeclaration getEntityByIdMethodDeclaration = JavaMethodDeclaration
                .method("getAll" + domainClassDescription.getClassName() + "s")
                .modifiers(PUBLIC)
                .returning("java.util.List")
                .returnGenerics(domainClassDescription.getClassName())
                .body(code);
        serviceTypeDeclaration.addMethodDeclaration(getEntityByIdMethodDeclaration);
    }

    private JavaFieldDeclaration createRepositoryFieldDeclaration(DomainClassDescription domainClassDescription, JavaTypeDeclaration serviceTypeDeclaration) {
        JavaFieldDeclaration repositoryFieldDeclaration = JavaFieldDeclaration
                .field(domainClassDescription.getClassName().toLowerCase() + "Repository")
                .modifiers(PRIVATE | FINAL)
                .returning(this.description.getPackageName() + ".repositories." + domainClassDescription.getClassName() + "Repository");
        serviceTypeDeclaration.addFieldDeclaration(repositoryFieldDeclaration);
        return repositoryFieldDeclaration;
    }

    private void addAutowiredConstructor(JavaTypeDeclaration serviceTypeDeclaration, JavaFieldDeclaration repositoryFieldDeclaration) {
        CodeBlock code = CodeBlock.builder().addStatement("this.$L = $L", repositoryFieldDeclaration.getName(), repositoryFieldDeclaration.getName()).build();
        JavaMethodDeclaration autowiredConstructorDeclaration = JavaMethodDeclaration
                .method("")
                .modifiers(PUBLIC)
                .returning(serviceTypeDeclaration.getName())
                .parameters(Parameter.of(repositoryFieldDeclaration.getName(), repositoryFieldDeclaration.getReturnType()))
                .body(code);
        serviceTypeDeclaration.addMethodDeclaration(autowiredConstructorDeclaration);
    }


    private void addGetByIdMethod(DomainClassDescription domainClassDescription, JavaFieldDeclaration repositoryFieldDeclaration, JavaTypeDeclaration serviceTypeDeclaration) {
        CodeBlock code = CodeBlock.builder().addStatement("return this.$L.findById(id).orElse(null)", repositoryFieldDeclaration.getName()).build();
        JavaMethodDeclaration getEntityByIdMethodDeclaration = JavaMethodDeclaration
                .method("get" + domainClassDescription.getClassName() + "ById")
                .modifiers(PUBLIC)
                .returning(getDomainClassImport(domainClassDescription))
                .parameters(
                        Parameter.of(
                                "id",
                                domainClassDescription.getPrimaryKeyField().getClassType()
                        )
                )
                .body(code);
        serviceTypeDeclaration.addMethodDeclaration(getEntityByIdMethodDeclaration);
    }

    private void addSaveMethod(DomainClassDescription domainClassDescription, JavaFieldDeclaration repositoryFieldDeclaration, JavaTypeDeclaration serviceTypeDeclaration) {
        CodeBlock code = CodeBlock.builder().addStatement("return this.$L.save($L)", repositoryFieldDeclaration.getName(), domainClassDescription.getClassName().toLowerCase()).build();
        JavaMethodDeclaration saveEntityMethodDeclaration = JavaMethodDeclaration
                .method("save" + domainClassDescription.getClassName())
                .modifiers(PUBLIC)
                .returning(getDomainClassImport(domainClassDescription))
                .parameters(Parameter.of(
                        domainClassDescription.getClassName().toLowerCase(),
                        getDomainClassImport(domainClassDescription)
                ))
                .body(code);

        serviceTypeDeclaration.addMethodDeclaration(saveEntityMethodDeclaration);
    }

    private void addPutMethod(DomainClassDescription domainClassDescription, JavaFieldDeclaration repositoryFieldDeclaration, JavaTypeDeclaration serviceTypeDeclaration) {
        String repositoryName = repositoryFieldDeclaration.getName();
        String className = domainClassDescription.getClassName();
        String lowerClassName = className.toLowerCase();
        String updateFields = updateFieldsCodeBlock(domainClassDescription);

        CodeBlock code = CodeBlock.of(String.format(
                "return this.%s.findById(id).map(existing%s -> {%n" +
                        "%s" +
                        "      return %s.save(existing%s);%n" +
                        "    }).orElseGet(() -> {%n" +
                        "      return %s.save(%s);%n" +
                        "    });%n  ",
                repositoryName,
                className,
                updateFields,
                repositoryName,
                className,
                repositoryName,
                lowerClassName
        ));
        JavaMethodDeclaration saveEntityMethodDeclaration = JavaMethodDeclaration
                .method("update" + domainClassDescription.getClassName())
                .modifiers(PUBLIC)
                .returning(getDomainClassImport(domainClassDescription))
                .parameters(
                        Parameter.of(
                                domainClassDescription.getClassName().toLowerCase(),
                                getDomainClassImport(domainClassDescription)
                        ),
                        Parameter.of(
                                "id",
                                domainClassDescription.getPrimaryKeyField().getClassType()
                        )
                )
                .body(code);

        serviceTypeDeclaration.addMethodDeclaration(saveEntityMethodDeclaration);
    }

    private String updateFieldsCodeBlock(DomainClassDescription domainClassDescription) {
        StringBuilder res = new StringBuilder();
        String getter = "";
        String setter = "";
        for (FieldDescription field : domainClassDescription.getFields()) {
            if (field.getFieldName().equals("id")) continue;
            getter = "get" + capitalize(field.getFieldName());
            setter = "set" + capitalize(field.getFieldName());
            res.append("      if (")
                .append(domainClassDescription.getClassName().toLowerCase())
                .append(".").append(getter).append("() != null) {\n")
                .append("        existing").append(domainClassDescription.getClassName()).append(".").append(setter)
                .append("(").append(domainClassDescription.getClassName().toLowerCase()).append(".").append(getter).append("());\n")
                .append("      }\n");
        }

        List<AssociationDescription> associations = this.description.getAssotiationDescriptions().stream().filter(association ->
                association.getFirstClassName().equals(domainClassDescription.getClassName()) ||
                        association.getSecondClassName().equals(domainClassDescription.getClassName())
        ).toList();
        for (AssociationDescription association : associations) {
            switch (association.getAssotiationType()) {
                case ONE_TO_ONE: {
                    if (domainClassDescription.getClassName().equals(association.getFirstClassName())) {
                        getter = "get" + capitalize(association.getSecondClassName());
                        setter = "set" + capitalize(association.getSecondClassName());
                    } else {
                        getter = "get" + capitalize(association.getFirstClassName());
                        setter = "set" + capitalize(association.getFirstClassName());
                    }
                    res.append("      if (")
                            .append(domainClassDescription.getClassName().toLowerCase())
                            .append(".").append(getter).append("() != null) {\n")
                            .append("        existing").append(domainClassDescription.getClassName()).append(".").append(setter)
                            .append("(").append(domainClassDescription.getClassName().toLowerCase()).append(".").append(getter).append("());\n")
                            .append("      }\n");
                    break;
                }
                case ONE_TO_MANY: {
                    if (domainClassDescription.getClassName().equals(association.getFirstClassName())) {
                        getter = "get" + capitalize(association.getSecondClassName()) + "s";
                        setter = "set" + capitalize(association.getSecondClassName()) + "s";
                    } else {
                        getter = "get" + capitalize(association.getFirstClassName());
                        setter = "set" + capitalize(association.getFirstClassName());
                    }
                    res.append("      if (")
                            .append(domainClassDescription.getClassName().toLowerCase())
                            .append(".").append(getter).append("() != null) {\n")
                            .append("        existing").append(domainClassDescription.getClassName()).append(".").append(setter)
                            .append("(").append(domainClassDescription.getClassName().toLowerCase()).append(".").append(getter).append("());\n")
                            .append("      }\n");
                    break;
                }
                case MANY_TO_ONE: {
                    if (domainClassDescription.getClassName().equals(association.getFirstClassName())) {
                        getter = "get" + capitalize(association.getSecondClassName());
                        setter = "set" + capitalize(association.getSecondClassName());
                    } else {
                        getter = "get" + capitalize(association.getFirstClassName()) + "s";
                        setter = "set" + capitalize(association.getFirstClassName()) + "s";
                    }
                    res.append("      if (")
                            .append(domainClassDescription.getClassName().toLowerCase())
                            .append(".").append(getter).append("() != null) {\n")
                            .append("        existing").append(domainClassDescription.getClassName()).append(".").append(setter)
                            .append("(").append(domainClassDescription.getClassName().toLowerCase()).append(".").append(getter).append("());\n")
                            .append("      }\n");
                    break;

                }
                case MANY_TO_MANY: {
                    if (domainClassDescription.getClassName().equals(association.getFirstClassName())) {
                        getter = "get" + capitalize(association.getSecondClassName()) + "s";
                        setter = "set" + capitalize(association.getSecondClassName()) + "s";
                    } else {
                        getter = "get" + capitalize(association.getFirstClassName()) + "s";
                        setter = "set" + capitalize(association.getFirstClassName()) + "s";
                    }
                    res.append("      if (")
                            .append(domainClassDescription.getClassName().toLowerCase())
                            .append(".").append(getter).append("() != null) {\n")
                            .append("        existing").append(domainClassDescription.getClassName()).append(".").append(setter)
                            .append("(").append(domainClassDescription.getClassName().toLowerCase()).append(".").append(getter).append("());\n")
                            .append("      }\n");
                    break;
                }
            }
        }
        return res.toString();
    }

    private void addDeleteByIdMethod(DomainClassDescription domainClassDescription, JavaFieldDeclaration repositoryFieldDeclaration, JavaTypeDeclaration serviceTypeDeclaration) {
        CodeBlock code = CodeBlock.builder().addStatement("this.$L.deleteById(id)", repositoryFieldDeclaration.getName()).build();
        JavaMethodDeclaration deleteEntityByIdMethod = JavaMethodDeclaration
                .method("delete" + domainClassDescription.getClassName() + "ById")
                .modifiers(PUBLIC)
                .returning("void")
                .parameters(
                        Parameter.of(
                                "id",
                                domainClassDescription.getPrimaryKeyField().getClassType()
                        )
                )
                .body(code);
        serviceTypeDeclaration.addMethodDeclaration(deleteEntityByIdMethod);
    }

    private String getDomainClassImport(DomainClassDescription domainClassDescription) {
        return this.description.getPackageName() + ".domain." + domainClassDescription.getClassName();
    }

    public String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override
    public int getOrder() {
        return ProjectContributor.super.getOrder();
    }
}
