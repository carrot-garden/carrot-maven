package com.carrotgarden.m2e.config;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
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

	private final static Set<IProject> NOOP = null;

	public ConfigBuildParticipant(final MojoExecution execution) {
		super(execution, true);
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

		final BuildContext buildContext = getBuildContext();
		final MavenSession session = getSession();
		final MojoExecution execution = getMojoExecution();
		final MavenProject project = session.getCurrentProject();

		log.info("### project : {}", project);
		log.info("### execution : {}", execution);
		log.info("### isIncremental : {}", buildContext.isIncremental());

		//

		final List<String> sourceRoots = project.getCompileSourceRoots();

		if (!MojoUtil.isValid(sourceRoots)) {
			log.warn("### not valid source roots");
			return NOOP;
		}

		int countSCR = 0;
		int countBND = 0;

		for (final String rootPath : sourceRoots) {

			if (!MojoUtil.isValid(rootPath)) {
				log.warn("### not valid root path");
				continue;
			}

			log.info("### rootPath : {}", rootPath);

			final File rootDir = new File(rootPath);

			final Scanner scanner = buildContext.newScanner(rootDir);

			scanner.scan();

			final String[] includedFiles = scanner.getIncludedFiles();

			if (!MojoUtil.isValid(includedFiles)) {
				log.warn("not valid included files");
				continue;
			}

			for (final String relativePath : includedFiles) {

				final File file = new File(rootDir, relativePath);

				log.info("### file : {}", file);

				if (MojoUtil.isInterestSCR(file)) {
					countSCR++;
				}

				if (MojoUtil.isInterestBND(file)) {
					countBND++;
				}

			}

		}

		final MavenContext context = new MavenContext(maven, session, execution);

		final boolean hasSCR = countSCR > 0 && MojoUtil.isMojoSCR(context);
		final boolean hasBND = countBND > 0 && MojoUtil.isMojoBND(context);

		if (hasSCR || hasBND) {

			final String key = context.getKey();

			MavenJob job = (MavenJob) buildContext.getValue(key);

			if (job != null) {
				job.cancel();
			}

			job = new MavenJob(context);

			buildContext.setValue(key, job);

			job.schedule(1 * 1000);

		} else {

			log.warn("### no interesting files");

		}

		return NOOP;

	}

}
