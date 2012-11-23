package com.carrotgarden.maven.aws.grv;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * groovy script executor; has access to {@link MavenProject}; for example:
 * 
 * <pre>
 * def value = project.properties['key'] // read from project
 * project.properties['key'] = value + 1 // save into project
 * </pre>
 * 
 * @goal groovy-execute-script
 * 
 * @phase prepare-package
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class CarrotAwsGroovyExec extends CarrotAwsGroovy {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {

			getLog().info("groovy exec init ");

			// logProps("syst", session.getSystemProperties());
			// logProps("user", session.getUserProperties());
			// logProps("proj", project.getProperties());

			final GroovyRunner runner = newRunner();

			if (groovyFile != null && groovyFile.exists()) {
				getLog().info("groovy exec file : " + groovyFile);
				runner.execute(groovyFile);
			}

			if (groovyText != null && groovyText.length() != 0) {
				getLog().info(
						"groovy exec text : "
								+ groovyText.replaceAll("\n", ";"));
				runner.execute(groovyText);
			}

			getLog().info("groovy exec done ");

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}

}
