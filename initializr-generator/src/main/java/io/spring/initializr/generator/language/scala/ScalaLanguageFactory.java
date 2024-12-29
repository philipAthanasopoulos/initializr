package io.spring.initializr.generator.language.scala;

import io.spring.initializr.generator.language.Language;
import io.spring.initializr.generator.language.LanguageFactory;
import io.spring.initializr.generator.language.java.JavaLanguage;

public class ScalaLanguageFactory implements LanguageFactory {

	@Override
	public Language createLanguage(String id, String jvmVersion) {
		if (ScalaLanguage.ID.equals(id)) {
			return new ScalaLanguage(jvmVersion);
		}
		return null;
	}

}
