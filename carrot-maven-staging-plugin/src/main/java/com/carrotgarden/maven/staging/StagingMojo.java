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

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

/**
 * 
 * @goal carrot-staging
 */
public class StagingMojo extends AbstractMojo {

	/**
	 * The project currently being build.
	 * 
	 * @parameter default-value="${project}"
	 * @parameter required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * The current Maven session.
	 * 
	 * @parameter default-value="${session}"
	 * @parameter required
	 * @readonly
	 */
	protected MavenSession session;

	/**
	 * The Maven BuildPluginManager component.
	 * 
	 * @component
	 * @required
	 */
	protected BuildPluginManager manager;

	//

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

	//

	/**
	 * @component
	 * @readonly
	 */
	protected RepositorySystem repoSystem;

	/**
	 * @parameter default-value="${repositorySystemSession}"
	 * @readonly
	 */
	protected RepositorySystemSession repoSession;

	/**
	 * @parameter default-value="${project.remotePluginRepositories}"
	 * @readonly
	 */
	protected List<RemoteRepository> remoteRepos;

	/**
	 * @parameter expression="${localRepository}"
	 * @required
	 * @readonly
	 */
	protected ArtifactRepository localRepository;

	//

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
	 * @parameter default-value="pom,jar,sources:jar,javadoc:jar"
	 * @required
	 */
	protected String searchList;

	/**
	 * @parameter default-value="copy"
	 * @required
	 */
	protected String dependGoal;

	/**
	 * @parameter default-value="sign-and-deploy-file"
	 * @required
	 */
	protected String signerGoal;

	/**
	 * @parameter default-value="${project.build.directory}/carrot-staging"
	 * @required
	 */
	protected File stagingFolder;

	//

	protected boolean isResolved(final Artifact artifact) {

		final ArtifactRequest request = new ArtifactRequest();

		request.setArtifact(artifact);

		request.setRepositories(remoteRepos);

		try {

			final ArtifactResult result = //
			repoSystem.resolveArtifact(repoSession, request);

			return result.isResolved();

		} catch (final ArtifactResolutionException e) {

			getLog().warn("missing artifact : " + artifact);

			return false;

		}

	}

	protected List<Artifact> artifactList() {

		final List<Artifact> artifactList = new ArrayList<Artifact>();

		final List<Tuple> tupleList = Tuple.fromList(searchList);

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

	protected Element signerFile(final Artifact artifact) {

		final String file = "";

		return element("file", file);

	}

	@Override
	public void execute() throws MojoExecutionException {

		final List<Artifact> artifactList = artifactList();

		for (final Artifact artifact : artifactList) {

			executeDepend(artifact);

			executeSigner(artifact);

		}

	}

	protected void executeDepend(final Artifact artifact)
			throws MojoExecutionException {

		final List<Artifact> artifactList = new ArrayList<Artifact>();
		artifactList.add(artifact);

		final Element artifactItems = //
		Util.artifactItemList(stagingFolder, artifactList);

		executeMojo(dependPlugin, dependGoal, //

				configuration(

				artifactItems

				), //

				executionEnvironment(project, session, manager) //

		);

	}

	protected void executeSigner(final Artifact artifact)
			throws MojoExecutionException {

		final Element signerFile = signerFile(artifact);

		executeMojo(signerPlugin, signerGoal, //

				configuration(

				signerFile

				), //

				executionEnvironment(project, session, manager) //

		);

	}

}
