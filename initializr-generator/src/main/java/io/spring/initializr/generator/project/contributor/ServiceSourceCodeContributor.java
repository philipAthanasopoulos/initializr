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

            //create repository field
            JavaFieldDeclaration repositoryFieldDeclaration = JavaFieldDeclaration
                    .field(domainClassDescription.getClassName().toLowerCase() + "Repository")
                    .modifiers(PRIVATE | FINAL)
                    .returning(this.description.getPackageName() + ".repositories." + domainClassDescription.getClassName() + "Repository");
            serviceTypeDeclaration.addFieldDeclaration(repositoryFieldDeclaration);

            //create autowired constructor
            JavaMethodDeclaration autowiredConstructorDeclaration = JavaMethodDeclaration
                    .method("")
                    .modifiers(PUBLIC)
                    .returning(serviceTypeDeclaration.getName())
                    .parameters(Parameter.of(repositoryFieldDeclaration.getName(), repositoryFieldDeclaration.getReturnType()))
                    .body(CodeBlock.ofStatement("this." + repositoryFieldDeclaration.getName() + " = " + repositoryFieldDeclaration.getName()));

            serviceTypeDeclaration.addMethodDeclaration(autowiredConstructorDeclaration);

            //save entity method
            String domainClassImport = this.description.getPackageName() + ".domain." + domainClassDescription.getClassName();
            JavaMethodDeclaration saveEntityMethodDeclaration = JavaMethodDeclaration
                    .method("save" + domainClassDescription.getClassName())
                    .modifiers(PUBLIC)
                    .returning(domainClassImport)
                    .parameters(Parameter.of(
                            domainClassDescription.getClassName().toLowerCase(),
                            domainClassImport
                    ))
                    .body(CodeBlock.of("return this." + repositoryFieldDeclaration.getName() + ".save(" + domainClassDescription.getClassName().toLowerCase() + ");"));

            serviceTypeDeclaration.addMethodDeclaration(saveEntityMethodDeclaration);


            //get by id method
            JavaMethodDeclaration getEntityByIdMethodDeclaration = JavaMethodDeclaration
                    .method("get" + domainClassDescription.getClassName() + "ById")
                    .modifiers(PUBLIC)
                    .returning(domainClassImport)
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

        this.sourceCodeWriter.writeTo(
                this.description.getBuildSystem().getMainSource(projectRoot, this.description.getLanguage()),
                sourceCode);
    }

    @Override
    public int getOrder() {
        return ProjectContributor.super.getOrder();
    }
}
