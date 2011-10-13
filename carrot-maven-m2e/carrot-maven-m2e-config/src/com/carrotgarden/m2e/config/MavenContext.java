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

	private final IMaven maven;

	private final MavenSession session;

	private final MojoExecution execution;

	MavenContext(final IMaven maven, final MavenSession session,
			final MojoExecution execution) {

		this.maven = maven;
		this.session = session;
		this.execution = execution;

	}

	public IMaven getMaven() {
		return maven;
	}

	public MavenSession getSession() {
		return session;
	}

	public MojoExecution getExecution() {
		return execution;
	}

	public String getKey() {

		final MavenProject project = session.getCurrentProject();

		final String projId = project.getId();

		final String execId = execution.toString();

		final String key = projId + "/" + execId;

		return key;

	}

	public void execute(final IProgressMonitor monitor) {

		log.info("### EXECUTE @ CONTEXT : {}", getKey());

		maven.execute(session, execution, monitor);

	}

	public void cancel() {

		log.info("### CANCEL  @ CONTEXT : {}", getKey());

		// FIXME do actual cancel

	}

}
