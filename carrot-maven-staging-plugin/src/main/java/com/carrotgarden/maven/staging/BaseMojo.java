/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.staging;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;

/**
 */
public abstract class BaseMojo extends AbstractMojo {

	/**
	 * @parameter expression="${localRepository}"
	 * @required
	 * @readonly
	 */
	protected ArtifactRepository localRepo;
	/**
	 * The Maven BuildPluginManager component.
	 * 
	 * @component
	 * @required
	 */
	protected BuildPluginManager manager;
	/**
	 * The project currently being build.
	 * 
	 * @parameter default-value="${project}"
	 * @parameter required
	 * @readonly
	 */
	protected MavenProject project;
	/**
	 * @parameter default-value="${project.remotePluginRepositories}"
	 * @readonly
	 */
	protected List<RemoteRepository> remoteRepoList;
	/**
	 * @parameter default-value="${repositorySystemSession}"
	 * @readonly
	 */
	protected RepositorySystemSession repoSession;
	/**
	 * @component
	 * @readonly
	 */
	protected RepositorySystem repoSystem;
	/**
	 * The current Maven session.
	 * 
	 * @parameter default-value="${session}"
	 * @parameter required
	 * @readonly
	 */
	protected MavenSession session;
	/**
	 * 
	 * artifact to copy
	 * 
	 * @parameter default-value="${project.artifactId}"
	 * @required
	 */
	protected String stagingArtifactId;
	/**
	 * * artifact to copy
	 * 
	 * @parameter default-value="${project.groupId}"
	 * @required
	 */
	protected String stagingGroupId;
	/**
	 * artifact to copy
	 * 
	 * @parameter default-value="${project.version}"
	 * @required
	 */
	protected String stagingVersion;
	/**
	 * @parameter default-value="${project.build.directory}/sonatype-staging"
	 * @required
	 */
	protected File workingFolder;

	protected boolean isResolved(final Artifact artifact) {

		final ArtifactRequest request = new ArtifactRequest();

		request.setArtifact(artifact);

		request.setRepositories(remoteRepoList);

		try {

			final ArtifactResult result = //
			repoSystem.resolveArtifact(repoSession, request);

			return result.isResolved();

		} catch (final ArtifactResolutionException e) {

			getLog().warn("missing artifact : " + artifact);

			return false;

		}

	}

	protected Artifact resolved(final Artifact artifact) {

		final ArtifactRequest request = new ArtifactRequest();

		request.setArtifact(artifact);

		request.setRepositories(remoteRepoList);

		try {

			final ArtifactResult result = //
			repoSystem.resolveArtifact(repoSession, request);

			if (result.isResolved()) {
				return result.getArtifact();
			}

		} catch (final ArtifactResolutionException e) {

			getLog().warn("missing artifact : " + artifact);

		}

		return null;

	}

	protected boolean isPackagingPom() {
		return "pom".equals(project.getPackaging());
	}

	/** working folder */
	protected String localPath() {
		return workingFolder.getAbsolutePath();
	}

	/** working folder artifact */
	protected String localPath(final String artifact) {
		return new File(workingFolder, artifact).getAbsolutePath();
	}

	/** artifact familty identity */
	protected String artifactPrefix() {
		return stagingArtifactId + "-" + stagingVersion;
	}

	/** relative folder path on source or target server */
	protected String remotePath() {
	
		final String groupPath = stagingGroupId.replaceAll("\\.", "/");
	
		final String artifactPath = stagingArtifactId + "/" + stagingVersion;
	
		final String remotePath = groupPath + "/" + artifactPath;
	
		return remotePath;
	
	}

	/** relative artifact path on source or target server */
	protected String remotePath(final String artifact) {
		return remotePath() + "/" + artifact;
	}

}
