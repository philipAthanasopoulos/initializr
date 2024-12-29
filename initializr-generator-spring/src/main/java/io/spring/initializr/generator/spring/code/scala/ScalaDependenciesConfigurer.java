package io.spring.initializr.generator.spring.code.scala;

import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.DependencyScope;
import io.spring.initializr.generator.spring.build.BuildCustomizer;

public class ScalaDependenciesConfigurer implements BuildCustomizer<Build> {

	@Override
	public void customize(Build build) {
		String groupId = "org.scala-lang";
		build.dependencies().add("scala-library", groupId, "scala-library", DependencyScope.COMPILE);
	}

}