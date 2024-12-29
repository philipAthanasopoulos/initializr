package io.spring.initializr.generator.spring.code.scala;

import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.spring.build.BuildCustomizer;

public class ScalaMavenBuildCustomizer implements BuildCustomizer<MavenBuild> {

	@Override
	public void customize(MavenBuild build) {
		build.plugins().add("net.alchim31.maven", "scala-maven-plugin", (scalaMavenPlugin) -> {
			scalaMavenPlugin.version("4.5.6");
			scalaMavenPlugin.execution(null,
					(execution) -> execution.goal("compile")
						.goal("testCompile"));
		});
	}

}