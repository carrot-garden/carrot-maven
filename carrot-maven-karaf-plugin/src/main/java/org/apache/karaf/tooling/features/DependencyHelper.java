/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package org.apache.karaf.tooling.features;

import static org.apache.karaf.deployer.kar.KarArtifactInstaller.*;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionContext;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.collection.DependencyGraphTransformer;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.graph.selector.AndDependencySelector;
import org.sonatype.aether.util.graph.selector.ExclusionDependencySelector;
import org.sonatype.aether.util.graph.selector.OptionalDependencySelector;
import org.sonatype.aether.util.graph.selector.ScopeDependencySelector;
import org.sonatype.aether.util.graph.transformer.ChainedDependencyGraphTransformer;
import org.sonatype.aether.util.graph.transformer.ConflictMarker;
import org.sonatype.aether.util.graph.transformer.JavaDependencyContextRefiner;
import org.sonatype.aether.util.graph.transformer.JavaEffectiveScopeCalculator;

/**
 * @version $Rev:ision$
 */
public class DependencyHelper {

	/**
	 * The entry point to Aether, i.e. the component doing all the work.
	 * 
	 * @component
	 * @required
	 * @readonly
	 */
	private final RepositorySystem repoSystem;

	/**
	 * The current repository/network configuration of Maven.
	 * 
	 * @parameter default-value="${repositorySystemSession}"
	 * @required
	 * @readonly
	 */
	private final RepositorySystemSession repoSession;

	/**
	 * The project's remote repositories to use for the resolution of project
	 * dependencies.
	 * 
	 * @parameter default-value="${project.remoteProjectRepositories}"
	 * @readonly
	 */
	private final List<RemoteRepository> projectRepos;

	/**
	 * The project's remote repositories to use for the resolution of plugins
	 * and their dependencies.
	 * 
	 * @parameter default-value="${project.remotePluginRepositories}"
	 * @required
	 * @readonly
	 */
	private final List<RemoteRepository> pluginRepos;

	// dependencies we are interested in
	protected Map<Artifact, String> localDependencies;

	// log of what happened during search
	protected String treeListing;

	public DependencyHelper(final List<RemoteRepository> pluginRepos,
			final List<RemoteRepository> projectRepos,
			final RepositorySystemSession repoSession,
			final RepositorySystem repoSystem) {

		this.pluginRepos = pluginRepos;
		this.projectRepos = projectRepos;
		this.repoSession = repoSession;
		this.repoSystem = repoSystem;

	}

	public Map<Artifact, String> getLocalDependencies() {
		return localDependencies;
	}

	public String getTreeListing() {
		return treeListing;
	}

	// artifact search code adapted from geronimo car plugin

	private void log(final String text) {
		System.err.println("### " + text);
	}

	private void print(final DependencyNode root, final String margin) {

		final Dependency dependency = root.getDependency();

		final Artifact artifact = dependency.getArtifact();
		final String scope = dependency.getScope();

		log(margin + artifact + " @ " + scope);

		for (final DependencyNode next : root.getChildren()) {
			print(next, "   ");
		}

	}

	public void getDependencies(final MavenProject project,
			final boolean useTransitiveDependencies)
			throws MojoExecutionException {

		final DependencyNode rootNode = getDependencyTree(RepositoryUtils
				.toArtifact(project.getArtifact()));

		print(rootNode, "");

		final Scanner scanner = new Scanner();

		scanner.scan(rootNode, useTransitiveDependencies);

		localDependencies = scanner.localDependencies;

		treeListing = scanner.getLog();

	}

	private DependencyNode getDependencyTree(final Artifact artifact)
			throws MojoExecutionException {
		try {

			final CollectRequest collectRequest = new CollectRequest(
					new Dependency(artifact, "compile"), null, projectRepos);

			final DefaultRepositorySystemSession session = new DefaultRepositorySystemSession(
					repoSession);

			session.setDependencySelector(new AndDependencySelector(
					new OptionalDependencySelector(),
					new ScopeDependencySelector("test", "provided", "system"),
					new ExclusionDependencySelector(null)));

			final DependencyGraphTransformer transformer = new ChainedDependencyGraphTransformer(
					new ConflictMarker(), new JavaEffectiveScopeCalculator(),
					new JavaDependencyContextRefiner());

			session.setDependencyGraphTransformer(transformer);

			final CollectResult result = repoSystem.collectDependencies(
					session, collectRequest);

			return result.getRoot();

		} catch (final DependencyCollectionException e) {
			throw new MojoExecutionException(
					"Cannot build project dependency tree", e);
		}
	}

	// aether's ScopeDependencySelector appears to always exclude the configured
	// scopes (test and provided) and there is no way to configure it to
	// accept the top level provided scope dependencies. We need this 3 layer
	// cake since aether never actually uses the top level selector you give it,
	// it always starts by getting the child to apply to the project's
	// dependencies.
	private static class ScopeDependencySelector1 implements DependencySelector {

