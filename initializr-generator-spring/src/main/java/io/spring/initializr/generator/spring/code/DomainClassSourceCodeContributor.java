package io.spring.initializr.generator.spring.code;

import io.spring.initializr.generator.language.*;
import io.spring.initializr.generator.language.java.JavaCompilationUnit;
import io.spring.initializr.generator.language.java.JavaTypeDeclaration;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.contributor.ProjectContributor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.reflect.Modifier.PUBLIC;

public class DomainClassSourceCodeContributor<T extends TypeDeclaration, C extends CompilationUnit<T>, S extends SourceCode<T, C>>
        implements ProjectContributor {

    private List<DomainClassDescription> domainClassDescriptions = new ArrayList<>();
    private final SourceCodeWriter<S> sourceCodeWriter;
    private final Supplier<S> sourceFactory;
    private final ProjectDescription description;


    public DomainClassSourceCodeContributor(SourceCodeWriter<S> sourceCodeWriter, Supplier<S> sourceFactory, ProjectDescription description) {
        this.sourceCodeWriter = sourceCodeWriter;
        this.sourceFactory = sourceFactory;
        this.description = description;

        domainClassDescriptions.addAll(
                List.of(
                        new DomainClassDescription("User", new ArrayList<>()),
                        new DomainClassDescription("Product", new ArrayList<>()),
                        new DomainClassDescription("Order", new ArrayList<>()),
                        new DomainClassDescription("Customer", new ArrayList<>()),
                        new DomainClassDescription("Invoice", new ArrayList<>()),
                        new DomainClassDescription("Payment", new ArrayList<>()),
                        new DomainClassDescription("Shipment", new ArrayList<>())
                )
        );
    }

    @Override
    public void contribute(Path projectRoot) throws IOException {
        S sourceCode = this.sourceFactory.get();

        for (DomainClassDescription domainClassDescription : domainClassDescriptions) {
            JavaCompilationUnit domainClassCompilationUnit = (JavaCompilationUnit) sourceCode.createCompilationUnit(this.description.getPackageName() + ".domain", domainClassDescription.getClassName());
            JavaTypeDeclaration domainClassTypeDeclaration = domainClassCompilationUnit.createTypeDeclaration(domainClassDescription.getClassName());
            domainClassTypeDeclaration.modifiers(PUBLIC);
            domainClassTypeDeclaration.annotations().add(ClassName.of("jakarta.persistence.Entity"));
            domainClassTypeDeclaration.annotations().add(ClassName.of("jakarta.persistence.Table"),
                    (annotation) -> annotation.add("name", domainClassDescription.getClassName() + "_table"));
            domainClassTypeDeclaration.annotations().add(ClassName.of("lombok.Data"));
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
