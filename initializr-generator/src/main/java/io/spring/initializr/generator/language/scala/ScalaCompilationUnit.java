package io.spring.initializr.generator.language.scala;

import io.spring.initializr.generator.language.CompilationUnit;

public class ScalaCompilationUnit extends CompilationUnit<ScalaTypeDeclaration> {

	ScalaCompilationUnit(String packageName, String name) {
		super(packageName, name);
	}

	@Override
	protected ScalaTypeDeclaration doCreateTypeDeclaration(String name) {
		return new ScalaTypeDeclaration(name);
	}

}
