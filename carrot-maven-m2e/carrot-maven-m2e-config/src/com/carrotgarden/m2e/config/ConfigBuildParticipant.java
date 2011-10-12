package com.carrotgarden.m2e.config;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.Scanner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.sonatype.plexus.build.incremental.BuildContext;

public class ConfigBuildParticipant extends MojoExecutionBuildParticipant {

	private static final String ANNOTATIONS = "org.apache.felix.scr.annotations";

	private final static Set<IProject> NOOP = null;

	public ConfigBuildParticipant(final MojoExecution execution) {
		super(execution, true);
	}

	private boolean isValid(final Set<?> set) {
		return set != null && set.size() > 0;
	}

	private boolean isValid(final List<?> list) {
		return list != null && list.size() > 0;
	}

	private boolean isValid(final Object[] array) {
		return array != null && array.length > 0;
	}

	private boolean hasAnnotations(final String text) {
		return text.contains(ANNOTATIONS);
	}

	private boolean isInteresing(final String filePath) throws Exception {

		if (filePath == null) {
			return false;
		}

		if (!filePath.endsWith("java")) {
			return false;
		}

		final File file = new File(filePath);

		final String text = FileUtil.readTextFile(file);

		if (!hasAnnotations(text)) {
			return false;
		}

		return true;
	}

	@Override
	public Set<IProject> build(final int kind, final IProgressMonitor monitor)
			throws Exception {

		final IMaven maven = MavenPlugin.getMaven();
		final Settings settings = maven.getSettings();
		final BuildContext buildContext = getBuildContext();
		final MavenSession session = getSession();
		final MojoExecution execution = getMojoExecution();
		final MavenProject project = session.getCurrentProject();

		//

		final List<String> sourceRoots = project.getCompileSourceRoots();

		if (!isValid(sourceRoots)) {
			return NOOP;
		}

		final Set<IProject> buildSet = new HashSet<IProject>();

		for (final String rootPath : sourceRoots) {

			final File rootFile = new File(rootPath);

			final Scanner scanner = buildContext.newScanner(rootFile);

			scanner.scan();

			final String[] includedFiles = scanner.getIncludedFiles();

			if (!isValid(includedFiles)) {
				continue;
			}

			int count = 0;
			for (final String filePath : includedFiles) {
				if (isInteresing(filePath)) {
					count++;
				}
			}

			if (count == 0) {
				continue;
			}

			final Set<IProject> rootSet = super.build(kind, monitor);

			if (!isValid(rootSet)) {
				continue;
			}

			buildSet.addAll(rootSet);

		}

		return buildSet;

	}

}
