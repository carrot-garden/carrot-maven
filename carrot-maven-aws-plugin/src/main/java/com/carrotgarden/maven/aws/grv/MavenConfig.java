package com.carrotgarden.maven.aws.grv;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

public class MavenConfig {

	public final MavenSession session;
	public final MavenProject project;

	public final boolean isSystem;
	public final boolean isCommand;
	public final boolean isProject;

	public MavenConfig(//
			final MavenSession session, //
			final MavenProject project, //
			final boolean isSystem,//
			final boolean isCommand, //
			final boolean isProject //
	) {

		this.session = session;
		this.project = project;

		this.isSystem = isSystem;
		this.isCommand = isCommand;
		this.isProject = isProject;

	}

}
