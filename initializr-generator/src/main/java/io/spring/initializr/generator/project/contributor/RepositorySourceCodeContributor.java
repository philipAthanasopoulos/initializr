package io.spring.initializr.generator.project.contributor;

import io.spring.initializr.generator.language.*;
import io.spring.initializr.generator.language.java.JavaCompilationUnit;
import io.spring.initializr.generator.language.java.JavaTypeDeclaration;
import io.spring.initializr.generator.project.DomainClassDescription;
import io.spring.initializr.generator.project.ProjectDescription;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.reflect.Modifier.INTERFACE;
import static java.lang.reflect.Modifier.PUBLIC;

public class RepositorySourceCodeContributor<T extends TypeDeclaration, C extends CompilationUnit<T>, S extends SourceCode<T, C>> implements ProjectContributor {

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


    public RepositorySourceCodeContributor(SourceCodeWriter<S> sourceCodeWriter, Supplier<S> sourceFactory, ProjectDescription description) {
        this.sourceCodeWriter = sourceCodeWriter;
        this.sourceFactory = sourceFactory;
        this.description = description;
        this.domainClassDescriptions = description.getDomainClassDescriptions();
    }

    @Override
    public void contribute(Path projectRoot) throws IOException {
        S sourceCode = this.sourceFactory.get();

        for (DomainClassDescription domainClassDescription : domainClassDescriptions) {
            JavaCompilationUnit repositoryCompilationUnit = (JavaCompilationUnit) sourceCode.createCompilationUnit(
                    this.description.getPackageName() + ".repositories",
                    domainClassDescription.getClassName() + "Repository");
            JavaTypeDeclaration repositoryTypeDeclaration = repositoryCompilationUnit.createTypeDeclaration(
                    domainClassDescription.getClassName() + "Repository");
            repositoryTypeDeclaration.annotations().add(ClassName.of("org.springframework.stereotype.Repository"));
            repositoryTypeDeclaration.extend("org.springframework.data.jpa.repository.JpaRepository");
            repositoryTypeDeclaration.extendedGenerics(
                    this.description.getPackageName() + ".domain." + domainClassDescription.getClassName(),
                    "java.lang.Long");
            repositoryTypeDeclaration.modifiers(PUBLIC | INTERFACE);
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
