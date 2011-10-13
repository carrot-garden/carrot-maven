package com.carrotgarden.m2e.config;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.embedder.IMaven;

public class MavenContext {

	final IMaven maven;

	final MavenSession session;

	final MojoExecution execution;

	MavenContext(final IMaven maven, final MavenSession session,
			final MojoExecution execution) {

		this.maven = maven;
		this.session = session;
		this.execution = execution;

	}

	String getName() {
		return session.getCurrentProject().getName() + " / " + execution;
	}

	void execute(final IProgressMonitor monitor) {

		maven.execute(session, execution, monitor);

	}

}
