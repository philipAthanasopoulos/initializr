package io.spring.initializr.generator.language.scala;

import io.spring.initializr.generator.io.IndentingWriter;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.language.*;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScalaSourceCodeWriter implements SourceCodeWriter<ScalaSourceCode> {

	private static final Map<Predicate<Integer>, String> TYPE_MODIFIERS;

	private static final Map<Predicate<Integer>, String> FIELD_MODIFIERS;

	private static final Map<Predicate<Integer>, String> METHOD_MODIFIERS;

	static {
		Map<Predicate<Integer>, String> typeModifiers = new LinkedHashMap<>();
		typeModifiers.put(Modifier::isPublic, "public");
		typeModifiers.put(Modifier::isProtected, "protected");
		typeModifiers.put(Modifier::isPrivate, "private");
		typeModifiers.put(Modifier::isAbstract, "abstract");
		typeModifiers.put(Modifier::isStatic, "static");
		typeModifiers.put(Modifier::isFinal, "final");
		typeModifiers.put(Modifier::isStrict, "strictfp");
		TYPE_MODIFIERS = typeModifiers;
		Map<Predicate<Integer>, String> fieldModifiers = new LinkedHashMap<>();
		fieldModifiers.put(Modifier::isPublic, "public");
		fieldModifiers.put(Modifier::isProtected, "protected");
		fieldModifiers.put(Modifier::isPrivate, "private");
		fieldModifiers.put(Modifier::isStatic, "static");
		fieldModifiers.put(Modifier::isFinal, "final");
		fieldModifiers.put(Modifier::isTransient, "transient");
		fieldModifiers.put(Modifier::isVolatile, "volatile");
		FIELD_MODIFIERS = fieldModifiers;
		Map<Predicate<Integer>, String> methodModifiers = new LinkedHashMap<>(typeModifiers);
		methodModifiers.put(Modifier::isSynchronized, "synchronized");
		methodModifiers.put(Modifier::isNative, "native");
		METHOD_MODIFIERS = methodModifiers;
	}

	private final IndentingWriterFactory indentingWriterFactory;

	public ScalaSourceCodeWriter(IndentingWriterFactory indentingWriterFactory) {
		this.indentingWriterFactory = indentingWriterFactory;
	}

	@Override
	public void writeTo(SourceStructure structure, ScalaSourceCode sourceCode) throws IOException {
		for (ScalaCompilationUnit compilationUnit : sourceCode.getCompilationUnits()) {
			writeTo(structure, compilationUnit);
		}
	}

	private void writeTo(SourceStructure structure, ScalaCompilationUnit compilationUnit) throws IOException {
		Path output = structure.createSourceFile(compilationUnit.getPackageName(), compilationUnit.getName());
		Files.createDirectories(output.getParent());
		try (IndentingWriter writer = this.indentingWriterFactory.createIndentingWriter("scala",
				Files.newBufferedWriter(output))) {
			writer.println("package " + compilationUnit.getPackageName());
			writer.println();
			Set<String> imports = determineImports(compilationUnit);
			if (!imports.isEmpty()) {
				for (String importedType : imports) {
					writer.println("import " + importedType);
				}
				writer.println();
			}
			for (ScalaTypeDeclaration type : compilationUnit.getTypeDeclarations()) {
				writeAnnotations(writer, type, writer::println);
				writeModifiers(writer, TYPE_MODIFIERS, type.getModifiers());
				writer.print("class " + type.getName());
				if (type.getExtends() != null) {
					writer.print(" extends " + getUnqualifiedName(type.getExtends()));
				}
				writer.println(" {");
				writer.println();
				List<ScalaFieldDeclaration> fieldDeclarations = type.getFieldDeclarations();
				if (!fieldDeclarations.isEmpty()) {
					writer.indented(() -> {
						for (ScalaFieldDeclaration fieldDeclaration : fieldDeclarations) {
							writeFieldDeclaration(writer, fieldDeclaration);
						}
					});
				}
				List<ScalaMethodDeclaration> methodDeclarations = type.getMethodDeclarations();
				if (!methodDeclarations.isEmpty()) {
					writer.indented(() -> {
						for (ScalaMethodDeclaration methodDeclaration : methodDeclarations) {
							writeMethodDeclaration(writer, methodDeclaration);
						}
					});
				}
				writer.println("}");
			}
		}
	}

	private void writeAnnotations(IndentingWriter writer, Annotatable annotatable, Runnable separator) {
		annotatable.annotations().values().forEach((annotation) -> {
			annotation.write(writer, CodeBlock.JAVA_FORMATTING_OPTIONS);
			separator.run();
		});
	}

	private void writeAnnotations(IndentingWriter writer, Annotatable annotatable) {
		writeAnnotations(writer, annotatable, writer::println);
	}

	private void writeFieldDeclaration(IndentingWriter writer, ScalaFieldDeclaration fieldDeclaration) {
		writeAnnotations(writer, fieldDeclaration);
		writeModifiers(writer, FIELD_MODIFIERS, fieldDeclaration.getModifiers());
		writer.print("val " + fieldDeclaration.getName() + ": " + getUnqualifiedName(fieldDeclaration.getReturnType()));
		if (fieldDeclaration.isInitialized()) {
			writer.print(" = " + String.valueOf(fieldDeclaration.getValue()));
		}
		writer.println();
		writer.println();
	}

	private void writeMethodDeclaration(IndentingWriter writer, ScalaMethodDeclaration methodDeclaration) {
		writeAnnotations(writer, methodDeclaration);
		writeModifiers(writer, METHOD_MODIFIERS, methodDeclaration.getModifiers());
		writer.print("def " + methodDeclaration.getName() + "(");
		writeParameters(writer, methodDeclaration.getParameters());
		writer.println("): " + getUnqualifiedName(methodDeclaration.getReturnType()) + " = {");
		writer.indented(() -> methodDeclaration.getCode().write(writer, CodeBlock.JAVA_FORMATTING_OPTIONS));
		writer.println("}");
		writer.println();
	}

	private void writeParameters(IndentingWriter writer, List<Parameter> parameters) {
		if (parameters.isEmpty()) {
			return;
		}
		Iterator<Parameter> it = parameters.iterator();
		while (it.hasNext()) {
			Parameter parameter = it.next();
			writeAnnotations(writer, parameter, () -> writer.print(" "));
			writer.print(parameter.getName() + ": " + getUnqualifiedName(parameter.getType()));
			if (it.hasNext()) {
				writer.print(", ");
			}
		}
	}

	private void writeModifiers(IndentingWriter writer, Map<Predicate<Integer>, String> availableModifiers,
			int declaredModifiers) {
		String modifiers = availableModifiers.entrySet()
			.stream()
			.filter((entry) -> entry.getKey().test(declaredModifiers))
			.map(Map.Entry::getValue)
			.collect(Collectors.joining(" "));
		if (!modifiers.isEmpty()) {
			writer.print(modifiers);
			writer.print(" ");
		}
	}

	private Set<String> determineImports(ScalaCompilationUnit compilationUnit) {
		List<String> imports = new ArrayList<>();
		for (ScalaTypeDeclaration typeDeclaration : compilationUnit.getTypeDeclarations()) {
			imports.add(typeDeclaration.getExtends());

			imports.addAll(appendImports(typeDeclaration.annotations().values(), Annotation::getImports));
			for (ScalaFieldDeclaration fieldDeclaration : typeDeclaration.getFieldDeclarations()) {
				imports.add(fieldDeclaration.getReturnType());
				imports.addAll(appendImports(fieldDeclaration.annotations().values(), Annotation::getImports));
			}
			for (ScalaMethodDeclaration methodDeclaration : typeDeclaration.getMethodDeclarations()) {
				imports.add(methodDeclaration.getReturnType());
				imports.addAll(appendImports(methodDeclaration.annotations().values(), Annotation::getImports));
				for (Parameter parameter : methodDeclaration.getParameters()) {
					imports.add(parameter.getType());
					imports.addAll(appendImports(parameter.annotations().values(), Annotation::getImports));
				}
				imports.addAll(methodDeclaration.getCode().getImports());
			}
		}
		return imports.stream()
			.filter((candidate) -> isImportCandidate(compilationUnit, candidate))
			.sorted()
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private <T> List<String> appendImports(Stream<T> candidates, Function<T, Collection<String>> mapping) {
		return candidates.map(mapping).flatMap(Collection::stream).collect(Collectors.toList());
	}

	private String getUnqualifiedName(String name) {
		if (!name.contains(".")) {
			return name;
		}
		return name.substring(name.lastIndexOf(".") + 1);
	}

	private boolean isImportCandidate(CompilationUnit<?> compilationUnit, String name) {
		if (name == null || !name.contains(".")) {
			return false;
		}
		String packageName = name.substring(0, name.lastIndexOf('.'));
		return !"java.lang".equals(packageName) && !compilationUnit.getPackageName().equals(packageName);
	}

}