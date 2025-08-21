package io.spring.initializr.generator.project.contributor;

import io.spring.initializr.generator.buildsystem.BuildSystem;
import io.spring.initializr.generator.language.Language;
import io.spring.initializr.generator.language.SourceCodeWriter;
import io.spring.initializr.generator.language.java.JavaCompilationUnit;
import io.spring.initializr.generator.language.java.JavaMethodDeclaration;
import io.spring.initializr.generator.language.java.JavaSourceCode;
import io.spring.initializr.generator.language.java.JavaTypeDeclaration;
import io.spring.initializr.generator.project.DomainClassDescription;
import io.spring.initializr.generator.project.FieldDescription;
import io.spring.initializr.generator.project.ProjectDescription;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DomainClassSourceCodeContributorTests {
    static SourceCodeWriter<JavaSourceCode> writer;
    static JavaSourceCode sourceCode;
    static DomainClassSourceCodeContributor<JavaTypeDeclaration, JavaCompilationUnit, JavaSourceCode> contributor;

    @BeforeAll
    static void setupDomainClassAndContributeClassWithFields() throws IOException {
        writer = mock(SourceCodeWriter.class);
        sourceCode = new JavaSourceCode();
        Supplier<JavaSourceCode> sourceFactory = () -> sourceCode;

        FieldDescription idField = new FieldDescription("id", "Long");
        FieldDescription nameField = new FieldDescription("name", "String");
        DomainClassDescription domainClass = new DomainClassDescription("User", List.of(idField, nameField), false, false, false);

        ProjectDescription projectDescription = mock(ProjectDescription.class);
        when(projectDescription.getDomainClassDescriptions()).thenReturn(List.of(domainClass));
        when(projectDescription.getPackageName()).thenReturn("com.example");
        when(projectDescription.getBuildSystem()).thenReturn(mock(BuildSystem.class));
        when(projectDescription.getLanguage()).thenReturn(mock(Language.class));
        when(projectDescription.getAssotiationDescriptions()).thenReturn(List.of());

        contributor = new DomainClassSourceCodeContributor<>(writer, sourceFactory, projectDescription);
        contributor.contribute(Path.of("test-root"));
    }

    @Test
    void contributeShouldGenerateDomainClass() throws Exception {
        assertFalse(sourceCode.getCompilationUnits().isEmpty());
        JavaCompilationUnit unit = sourceCode.getCompilationUnits().get(0);
        assertEquals("User", unit.getName());
        JavaTypeDeclaration type = unit.getTypeDeclarations().get(0);
        assertEquals("User", type.getName());
        assertTrue(type.getFieldDeclarations().stream().anyMatch(f -> f.getName().equals("id")));
        assertTrue(type.getFieldDeclarations().stream().anyMatch(f -> f.getName().equals("name")));
        verify(writer).writeTo(any(), eq(sourceCode));
    }

    @Test
    void contributeShouldGenerateConstructor() {
        JavaCompilationUnit unit = sourceCode.getCompilationUnits().get(0);
        JavaTypeDeclaration type = unit.getTypeDeclarations().get(0);
        JavaMethodDeclaration constructor = type.getMethodDeclarations().get(0);
        assertEquals("", constructor.getName());
    }

    @Test
    void contributeShouldGenerateGettersAndSetters() {
        JavaCompilationUnit unit = sourceCode.getCompilationUnits().get(0);
        JavaTypeDeclaration type = unit.getTypeDeclarations().get(0);
        type.getFieldDeclarations().forEach(field -> {
            assertTrue(type.getMethodDeclarations().stream()
                    .anyMatch(method -> method.getName().equals("get" + capitalize(field.getName()))));
            assertTrue(type.getMethodDeclarations().stream()
                    .anyMatch(method -> method.getName().equals("set" + capitalize(field.getName()))));
        });
    }

    @Test
    void test() {
    }

    private String capitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}