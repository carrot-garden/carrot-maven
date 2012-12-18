/*
 * Copyright 2008-2011 Don Brown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.carrotgarden.maven.staging;

import static com.carrotgarden.maven.staging.Util.*;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

/**
 * 
 * @goal sonatype-staging
 */
public class StagingMojo extends BaseMojo {

	/**
	 * @parameter
	 * @required
	 */
	// protected Plugin dependPlugin;

	/**
	 * @parameter
	 * @required
	 */
	// protected Plugin signerPlugin;

	/**
	 * @parameter
	 * @required
	 */
	protected Plugin deployPlugin;

	/**
	 * @parameter
	 * @required
	 */
	protected Plugin nexusPlugin;

	/**
	 * @parameter default-value="${project.groupId}"
	 * @required
	 */
	protected String stagingGroupId;
	/**
	 * @parameter default-value="${project.artifactId}"
	 * @required
	 */
	protected String stagingArtifactId;

	/**
	 * @parameter default-value="${project.version}"
	 * @required
	 */
	protected String stagingVersion;

	/**
	 * @parameter default-value="jar"
	 * @required
	 */
	protected String stagingExtension;

	/**
	 * @parameter default-value="pom,jar,sources:jar,javadoc:jar"
	 * @required
	 */
	protected String stagingSearchList;

	/**
	 * @parameter default-value="copy"
	 * @required
	 */
	protected String dependGoal;

	/**
	 * @parameter default-value="deploy-file"
	 * @required
	 */
	protected String deployGoal;

	/**
	 * @parameter default-value="staging-close"
	 * @required
	 */
	protected String nexusGoal;

	/**
	 * @parameter default-value="sign-and-deploy-file"
	 * @required
	 */
	protected String signerGoal;

	/**
	 * @parameter default-value="${project.build.directory}/sonatype-staging"
	 * @required
	 */
	protected File stagingFolder;

	/**
	 * @parameter default-value= "https://oss.sonatype.org/"
	 * @required
	 */
	protected String stagingNexusURL;
	/**
	 * @parameter default-value=
	 *            "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
	 * @required
	 */
	protected String stagingDeployURL;

	/**
	 * @parameter default-value="sonatype-nexus-staging"
	 * @required
	 */
	protected String stagingServerId;

	//

	protected List<Artifact> artifactList() {

		final List<Artifact> artifactList = new ArrayList<Artifact>();

		final List<Tuple> tupleList = Tuple.fromList(stagingSearchList);

		for (final Tuple tuple : tupleList) {

			final Artifact artifact = new DefaultArtifact( //
					stagingGroupId, //
					stagingArtifactId, //
					tuple.classifier, //
					tuple.extension, //
					stagingVersion //
			);

			if (!isResolved(artifact)) {
				continue;
			}

			artifactList.add(artifact);

		}

		return artifactList;

	}

	protected Artifact stagingPom() {

		final Artifact artifact = new DefaultArtifact( //
				stagingGroupId, //
				stagingArtifactId, //
				null, //
				"pom", //
				stagingVersion //
		);

		return artifact;

	}

	protected Artifact stagingArtifact() {

		final Artifact artifact = new DefaultArtifact( //
				stagingGroupId, //
				stagingArtifactId, //
				"", //
				stagingExtension, //
				stagingVersion //
		);

		return artifact;

	}

	protected void assertStagingPom() throws MojoExecutionException {
		if (isResolved(stagingPom())) {
			return;
		}
		final String message = "" + stagingPom();
		getLog().error(message);
		throw new MojoExecutionException(message);
	}

	@Override
	public void execute() throws MojoExecutionException {

		getLog().info("### init");

		assertStagingPom();

		getLog().info("### get pom");

		// executeDepend(stagingPom());

		// if (isPackagingPom()) {
		// executePom();
		// } else {
		// executeJar();
		// }

		executeArtifact();

		getLog().info("### close");

		executeNexus();

		getLog().info("### done");

	}

	protected void executePom() throws MojoExecutionException {

		executeSigner(stagingPom());

	}

	protected void executeArtifact() throws MojoExecutionException {

		final List<Artifact> artifactList = artifactList();

		getLog().info("### put list " + artifactList.size());

		for (final Artifact artifact : artifactList) {

			getLog().info("### get artifact=" + artifact);

			// executeDepend(artifact);

		}

		for (final Artifact artifact : artifactList) {

			getLog().info("### put artifact=" + artifact);

			executeDeploy(artifact);

		}

	}

	protected void executeDepend(final Artifact artifact)
			throws MojoExecutionException {

		/** XXX */
		executeMojo(null, dependGoal, //

				configuration( //
				dependArtifactItemList(stagingFolder, artifact) //
				), //

				executionEnvironment(project, session, manager) //

		);

	}

	protected void executeDeploy(final Artifact artifact)
			throws MojoExecutionException {

		// element("pomFile", artifactFile(stagingFolder,
		// stagingPom())), //

		final String fileOne = artifact.getFile().getAbsolutePath();

		final String fileTwo = fileOne + ".asc";

		final Element[] configOne = new Element[] {
				element("groupId", artifact.getGroupId()), //
				element("artifactId", artifact.getArtifactId()), //
				element("version", artifact.getVersion()), //
				element("classifier", artifact.getClassifier()), //
				element("packaging", artifact.getExtension()), //
				element("generatePom", "false"), //
				element("file", fileOne), //
				element("url", stagingDeployURL), //
				element("repositoryId", stagingServerId) //
		};

		final Element[] configTwo = new Element[] {
				element("groupId", artifact.getGroupId()), //
				element("artifactId", artifact.getArtifactId()), //
				element("version", artifact.getVersion()), //
				element("classifier", artifact.getClassifier()), //
				element("packaging", artifact.getExtension()), //
				element("generatePom", "false"), //
				element("file", fileTwo), //
				element("url", stagingDeployURL), //
				element("repositoryId", stagingServerId) //
		};

		executeMojo(deployPlugin, deployGoal, configuration(configOne),
				executionEnvironment(project, session, manager) //
		);

		executeMojo(deployPlugin, deployGoal, configuration(configTwo),
				executionEnvironment(project, session, manager) //
		);

	}

	protected void executeSigner(final Artifact artifact)
			throws MojoExecutionException {

		/** XXX */
		executeMojo(null, signerGoal,

		configuration( //
				element("groupId", artifact.getGroupId()), //
				element("artifactId", artifact.getArtifactId()), //
				element("version", artifact.getVersion()), //
				element("classifier", artifact.getClassifier()), //
				element("packaging", artifact.getExtension()), //
				//
				element("file", artifactFile(stagingFolder, artifact)), //
				element("pomFile", artifactFile(stagingFolder, stagingPom())), //
				//
				element("url", stagingDeployURL), //
				element("repositoryId", stagingServerId) //
				), //

				executionEnvironment(project, session, manager) //

		);

	}

	protected void executeNexus() throws MojoExecutionException {

		executeMojo(nexusPlugin, nexusGoal, //

				configuration( //
						//
						element("automatic", "true"), //
						//
						element("groupId", stagingGroupId), //
						element("artifactId", stagingArtifactId), //
						element("version", stagingVersion), //
						//
						element("nexusUrl", stagingNexusURL), //
						element("serverAuthId", stagingServerId) //
				), //

				executionEnvironment(project, session, manager) //

		);

	}

}
