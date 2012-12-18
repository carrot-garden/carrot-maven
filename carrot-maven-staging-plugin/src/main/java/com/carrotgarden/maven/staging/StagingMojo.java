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

/**
 * 
 * @goal sonatype-staging
 */
public class StagingMojo extends BaseMojo {

	/**
	 * @parameter
	 * @required
	 */
	protected Plugin dependPlugin;

	/**
	 * @parameter
	 * @required
	 */
	protected Plugin signerPlugin;

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
	 * @parameter default-value="jar,sources:jar,javadoc:jar"
	 * @required
	 */
	protected String stagingSearchList;

	/**
	 * @parameter default-value="copy"
	 * @required
	 */
	protected String dependGoal;

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
				"", //
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

		getLog().info("### find pom");

		executeDepend(stagingPom());

		if (isPackagingPom()) {

			getLog().info("### pom only");

			executeSigner(stagingPom());

		} else {

			final List<Artifact> artifactList = artifactList();

			getLog().info("### pom and artifact " + artifactList.size());

			for (final Artifact artifact : artifactList) {

				executeDepend(artifact);

			}

			executeSigner(stagingArtifact());

		}

		getLog().info("### close");

		executeNexus();

		getLog().info("### done");

	}

	protected void executeDepend(final Artifact artifact)
			throws MojoExecutionException {

		executeMojo(dependPlugin, dependGoal, //

				configuration( //
				dependArtifactItemList(stagingFolder, artifact) //
				), //

				executionEnvironment(project, session, manager) //

		);

	}

	protected void executeSigner(final Artifact artifact)
			throws MojoExecutionException {

		executeMojo(signerPlugin,
				signerGoal, //

				configuration(
						signerURL(stagingDeployURL), //
						signerRepoId(stagingServerId), //
						signerFile(stagingFolder, artifact), //
						signerPomFile(artifactFile(stagingFolder, stagingPom())) //
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
