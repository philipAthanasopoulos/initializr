package io.spring.initializr.generator.language.scala;

import io.spring.initializr.generator.language.Annotatable;
import io.spring.initializr.generator.language.AnnotationContainer;
import io.spring.initializr.generator.language.CodeBlock;
import io.spring.initializr.generator.language.Parameter;
import io.spring.initializr.generator.language.java.JavaMethodDeclaration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScalaMethodDeclaration implements Annotatable {

	private final AnnotationContainer annotations = new AnnotationContainer();

	private final String name;

	private final String returnType;

	private final int modifiers;

	private final List<Parameter> parameters;

	private final CodeBlock code;

	private ScalaMethodDeclaration(Builder builder, CodeBlock code) {
		this.name = builder.name;
		this.returnType = builder.returnType;
		this.modifiers = builder.modifiers;
		this.parameters = List.copyOf(builder.parameters);
		this.code = code;
	}

	public static Builder method(String name) {
		return new Builder(name);
	}

	String getName() {
		return this.name;
	}

	String getReturnType() {
		return this.returnType;
	}

	List<Parameter> getParameters() {
		return this.parameters;
	}

	int getModifiers() {
		return this.modifiers;
	}

	CodeBlock getCode() {
		return this.code;
	}

	@Override
	public AnnotationContainer annotations() {
		return this.annotations;
	}

	/**
	 * Builder for creating a {@link JavaMethodDeclaration}.
	 */
	public static final class Builder {

		private final String name;

		private List<Parameter> parameters = new ArrayList<>();

		private String returnType = "void";

		private int modifiers;

		private Builder(String name) {
			this.name = name;
		}

		public Builder modifiers(int modifiers) {
			this.modifiers = modifiers;
			return this;
		}

		public Builder returning(String returnType) {
			this.returnType = returnType;
			return this;
		}

		public Builder parameters(Parameter... parameters) {
			this.parameters = Arrays.asList(parameters);
			return this;
		}

		public ScalaMethodDeclaration body(CodeBlock code) {
			return new ScalaMethodDeclaration(this, code);
		}

	}

}
