package com.carrotgarden.maven.osgi;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.util.FileUtils;
import org.ops4j.pax.construct.util.PomUtils;

import com.carrotgarden.maven.osgi.enums.ArtifactScope;
import com.carrotgarden.maven.osgi.enums.ProjectFunction;
import com.carrotgarden.osgi.feature.FeatureBundle;
import com.carrotgarden.osgi.feature.FeatureEntry;
import com.carrotgarden.osgi.feature.FeatureRepository;
import com.carrotgarden.utils.json.JSON;

/**
 * 
 * @goal make-archive
 * @aggregator false
 * 
 * @requiresProject true
 * @requiresDependencyResolution test
 * 
 */
public class MakeArchiveMojo extends BaseMojo {

	protected final static FeatureRepository REPO = new FeatureRepository();

	protected final static Collection<FeatureEntry> REPO_FEATURES = new LinkedList<FeatureEntry>();

	static {
		REPO.setFeatures(REPO_FEATURES);
	}

	//

	protected void LogScopeList() {

		getLog().info("### includeScopeList : " + includeScopeList);

		for (Object scope : includeScopeList) {
			getLog().info("### includeScopeList : " + scope.getClass());
		}

	}

	@Override
	public void execute() throws MojoExecutionException {
		try {

			// LogScopeList();

			switch (projectFunction) {
			case NONE:
				makeNone();
				return;
			case FEATURE:
				makeFeature();
				return;
			case REPOSITORY:
				makeRepository();
				return;
			default:
				throw new IllegalStateException("unknown projectFunction : "
						+ projectFunction);
			}

		} catch (Throwable e) {

			throw new MojoExecutionException("BADA-BOOM", e);

		}
	}

	protected void makeNone() {
		getLog().info("### skip project : " + m_project.getArtifactId());
	}

	@SuppressWarnings("unchecked")
	protected void makeFeature() {

		getLog().info("### feature project : " + m_project.getArtifactId());

		Set<Artifact> declaredArtifacts = new HashSet<Artifact>();

		if (includeTransitive) {
			declaredArtifacts.addAll(m_project.getArtifacts());
		} else {
			declaredArtifacts.addAll(m_project.getDependencies());
		}

		Set<Artifact> resolvedArtifacts = new HashSet<Artifact>();

		for (Artifact artifact : declaredArtifacts) {
			if (isIncluded(artifact) && hasProvisionedBundle(artifact)) {
				resolvedArtifacts.add(artifact);
			}
		}

		List<FeatureBundle> bundleList = new LinkedList<FeatureBundle>();

		for (Artifact artifact : resolvedArtifacts) {

			String location = getRepositoryBundleURL(artifact);
			int startLevel = featureStartLevel;

			FeatureBundle featureBundle = new FeatureBundle();
			featureBundle.setLocation(location);
			featureBundle.setStartLevel(startLevel);

			bundleList.add(featureBundle);

		}

		FeatureEntry feature = new FeatureEntry();
		feature.setName(featureName);
		feature.setVersion(featureVersion);
		feature.setBundles(bundleList);

		REPO_FEATURES.add(feature);

	}

	protected void makeRepository() throws Exception {

		getLog().info("### repository project : " + m_project.getArtifactId());

		Set<Artifact> bundles = new TreeSet<Artifact>(BUNDLES);

		for (Artifact artifact : bundles) {
			getLog().info("### bundle : " + artifact);
		}

		String index = JSON.intoText(REPO);
		getLog().info("### repository index file: \n" + index);

		//

		getLog().info("### repository build file: ");

		File archiveFile = new File(repositoryBuildFolder, repositoryBuildFile);
		Archiver archiver = m_archiverManager.getArchiver(repositoryArchiver);
		archiver.setDestFile(archiveFile);

		for (Artifact artifact : bundles) {
			File artifactFile = artifact.getFile();
			archiver.addFile(artifactFile, artifactFile.getName());
		}

		File indexFile = new File(repositoryBuildFolder, repositoryIndex);
		PrintWriter writer = new PrintWriter(indexFile);
		writer.write(index);
		writer.close();
		archiver.addFile(indexFile, indexFile.getName());

		archiver.createArchive();

	}

	protected boolean isIncluded(Artifact artifact) {
		if (artifact.isOptional() && !includeOptional) {
			return false;
		}
		if (!ArtifactScope.isIncluded(includeScopeList, artifact)) {
			return false;
		}
		return true;
	}

	protected String getRepositoryBundleURL(Artifact artifact) {
		return repositoryURL + "/" + artifact.getFile().getName();
	}

	protected boolean isBundle(Artifact artifact) {
		return PomUtils.isBundleArtifact(//
				artifact, m_resolver, m_remoteRepos, m_localRepo, true);
	}

	protected boolean isDownloaded(Artifact artifact) {
		return PomUtils.downloadFile(//
				artifact, m_resolver, m_remoteRepos, m_localRepo);
	}

	protected boolean hasProvisionedBundle(Artifact artifact) {

		if (!isDownloaded(artifact)) {
			getLog().error(//
					"can not download artifact : " + artifact);
			return false;
		}

		if (!isBundle(artifact)) {
			getLog().error(//
					"can not use non-bundle artifact : " + artifact);
			return false;
		}

		BUNDLES.add(artifact);

		return true;

	}

	//

	protected void copyFile(File source, File target)
			throws MojoExecutionException {
		try {

			getLog().info("COPY");

			FileUtils.copyFile(source, target);

		} catch (Exception e) {
			throw new MojoExecutionException("Error copying artifact from "
					+ source + " to " + target, e);
		}
	}

	//

	/**
	 * @parameter
	 * @required
	 */
	private String repositoryURL;

	/**
	 * @parameter
	 * @required
	 */
	private String repositoryIndex;

	/**
	 * @parameter
	 * @required
	 */

	private String repositoryBuildFolder;

	/**
	 * @parameter
	 * @required
	 */
	private String repositoryBuildFile;

	/**
	 * @parameter
	 * @required
	 */
	private String repositoryArchiver;

	/**
	 * @parameter
	 * @required
	 */
	private String featureName;

	/**
	 * @parameter
	 * @required
	 */
	private String featureVersion;

	/**
	 * @parameter
	 * @required
	 */
	private int featureStartLevel;

	//

	/**
	 * @parameter
	 * @required
	 */
	private ProjectFunction projectFunction;

	/**
	 * @parameter
	 * @required
	 */
	protected boolean includeOptional;

	/**
	 * @parameter
	 * @required
	 */
	protected boolean includeTransitive;

	/**
	 * @parameter
	 * @required
	 */
	protected List<String> includeScopeList;

}
