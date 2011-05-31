package com.carrotgarden.maven.osgi;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.ops4j.pax.construct.util.PomUtils;

public abstract class BaseMojo extends AbstractMojo {

	protected final static String TAB = "\t";

	/**
	 * Accumulated set of bundles
	 */
	protected final static Set<Artifact> BUNDLES = new HashSet<Artifact>();

	// #######################################

	/**
	 */
	protected void addProjectBundles(MavenProject project, String tab) {

		if (PomUtils.isBundleProject(//
				project, m_resolver, m_remoteRepos, m_localRepo, true)) {

			getLog().warn(tab + "found bundle : " + project.getId());

			provisionBundle(project.getArtifact(), tab);

		} else {

			getLog().warn(tab + "check non-bundle: " + project.getId());

		}

		addProjectDependencies(project, tab + TAB);

	}

	/**
	 */
	protected void addProjectDependencies(MavenProject project, String tab) {

		// getLog().warn("addProjectDependencies : " + project);

		@SuppressWarnings("unchecked")
		Set<Artifact> artifacts = project.getArtifacts();

		// getLog().warn("artifacts.size : " + artifacts.size());

		for (Artifact artifact : artifacts) {

			if (artifact.isOptional()) {
				continue;
			}

			if (Artifact.SCOPE_TEST.equals(artifact.getScope())) {
				continue;
			}

			provisionBundle(artifact, tab);

		}

	}

	/**
	 */
	protected void provisionBundle(Artifact bundle, String tab) {

		// if ("pom".equals(bundle.getType())) {
		// getLog().warn(tab + "skipping pom : " + bundle);
		// return;
		// }

		/*
		 * force download here, as next check tries to avoid downloading where
		 * possible
		 */

		if (!PomUtils.downloadFile(bundle, m_resolver, m_remoteRepos,
				m_localRepo)) {

			getLog().warn(tab + "skipping missing : " + bundle);

			return;

		}

		if (PomUtils.isBundleArtifact(bundle, m_resolver, m_remoteRepos,
				m_localRepo, true)) {

			String version = PomUtils.getMetaVersion(bundle);

			String id = bundle.getGroupId() + ':' + bundle.getArtifactId()
					+ ':' + version + ':' + bundle.getType();

			if (BUNDLES.add(bundle)) {
				getLog().warn(tab + "using bundle : " + bundle);
			}

		} else {

			getLog().debug(tab + "skipping non-bundle : " + bundle);

		}

	}

	// #######################################

	/**
	 * Component for resolving Maven metadata
	 * 
	 * @component
	 */
	protected ArtifactMetadataSource m_source;
	/**
	 * Component factory for Maven artifacts
	 * 
	 * @component
	 */
	protected ArtifactFactory m_factory;
	/**
	 * Component for resolving Maven artifacts
	 * 
	 * @component
	 */
	protected ArtifactResolver m_resolver;
	/**
	 * Component for installing Maven artifacts
	 * 
	 * @component
	 */
	protected ArtifactInstaller m_installer;
	/**
	 * Component factory for Maven projects
	 * 
	 * @component
	 */
	protected MavenProjectBuilder m_projectBuilder;
	/**
	 * The local Maven settings.
	 * 
	 * @parameter expression="${settings}"
	 * @required
	 * @readonly
	 */
	protected Settings m_settings;
	/**
	 * List of remote Maven repositories for the containing project.
	 * 
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 * @required
	 * @readonly
	 */
	protected List m_remoteRepos;
	/**
	 * The local Maven repository for the containing project.
	 * 
	 * @parameter expression="${localRepository}"
	 * @required
	 * @readonly
	 */
	protected ArtifactRepository m_localRepo;
	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject m_project;
	/**
	 * The current Maven reactor.
	 * 
	 * @parameter expression="${reactorProjects}"
	 * @required
	 * @readonly
	 */
	protected List<MavenProject> m_reactorProjects;
	/**
	 * Component factory for Maven repositories.
	 * 
	 * @component
	 */
	protected ArtifactRepositoryFactory m_repoFactory;
	/**
	 * @component roleHint="default"
	 */
	protected ArtifactRepositoryLayout m_defaultLayout;
	/**
	 * Component for calculating mirror details.
	 * 
	 * @component
	 */
	protected WagonManager m_wagonManager;
	/**
	 * Runtime helper available on Maven 2.0.9 and above.
	 */
	protected Method m_getMirrorRepository;

	/**
	 * The Zip archiver.
	 * 
	 * @component role="org.codehaus.plexus.archiver.Archiver" roleHint="zip"
	 */
	protected ZipArchiver m_zipArchiver;

	/**
	 * Maven ProjectHelper.
	 * 
	 * @component
	 */
	protected MavenProjectHelper m_projectHelper;

	/**
	 * @component
	 */
	protected ArchiverManager m_archiverManager;

}
