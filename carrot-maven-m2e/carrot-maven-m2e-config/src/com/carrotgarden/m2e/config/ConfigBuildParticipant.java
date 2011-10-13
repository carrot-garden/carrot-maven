package com.carrotgarden.m2e.config;

import java.io.File;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

public class ConfigBuildParticipant extends MojoExecutionBuildParticipant {

	private static final Logger log = LoggerFactory
			.getLogger(ConfigBuildParticipant.class);

	private static final String ANNOTATIONS = "org.apache.felix.scr.annotations";

	private final static Set<IProject> NOOP = null;

	public ConfigBuildParticipant(final MojoExecution execution) {
		super(execution, true);
	}

	private boolean isValid(final String text) {
		return text != null && text.length() > 0;
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

	private boolean isInteresing(final File file) throws Exception {

		final String filePath = file.getAbsolutePath();

		if (!filePath.endsWith("java")) {
			return false;
		}

		final String text = FileUtil.readTextFile(file);

		if (!hasAnnotations(text)) {
			return false;
		}

		return true;
	}

	/**
	 * @see <a href=
	 *      "https://github.com/sonatype/sisu-build-api/tree/master/src/main/java/org/sonatype/plexus/build/incremental"
	 *      />
	 */
	@Override
	public Set<IProject> build(final int kind, final IProgressMonitor monitor)
			throws Exception {

		final IMaven maven = MavenPlugin.getMaven();
		final Settings settings = maven.getSettings();

		final BuildContext buildContext = getBuildContext();
		final MavenSession session = getSession();
		final MojoExecution execution = getMojoExecution();
		final MavenProject project = session.getCurrentProject();

		log.info("### build : {}", project);

		//

		final List<String> sourceRoots = project.getCompileSourceRoots();

		if (!isValid(sourceRoots)) {
			log.warn("not valid source roots");
			return NOOP;
		}

		int count = 0;

		for (final String rootPath : sourceRoots) {

			if (!isValid(rootPath)) {
				log.warn("not valid root path");
				continue;
			}

			log.info("### rootPath : {}", rootPath);

			final File rootDir = new File(rootPath);

			final Scanner scanner = buildContext.newScanner(rootDir);

			scanner.scan();

			final String[] includedFiles = scanner.getIncludedFiles();

			if (!isValid(includedFiles)) {
				log.warn("not valid included files");
				continue;
			}

			for (final String relativePath : includedFiles) {

				final File file = new File(rootDir, relativePath);

				log.info("### file : {}", file);

				if (isInteresing(file)) {
					count++;
				}

			}

		}

		if (count == 0) {
			log.warn("no interesting files");
			return NOOP;
		}

		log.info("### project : {}", project);
		log.info("### execution : {}", execution);
		log.info("### isIncremental : {}", buildContext.isIncremental());

		final MavenContext context = new MavenContext(maven, session, execution);

		final String key = context.getKey();

		MavenJob job = (MavenJob) buildContext.getValue(key);

		if (job != null) {
			job.cancel();
		}

		job = new MavenJob(context);

		buildContext.setValue(key, job);

		job.schedule(1 * 1000);

		return NOOP;

	}

}