		private final DependencySelector child = new ScopeDependencySelector2();

		@Override
		public boolean selectDependency(final Dependency dependency) {
			throw new IllegalStateException("this does not appear to be called");
		}

		@Override
		public DependencySelector deriveChildSelector(
				final DependencyCollectionContext context) {
			return child;
		}
	}

	private static class ScopeDependencySelector2 implements DependencySelector {

		private final DependencySelector child = new ScopeDependencySelector3();

		@Override
		public boolean selectDependency(final Dependency dependency) {
			final String scope = dependency.getScope();
			return !"test".equals(scope) && !"runtime".equals(scope);
		}

		@Override
		public DependencySelector deriveChildSelector(
				final DependencyCollectionContext context) {
			return child;
		}
	}

	private static class ScopeDependencySelector3 implements DependencySelector {

		@Override
		public boolean selectDependency(final Dependency dependency) {
			final String scope = dependency.getScope();
			return !"test".equals(scope) && !"provided".equals(scope)
					&& !"runtime".equals(scope);
		}

		@Override
		public DependencySelector deriveChildSelector(
				final DependencyCollectionContext context) {
			return this;
		}
	}

	private static class Scanner {

		private static enum Accept {

			ACCEPT(true, true), PROVIDED(true, false), STOP(false, false);

			private final boolean more;
			private final boolean local;

			private Accept(final boolean more, final boolean local) {
				this.more = more;
				this.local = local;
			}

			public boolean isContinue() {
				return more;
			}

			public boolean isLocal() {
				return local;
			}
		}

		// all the dependencies needed for this car, with provided dependencies
		// removed. artifact to scope map
		private final Map<Artifact, String> localDependencies = new LinkedHashMap<Artifact, String>();

		// dependencies from ancestor cars, to be removed from
		// localDependencies.
		private final Set<Artifact> carDependencies = new LinkedHashSet<Artifact>();

		private final StringBuilder log = new StringBuilder();

		public void scan(final DependencyNode rootNode,
				final boolean useTransitiveDependencies)
				throws MojoExecutionException {

			for (final DependencyNode child : rootNode.getChildren()) {
				scan(child, Accept.ACCEPT, useTransitiveDependencies, false, "");
			}

			if (useTransitiveDependencies) {
				localDependencies.keySet().removeAll(carDependencies);
			}

		}

		private void scan(final DependencyNode dependencyNode,
				final Accept parentAccept,
				final boolean useTransitiveDependencies, boolean isFromFeature,
				final String indent) throws MojoExecutionException {

			// Artifact artifact = getArtifact(rootNode);

			final Accept accept = accept(dependencyNode, parentAccept);

			if (accept.isLocal()) {

				if (isFromFeature) {
					if (!isFeature(dependencyNode)) {
						log.append(indent).append("from feature:")
								.append(dependencyNode).append("\n");
						carDependencies.add(dependencyNode.getDependency()
								.getArtifact());
					} else {
						log.append(indent).append("is feature:")
								.append(dependencyNode).append("\n");
					}
				} else {
					log.append(indent).append("local:").append(dependencyNode)
							.append("\n");
					if (carDependencies.contains(dependencyNode.getDependency()
							.getArtifact())) {
						log.append(indent)
								.append("already in feature, returning:")
								.append(dependencyNode).append("\n");
						return;
					}

					// TODO resolve scope conflicts
					localDependencies.put(dependencyNode.getDependency()
							.getArtifact(), dependencyNode.getDependency()
							.getScope());

					if (isFeature(dependencyNode) || !useTransitiveDependencies) {
						isFromFeature = true;
					}

				}

				if (accept.isContinue()) {
					final List<DependencyNode> children = dependencyNode
							.getChildren();
					for (final DependencyNode child : children) {
						scan(child, accept, useTransitiveDependencies,
								isFromFeature, indent + "  ");
					}
				}

			}

		}

		public String getLog() {
			return log.toString();
		}

		private Accept accept(final DependencyNode node, final Accept previous) {

			// final String scope = node.getPremanagedScope();
			final String scope = node.getDependency().getScope();

			if (scope == null || "runtime".equalsIgnoreCase(scope)
					|| "compile".equalsIgnoreCase(scope)) {
				return previous;
			}

			if ("provided".equalsIgnoreCase(scope)) {
				return Accept.PROVIDED;
			}

			return Accept.STOP;

		}

	}

	public static boolean isFeature(final DependencyNode dependencyNode) {
		return isFeature(dependencyNode.getDependency().getArtifact());
	}

	public static boolean isFeature(final Artifact artifact) {
		return artifact.getExtension().equals("kar")
				|| FEATURE_CLASSIFIER.equals(artifact.getClassifier());
	}

}
