package io.spring.initializr.generator.spring.code.scala;

import io.spring.initializr.generator.condition.ConditionalOnLanguage;
import io.spring.initializr.generator.io.IndentingWriterFactory;
import io.spring.initializr.generator.language.scala.*;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.spring.code.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@ProjectGenerationConfiguration
@ConditionalOnLanguage(ScalaLanguage.ID)
@Import(ScalaProjectGenerationDefaultContributorsConfiguration.class)
public class ScalaProjectGenerationConfiguration {

	private final ProjectDescription description;

	public ScalaProjectGenerationConfiguration(ProjectDescription description) {
		this.description = description;
	}

	@Bean
	ScalaSourceCodeWriter scalaSourceCodeWriter(IndentingWriterFactory indentingWriterFactory) {
		return new ScalaSourceCodeWriter(indentingWriterFactory);
	}

	@Bean
	MainSourceCodeProjectContributor<ScalaTypeDeclaration, ScalaCompilationUnit, ScalaSourceCode> mainScalaSourceCodeProjectContributor(
			ObjectProvider<MainApplicationTypeCustomizer<?>> mainApplicationTypeCustomizers,
			ObjectProvider<MainCompilationUnitCustomizer<?, ?>> mainCompilationUnitCustomizers,
			ObjectProvider<MainSourceCodeCustomizer<?, ?, ?>> mainSourceCodeCustomizers,
			ScalaSourceCodeWriter scalaSourceCodeWriter) {
		return new MainSourceCodeProjectContributor<>(this.description, ScalaSourceCode::new, scalaSourceCodeWriter,
				mainApplicationTypeCustomizers, mainCompilationUnitCustomizers, mainSourceCodeCustomizers);
	}

	@Bean
	TestSourceCodeProjectContributor<ScalaTypeDeclaration, ScalaCompilationUnit, ScalaSourceCode> testScalaSourceCodeProjectContributor(
			ObjectProvider<TestApplicationTypeCustomizer<?>> testApplicationTypeCustomizers,
			ObjectProvider<TestSourceCodeCustomizer<?, ?, ?>> testSourceCodeCustomizers,
			ScalaSourceCodeWriter scalaSourceCodeWriter) {
		return new TestSourceCodeProjectContributor<>(this.description, ScalaSourceCode::new, scalaSourceCodeWriter,
				testApplicationTypeCustomizers, testSourceCodeCustomizers);
	}

}
