package com.carrotgarden.m2e.config;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.embedder.IMaven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenContext {

	private static final Logger log = LoggerFactory
			.getLogger(MavenContext.class);

	final IMaven maven;

	final MavenSession session;

	final MojoExecution execution;

	MavenContext(final IMaven maven, final MavenSession session,
			final MojoExecution execution) {

		this.maven = maven;
		this.session = session;
		this.execution = execution;

	}

	String getKey() {

		final MavenProject project = session.getCurrentProject();

		final String projId = project.getId();

		final String execId = execution.toString();

		final String key = projId + "/" + execId;

		return key;

	}

	void execute(final IProgressMonitor monitor) {

		log.debug("### EXECUTE @ CONTEXT");

		maven.execute(session, execution, monitor);

	}

	void cancel() {

		log.debug("### CANCEL  @ CONTEXT");

	}

}
