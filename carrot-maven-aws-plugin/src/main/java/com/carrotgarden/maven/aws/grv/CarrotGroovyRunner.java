/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.grv;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.impl.MavenLoggerFactory;

/**
 * groovy script executor which exposes maven project
 */
public class CarrotGroovyRunner {

	private final MavenProject project;
	
	private final Log log;

	public CarrotGroovyRunner(final MavenProject project, Log log) {
		this.project = project;
		this.log = log;
	}

	public Binding binding() {

		final Binding binding = new Binding();

		binding.setProperty("project", project);

		return binding;

	}

	public Object execute(final File script) throws Exception {

		final GroovyShell shell = createShell();

		final Object result = shell.evaluate(script);

		return result;

	}

	public Object execute(final String script) throws Exception {

		final GroovyShell shell = createShell();

		final Object result = shell.evaluate(script);

		return result;

	}
	
	public GroovyShell createShell() {
		
		final GroovyShell shell = new GroovyShell(binding());
		shell.getContext().setProperty("log", createLogger());
		return shell;
	}
	
	private Logger createLogger() {
		String loggerName = String.format("%s.%s.Script", project.getGroupId(),
				project.getArtifactId());
		return MavenLoggerFactory.getLogger(loggerName, log);
	}

}
