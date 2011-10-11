package com.carrotgarden.m2e.config;

import java.io.File;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.util.Scanner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.sonatype.plexus.build.incremental.BuildContext;

public class TestBuildParticipant extends MojoExecutionBuildParticipant {

	public TestBuildParticipant(final MojoExecution execution) {
		super(execution, true);
	}

	@Override
	public Set<IProject> build(final int kind, final IProgressMonitor monitor)
			throws Exception {

		final IMaven maven = MavenPlugin.getMaven();
		final BuildContext buildContext = getBuildContext();

		final MavenSession session = getSession();
		final MojoExecution execution = getMojoExecution();

		/** check if any of the files changed */
		final File sourceDirectory = maven.getMojoParameterValue(session, execution,
				"sourceDirectory", File.class);

		/** delta or full scanner */
		final Scanner scanner = buildContext.newScanner(sourceDirectory);

		scanner.scan();

		final String[] includedFiles = scanner.getIncludedFiles();

		/** skip on empty */
		if (includedFiles == null || includedFiles.length <= 0) {
			return null;
		}

		/** execute mojo on non empty */
		final Set<IProject> projectSet = super.build(kind, monitor);

		/** tell m2e builder to refresh generated files */
		final File outputDirectory = maven.getMojoParameterValue(session, execution,
				"outputDirectory", File.class);

		if (outputDirectory != null) {
			buildContext.refresh(outputDirectory);
		}

		return projectSet;

	}

}
