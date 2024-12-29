package io.spring.initializr.generator.language.scala;

import io.spring.initializr.generator.language.Annotatable;
import io.spring.initializr.generator.language.AnnotationContainer;

public class ScalaFieldDeclaration implements Annotatable {

	private final AnnotationContainer annotations = new AnnotationContainer();

	private final int modifiers;

	private final String name;

	private final String returnType;

	private final Object value;

	private final boolean initialized;

	private ScalaFieldDeclaration(ScalaFieldDeclaration.Builder builder) {
		this.modifiers = builder.modifiers;
		this.name = builder.name;
		this.returnType = builder.returnType;
		this.value = builder.value;
		this.initialized = builder.initialized;
	}

	public static Builder field(String name) {
		return new Builder(name);
	}

	@Override
	public AnnotationContainer annotations() {
		return this.annotations;
	}

	public int getModifiers() {
		return this.modifiers;
	}

	public String getName() {
		return this.name;
	}

	public String getReturnType() {
		return this.returnType;
	}

	public Object getValue() {
		return this.value;
	}

	public boolean isInitialized() {
		return this.initialized;
	}

	/**
	 * Builder for creating a {@link ScalaFieldDeclaration}.
	 */
	public static final class Builder {

		private final String name;

		private String returnType;

		private int modifiers;

		private Object value;

		private boolean initialized;

		private Builder(String name) {
			this.name = name;
		}

		public Builder modifiers(int modifiers) {
			this.modifiers = modifiers;
			return this;
		}

		public Builder value(Object value) {
			this.value = value;
			this.initialized = true;
			return this;
		}

		public ScalaFieldDeclaration returning(String returnType) {
			this.returnType = returnType;
			return new ScalaFieldDeclaration(this);
		}

	}

}
