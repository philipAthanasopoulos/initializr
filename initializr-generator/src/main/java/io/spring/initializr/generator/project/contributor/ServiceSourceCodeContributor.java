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
            JavaCompilationUnit serviceCompilationUnit = (JavaCompilationUnit) sourceCode.createCompilationUnit(this.description.getPackageName() + ".services", domainClassDescription.getClassName() + "Service");
            JavaTypeDeclaration serviceTypeDeclaration = serviceCompilationUnit.createTypeDeclaration(domainClassDescription.getClassName() + "Service");
            serviceTypeDeclaration.modifiers(PUBLIC);
            serviceTypeDeclaration.annotations().add(ClassName.of("org.springframework.stereotype.Service"));

            JavaFieldDeclaration repositoryFieldDeclaration = createRepositoryFieldDeclaration(domainClassDescription, serviceTypeDeclaration);

            addAutowiredConstructor(serviceTypeDeclaration, repositoryFieldDeclaration);

            addSaveMethod(domainClassDescription, repositoryFieldDeclaration, serviceTypeDeclaration);

            addGetByIdMethod(domainClassDescription, repositoryFieldDeclaration, serviceTypeDeclaration);

            addDeleteByIdMethod(domainClassDescription, repositoryFieldDeclaration, serviceTypeDeclaration);
        }

        this.sourceCodeWriter.writeTo(
                this.description.getBuildSystem().getMainSource(projectRoot, this.description.getLanguage()),
                sourceCode);
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
        JavaMethodDeclaration autowiredConstructorDeclaration = JavaMethodDeclaration
                .method("")
                .modifiers(PUBLIC)
                .returning(serviceTypeDeclaration.getName())
                .parameters(Parameter.of(repositoryFieldDeclaration.getName(), repositoryFieldDeclaration.getReturnType()))
                .body(CodeBlock.ofStatement("this." + repositoryFieldDeclaration.getName() + " = " + repositoryFieldDeclaration.getName()));
        serviceTypeDeclaration.addMethodDeclaration(autowiredConstructorDeclaration);
    }

    private void addSaveMethod(DomainClassDescription domainClassDescription, JavaFieldDeclaration repositoryFieldDeclaration, JavaTypeDeclaration serviceTypeDeclaration) {
        JavaMethodDeclaration saveEntityMethodDeclaration = JavaMethodDeclaration
                .method("save" + domainClassDescription.getClassName())
                .modifiers(PUBLIC)
                .returning(getDomainClassImport(domainClassDescription))
                .parameters(Parameter.of(
                        domainClassDescription.getClassName().toLowerCase(),
                        getDomainClassImport(domainClassDescription)
                ))
                .body(CodeBlock.of("return this." + repositoryFieldDeclaration.getName() + ".save(" + domainClassDescription.getClassName().toLowerCase() + ");"));

        serviceTypeDeclaration.addMethodDeclaration(saveEntityMethodDeclaration);
    }

    private void addGetByIdMethod(DomainClassDescription domainClassDescription, JavaFieldDeclaration repositoryFieldDeclaration, JavaTypeDeclaration serviceTypeDeclaration) {
        JavaMethodDeclaration getEntityByIdMethodDeclaration = JavaMethodDeclaration
                .method("get" + domainClassDescription.getClassName() + "ById")
                .modifiers(PUBLIC)
                .returning(getDomainClassImport(domainClassDescription))
                .parameters(
                        Parameter.of(
                                "id",
                                "java.lang.Long"
                        )
                )
                .body(CodeBlock.of(
                        "return this." + repositoryFieldDeclaration.getName() + ".getById(id);"
                ));
        serviceTypeDeclaration.addMethodDeclaration(getEntityByIdMethodDeclaration);
    }

    private void addDeleteByIdMethod(DomainClassDescription domainClassDescription, JavaFieldDeclaration repositoryFieldDeclaration, JavaTypeDeclaration serviceTypeDeclaration) {
        JavaMethodDeclaration deleteEntityByIdMethod = JavaMethodDeclaration
                .method("delete" + domainClassDescription.getClassName() + "ById")
                .modifiers(PUBLIC)
                .returning("void")
                .parameters(
                        Parameter.of(
                                "id",
                                "java.lang.Long"
                        )
                )
                .body(CodeBlock.of(
                        "this." + repositoryFieldDeclaration.getName() + ".deleteById(id);"
                ));
        serviceTypeDeclaration.addMethodDeclaration(deleteEntityByIdMethod);
    }

    private String getDomainClassImport(DomainClassDescription domainClassDescription) {
        return this.description.getPackageName() + ".domain." + domainClassDescription.getClassName();
    }

    @Override
    public int getOrder() {
        return ProjectContributor.super.getOrder();
    }
}
