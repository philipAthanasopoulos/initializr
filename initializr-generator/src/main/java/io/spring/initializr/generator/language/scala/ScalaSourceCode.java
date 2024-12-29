package io.spring.initializr.generator.language.scala;

import io.spring.initializr.generator.language.SourceCode;

public class ScalaSourceCode extends SourceCode<ScalaTypeDeclaration, ScalaCompilationUnit> {

	public ScalaSourceCode() {
		super(ScalaCompilationUnit::new);
	}

}
