package com.carrotgarden.maven.aws.grv;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;

import org.apache.maven.project.MavenProject;

public class GroovyRunner {

	private final MavenProject project;

	public GroovyRunner(final MavenProject project) {
		this.project = project;
	}

	public Binding binding() {

		final Binding binding = new Binding();

		binding.setProperty("project", project);

		return binding;

	}

	public Object execute(final File script) throws Exception {

		final GroovyShell shell = new GroovyShell(binding());

		final Object result = shell.evaluate(script);

		return result;

	}

	public Object execute(final String script) throws Exception {

		final GroovyShell shell = new GroovyShell(binding());

		final Object result = shell.evaluate(script);

		return result;

	}

}
