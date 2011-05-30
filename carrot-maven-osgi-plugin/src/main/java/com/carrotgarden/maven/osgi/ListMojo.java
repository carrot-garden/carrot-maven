package com.carrotgarden.maven.osgi;

import java.util.HashSet;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * 
 * @goal list
 * @aggregator false
 * 
 * @requiresProject true
 * @requiresDependencyResolution test
 * 
 */
public class ListMojo extends BaseMojo {

	/**
	 * 
	 * @parameter
	 */
	private boolean isDescriptor;

	/**
	 * 
	 * @parameter
	 */
	private boolean isPublicator;

	@Override
	public void execute() throws MojoExecutionException {

		getLog().info("### mark=" + mark);
		getLog().info("### isExecutionRoot=" + m_project.isExecutionRoot());
		getLog().info("### isDescriptor=" + isDescriptor);
		getLog().info("### isPublicator=" + isPublicator);

		try {

			bundleIdSet = new HashSet<String>();

			// for (final MavenProject project : reactorProjects) {
			// getLog().info("project : " + project);
			// }

			addProjectBundles(m_project, TAB);

		} catch (Exception e) {

			throw new MojoExecutionException("bada-boom", e);

		}

	}

}
