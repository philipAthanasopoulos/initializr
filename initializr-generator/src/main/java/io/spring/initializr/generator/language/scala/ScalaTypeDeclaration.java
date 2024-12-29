package io.spring.initializr.generator.language.scala;

import io.spring.initializr.generator.language.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

public class ScalaTypeDeclaration extends TypeDeclaration {

	private final List<ScalaFieldDeclaration> fieldDeclarations = new ArrayList<>();

	private final List<ScalaMethodDeclaration> methodDeclarations = new ArrayList<>();

	private int modifiers;

	ScalaTypeDeclaration(String name) {
		super(name);
	}

	public void modifiers(int modifiers) {
		this.modifiers = modifiers;
	}

	public int getModifiers() {
		return this.modifiers;
	}

	public void addFieldDeclaration(ScalaFieldDeclaration fieldDeclaration) {
		this.fieldDeclarations.add(fieldDeclaration);
	}

	public List<ScalaFieldDeclaration> getFieldDeclarations() {
		return this.fieldDeclarations;
	}

	public void addMethodDeclaration(ScalaMethodDeclaration methodDeclaration) {
		this.methodDeclarations.add(methodDeclaration);
	}

	public List<ScalaMethodDeclaration> getMethodDeclarations() {
		return this.methodDeclarations;
	}

}
