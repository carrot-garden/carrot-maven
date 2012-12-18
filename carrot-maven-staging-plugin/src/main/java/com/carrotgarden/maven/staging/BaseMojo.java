package com.carrotgarden.maven.staging;

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

public abstract class BaseMojo extends AbstractMojo {

	/**
	 * @parameter expression="${localRepository}"
	 * @required
	 * @readonly
	 */
	protected ArtifactRepository localRepository;
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
	protected List<RemoteRepository> remoteRepos;
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

	protected boolean isPackagingPom() {
		return "pom".equals(project.getPackaging());
	}

}
