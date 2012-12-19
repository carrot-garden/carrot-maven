/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.staging;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * close staging repository, if any
 * 
 * @inheritByDefault false
 * 
 * @goal sonatype-staging-close
 */
public class StagingCloseMojo extends BaseMojo {

	/**
	 * provide dependency :
	 * https://repository.sonatype.org/content/sites/maven-sites
	 * /nexus-maven-plugin/
	 * 
	 * @parameter
	 * @required
	 */
	protected Plugin nexusPlugin;

	/**
	 * settings.xml credentials entry for source server
	 * 
	 * @parameter default-value="sonatype-nexusm-staging"
	 * @required
	 */
	protected String sourceServerId;

	/**
	 * source artifact server url
	 * 
	 * @parameter default-value=
	 *            "https://oss.sonatype.org/content/groups/public"
	 * @required
	 */
	protected String sourceServerURL;

	/**
	 * nexus server running staging suite
	 * 
	 * http://www.sonatype.com/books/nexus-book
	 * /reference/staging-sect-intro.html
	 * 
	 * @parameter default-value= "https://oss.sonatype.org/"
	 * @required
	 */
	protected String stagingNexusURL;

	/**
	 * settings.xml credentials entry for target server
	 * 
	 * @parameter default-value="sonatype-nexus-staging"
	 * @required
	 */
	protected String targetServerId;

	/**
	 * source artifact server url
	 * 
	 * 
	 * @parameter default-value=
	 *            "https://oss.sonatype.org/service/local/staging/deploy/maven2"
	 * @required
	 */
	protected String targetServerURL;

	/**
	 * plugin invocation
	 */
	@Override
	public void execute() throws MojoExecutionException {

		getLog().info("### init");

		executeNexus();

		getLog().info("### done");

	}

	/**
	 * close staging target
	 */
	protected void executeNexus() throws MojoExecutionException {

		getLog().info("### nexus");

		executeMojo(nexusPlugin, "staging-close", //

				configuration( //
						//
						element("automatic", "true"), //
						//
						element("groupId", stagingGroupId), //
						element("artifactId", stagingArtifactId), //
						element("version", stagingVersion), //
						//
						element("nexusUrl", stagingNexusURL), //
						element("serverAuthId", targetServerId) //
				), //

				executionEnvironment(project, session, manager) //

		);

	}

}
