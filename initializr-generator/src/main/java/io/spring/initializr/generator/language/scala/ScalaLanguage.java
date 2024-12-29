package io.spring.initializr.generator.language.scala;

import io.spring.initializr.generator.language.AbstractLanguage;

import java.util.Set;

public class ScalaLanguage extends AbstractLanguage {

	public static final String ID = "scala";

	// See https://docs.scala-lang.org/style/naming-conventions.html#keywords
	private static final Set<String> KEYWORDS = Set.of(
		"abstract", "case", "catch", "class", "def", "do", "else", "extends", "false", "final", "finally", "for",
		"forSome", "if", "implicit", "import", "lazy", "match", "new", "null", "object", "override", "package",
		"private", "protected", "return", "sealed", "super", "this", "throw", "trait", "try", "true", "type",
		"val", "var", "while", "with", "yield"
	);

	public ScalaLanguage() {
		this(DEFAULT_JVM_VERSION);
	}

	public ScalaLanguage(String jvmVersion) {
		super(ID, jvmVersion, "scala");
	}

	@Override
	public boolean supportsEscapingKeywordsInPackage() {
		return false;
	}

	@Override
	public boolean isKeyword(String input) {
		return KEYWORDS.contains(input);
	}

}