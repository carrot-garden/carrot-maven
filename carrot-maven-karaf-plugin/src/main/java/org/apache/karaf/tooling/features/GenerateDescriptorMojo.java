/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package org.apache.karaf.tooling.features;

import static org.apache.karaf.deployer.kar.KarArtifactInstaller.*;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.karaf.features.internal.model.Bundle;
import org.apache.karaf.features.internal.model.Dependency;
import org.apache.karaf.features.internal.model.Feature;
import org.apache.karaf.features.internal.model.Features;
import org.apache.karaf.features.internal.model.JaxbUtil;
import org.apache.karaf.features.internal.model.ObjectFactory;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResult;
import org.xml.sax.SAXException;

/**
 * Generates the features XML file NB this requires a recent
 * maven-install-plugin such as 2.3.1
 * 
 * @version $Revision: 1243427 $
 * @goal features-generate-descriptor
 * @phase compile
 * @requiresDependencyResolution runtime
 * @inheritByDefault true
 * @description Generates the features XML file starting with an optional source
 *              feature.xml and adding project dependencies as bundles and
 *              feature/car dependencies
 */
@SuppressWarnings("unchecked")
public class GenerateDescriptorMojo extends AbstractLogEnabled implements Mojo {

	/**
	 * The (optional) input feature.file to extend
	 * 
	 * @parameter 
	 *            default-value="${project.basedir}/src/main/feature/feature.xml"
	 */
	private File inputFile;

	/**
	 * (wrapper) The filtered input file
	 * 
	 * @parameter default-value=
	 *            "${project.build.directory}/feature/filteredInputFeature.xml"
	 */
	private File filteredInputFile;

	/**
	 * (wrapper) The file to generate
	 * 
	 * @parameter default-value="${project.build.directory}/feature/feature.xml"
	 */
	private File outputFile;

	/**
	 * The resolver to use for the feature. Normally null or "OBR" or "(OBR)"
	 * 
	 * @parameter default-value="${resolver}"
	 */
	private String resolver;

	/**
	 * (wrapper) The artifact type for attaching the generated file to the
	 * project
	 * 
	 * @parameter default-value="xml"
	 */
	private final String attachmentArtifactType = "xml";

	/**
	 * (wrapper) The artifact classifier for attaching the generated file to the
	 * project
	 * 
	 * @parameter default-value="features"
	 */
	private final String attachmentArtifactClassifier = "features";

	/**
	 * If false, feature dependencies are added to the assembled feature as
	 * dependencies. If true, feature dependencies xml descriptors are read and
	 * their contents added to the features descriptor under assembly.
	 * 
	 * @parameter default-value="${aggregateFeatures}"
	 */
	private final boolean aggregateFeatures = false;

	/**
	 * If present, the bundles added to the feature constructed from the
	 * dependencies will be marked with this startlevel.
	 * 
	 * @parameter
	 */
	private Integer startLevel;

	// new

	/**
	 * (wrapper) The maven project.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * The maven project's helper.
	 * 
	 * @component
	 * @required
	 * @readonly
	 */
	protected MavenProjectHelper projectHelper;

	/**
	 * The entry point to Aether, i.e. the component doing all the work.
	 * 
	 * @component
	 * @required
	 * @readonly
	 */
	private RepositorySystem repoSystem;

	/**
	 * The current repository/network configuration of Maven.
	 * 
	 * @parameter default-value="${repositorySystemSession}"
	 * @required
	 * @readonly
	 */
	private RepositorySystemSession repoSession;

	/**
	 * Flag indicating whether transitive dependencies should be included (
	 * <code>true</code>) or not (<code>false</code>).
	 * 
	 * @parameter default-value="true"
	 */
	private boolean includeTransitiveDependency;

	/**
	 * The project's remote repositories to use for the resolution of project
	 * dependencies.
	 * 
	 * @parameter default-value="${project.remoteProjectRepositories}"
	 * @readonly
	 */
	private List<RemoteRepository> projectRepos;

	/**
	 * The project's remote repositories to use for the resolution of plugins
	 * and their dependencies.
	 * 
	 * @parameter default-value="${project.remotePluginRepositories}"
	 * @required
	 * @readonly
	 */
	private List<RemoteRepository> pluginRepos;

	// dependencies we are interested in
	protected Map<Artifact, String> localDependencies;
	// log of what happened during search
	protected String treeListing;

	// maven log
	private Log log;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			final DependencyHelper dependencyHelper = new DependencyHelper(
					pluginRepos, projectRepos, repoSession, repoSystem);

			dependencyHelper.getDependencies(project,
					includeTransitiveDependency);

			this.localDependencies = dependencyHelper.getLocalDependencies();

			this.treeListing = dependencyHelper.getTreeListing();

			// XXX
			getLog().info(saveTreeListing());

			final File dir = outputFile.getParentFile();

			if (dir.isDirectory() || dir.mkdirs()) {

				final PrintStream out = new PrintStream(new FileOutputStream(
						outputFile));
				try {
					writeFeatures(out);
				} finally {
					out.close();
				}

				// now lets attach it
				projectHelper.attachArtifact(project, attachmentArtifactType,
						attachmentArtifactClassifier, outputFile);

			} else {
				throw new MojoExecutionException(
						"Could not create directory for features file: " + dir);
			}
		} catch (final Exception e) {
			getLogger().error(e.getMessage());
			throw new MojoExecutionException(
					"Unable to create features.xml file: " + e, e);
		}
	}

	/*
	 * Write all project dependencies as feature
	 */
	private void writeFeatures(final PrintStream out)
			throws ArtifactResolutionException, ArtifactNotFoundException,
			IOException, JAXBException, SAXException,
			ParserConfigurationException, XMLStreamException,
			MojoExecutionException {
		getLogger().info(
				"Generating feature descriptor file "
						+ outputFile.getAbsolutePath());

		// read in an existing feature.xml

		final ObjectFactory objectFactory = new ObjectFactory();

		Features features;

		getLog().info("### inputFile=" + inputFile);
		getLog().info("### inputFile.exists()=" + inputFile.exists());

		if (inputFile.exists()) {
			filter(inputFile, filteredInputFile);
			features = readFeaturesFile(filteredInputFile);
		} else {
			features = objectFactory.createFeaturesRoot();
		}

		if (features.getName() == null) {
			features.setName(project.getArtifactId());
		}

		Feature feature = null;
		for (final Feature test : features.getFeature()) {
			if (test.getName().equals(project.getArtifactId())) {
				feature = test;
			}
		}
		if (feature == null) {
			feature = objectFactory.createFeature();
			feature.setName(project.getArtifactId());
		}
		if (feature.getVersion() == null) {
			feature.setVersion(project.getArtifact().getBaseVersion());
		}
		if (feature.getDescription() == null) {
			feature.setDescription(project.getName());
		}
		if (resolver != null) {
			feature.setResolver(resolver);
		}
		if (project.getDescription() != null && feature.getDetails() == null) {
			feature.setDetails(project.getDescription());
		}
		for (final Map.Entry<Artifact, String> entry : localDependencies
				.entrySet()) {
			final Artifact artifact = entry.getKey();
			if (DependencyHelper.isFeature(artifact)) {
				if (aggregateFeatures
						&& FEATURE_CLASSIFIER.equals(artifact.getClassifier())) {
					final File featuresFile = resolve(artifact);
					if (featuresFile == null || !featuresFile.exists()) {
						throw new MojoExecutionException(
								"Cannot locate file for feature: " + artifact
										+ " at " + featuresFile);
					}
					final Features includedFeatures = readFeaturesFile(featuresFile);
					// TODO check for duplicates?
					features.getFeature().addAll(includedFeatures.getFeature());
				} else {
					final Dependency dependency = objectFactory
							.createDependency();
					dependency.setName(artifact.getArtifactId());
					// TODO convert to bundles version?
					dependency.setVersion(artifact.getVersion());
					feature.getFeature().add(dependency);
				}
			} else {
				String bundleName = MavenUtil.artifactToMvn(artifact);
				final File bundleFile = resolve(artifact);
				final Manifest manifest = getManifest(bundleFile);

				if (manifest == null
						|| !ManifestUtils.isBundle(getManifest(bundleFile))) {
					bundleName = "wrap:" + bundleName;
				}

				Bundle bundle = null;
				for (final Bundle b : feature.getBundle()) {
					if (bundleName.equals(b.getLocation())) {
						bundle = b;
						break;
					}
				}
				if (bundle == null) {
					bundle = objectFactory.createBundle();
					bundle.setLocation(bundleName);
					feature.getBundle().add(bundle);
				}
				if ("runtime".equals(entry.getValue())) {
					bundle.setDependency(true);
				}
				if (startLevel != null && bundle.getStartLevel() == 0) {
					bundle.setStartLevel(startLevel);
				}

			}
		}

		if ((!feature.getBundle().isEmpty() || !feature.getFeature().isEmpty())
				&& !features.getFeature().contains(feature)) {
			features.getFeature().add(feature);
		}

		JaxbUtil.marshal(features, out);
		try {
			checkChanges(features, objectFactory);
		} catch (final Exception e) {
			throw new MojoExecutionException("Features contents have changed",
					e);
		}
		getLogger().info("...done!");
	}

	/**
	 * Extract the MANIFEST from the give file.
	 */
	private Manifest getManifest(final File file) throws IOException {
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(file));
		} catch (final Exception e) {
			getLogger().warn("Error while opening artifact", e);
		}

		try {
			is.mark(256 * 1024);
			final JarInputStream jar = new JarInputStream(is);
			final Manifest m = jar.getManifest();
			if (m == null) {
				getLogger().warn(
						"Manifest not present in the first entry of the zip - "
								+ file.getName());
			}
			return m;
		} finally {
			if (is != null) { // just in case when we did not open bundle
				is.close();
			}
		}
	}

	private File resolve(final Artifact artifact) {
		final ArtifactRequest request = new ArtifactRequest();
		request.setArtifact(artifact);
		request.setRepositories(projectRepos);

		getLog().debug(
				"Resolving artifact " + artifact + " from " + projectRepos);

		ArtifactResult result;
		try {
			result = repoSystem.resolveArtifact(repoSession, request);
		} catch (final org.sonatype.aether.resolution.ArtifactResolutionException e) {
			getLog().warn("could not resolve " + artifact, e);
			return null;
		}

		getLog().debug(
				"Resolved artifact " + artifact + " to "
						+ result.getArtifact().getFile() + " from "
						+ result.getRepository());
		return result.getArtifact().getFile();
	}

	private Features readFeaturesFile(final File featuresFile)
			throws XMLStreamException, JAXBException, IOException {
		Features features;
		final InputStream in = new FileInputStream(featuresFile);
		try {
			features = JaxbUtil.unmarshal(in, false);
		} finally {
			in.close();
		}
		return features;
	}

	@Override
	public void setLog(final Log log) {
		this.log = log;
	}

	@Override
	public Log getLog() {
		if (log == null) {
			setLog(new SystemStreamLog());
		}
		return log;
	}

	// ------------------------------------------------------------------------//
	// dependency change detection

	/**
	 * Whether to look for changed dependencies at all
	 * 
	 * @parameter
	 */
	private boolean checkDependencyChange;

	/**
	 * Whether to fail on changed dependencies (default, false) or warn (true)
	 * 
	 * @parameter
	 */
	private boolean warnOnDependencyChange;

	/**
	 * Whether to show changed dependencies in log
	 * 
	 * @parameter
	 */
	private boolean logDependencyChanges;

	/**
	 * Whether to overwrite src/main/history/dependencies.xml if it has changed
	 * 
	 * @parameter
	 */
	private boolean overwriteChangedDependencies;

	/**
	 * (wrapper) Location of existing dependency file.
	 * 
	 * @parameter expression="${basedir}/src/main/history/dependencies.xml"
	 * @required
	 */
	private File dependencyFile;

	/**
	 * Location of filtered dependency file.
	 * 
	 * @parameter expression="${basedir}/target/history/dependencies.xml"
	 * @required
	 * @readonly
	 */
	private File filteredDependencyFile;

	// filtering support
	/**
	 * The character encoding scheme to be applied when filtering resources.
	 * 
	 * @parameter expression="${encoding}"
	 *            default-value="${project.build.sourceEncoding}"
	 */
	protected String encoding;

	/**
	 * @component 
	 *            role="org.apache.maven.shared.filtering.MavenResourcesFiltering"
	 *            role-hint="default"
	 * @required
	 * @readonly
	 */
	protected MavenResourcesFiltering mavenResourcesFiltering;

	/**
	 * @parameter expression="${session}"
	 * @required
	 * @readonly
	 */
	protected MavenSession session;

	/**
	 * Expression preceded with the String won't be interpolated \${foo} will be
	 * replaced with ${foo}
	 * 
	 * @parameter expression="${maven.resources.escapeString}"
	 */
	protected String escapeString = "\\";

	/**
	 * @plexus.requirement role-hint="default"
	 * @component
	 * @required
	 * @readonly
	 */
	protected MavenFileFilter mavenFileFilter;

	/**
	 * System properties.
	 * 
	 * @parameter
	 */
	protected Map<String, String> systemProperties;

	private Map<String, String> previousSystemProperties;

	private void checkChanges(final Features newFeatures,
			final ObjectFactory objectFactory) throws Exception, IOException,
			JAXBException, XMLStreamException {

		if (checkDependencyChange) {
			// combine all the dependencies to one feature and strip out
			// versions
			final Features features = objectFactory.createFeaturesRoot();
			features.setName(newFeatures.getName());
			final Feature feature = objectFactory.createFeature();
			features.getFeature().add(feature);
			for (final Feature f : newFeatures.getFeature()) {
				for (final Bundle b : f.getBundle()) {
					final Bundle bundle = objectFactory.createBundle();
					bundle.setLocation(b.getLocation());
					feature.getBundle().add(bundle);
				}
				for (final Dependency d : f.getFeature()) {
					final Dependency dependency = objectFactory
							.createDependency();
					dependency.setName(d.getName());
					feature.getFeature().add(dependency);
				}
			}

			Collections.sort(feature.getBundle(), new Comparator<Bundle>() {

				@Override
				public int compare(final Bundle bundle, final Bundle bundle1) {
					return bundle.getLocation()
							.compareTo(bundle1.getLocation());
				}
			});
			Collections.sort(feature.getFeature(),
					new Comparator<Dependency>() {
						@Override
						public int compare(final Dependency dependency,
								final Dependency dependency1) {
							return dependency.getName().compareTo(
									dependency1.getName());
						}
					});

			if (dependencyFile.exists()) {
				// filter dependencies file
				filter(dependencyFile, filteredDependencyFile);
				// read dependency types, convert to dependencies, compare.
				final Features oldfeatures = readFeaturesFile(filteredDependencyFile);
				final Feature oldFeature = oldfeatures.getFeature().get(0);

				final List<Bundle> addedBundles = new ArrayList<Bundle>(
						feature.getBundle());
				final List<Bundle> removedBundles = new ArrayList<Bundle>();
				for (final Bundle test : oldFeature.getBundle()) {
					final boolean t1 = addedBundles.contains(test);
					final int s1 = addedBundles.size();
					final boolean t2 = addedBundles.remove(test);
					final int s2 = addedBundles.size();
					if (t1 != t2) {
						getLogger().warn(
								"dependencies.contains: " + t1
										+ ", dependencies.remove(test): " + t2);
					}
					if (t1 == (s1 == s2)) {
						getLogger().warn(
								"dependencies.contains: " + t1
										+ ", size before: " + s1
										+ ", size after: " + s2);
					}
					if (!t2) {
						removedBundles.add(test);
					}
				}

				final List<Dependency> addedDependencys = new ArrayList<Dependency>(
						feature.getFeature());
				final List<Dependency> removedDependencys = new ArrayList<Dependency>();
				for (final Dependency test : oldFeature.getFeature()) {
					final boolean t1 = addedDependencys.contains(test);
					final int s1 = addedDependencys.size();
					final boolean t2 = addedDependencys.remove(test);
					final int s2 = addedDependencys.size();
					if (t1 != t2) {
						getLogger().warn(
								"dependencies.contains: " + t1
										+ ", dependencies.remove(test): " + t2);
					}
					if (t1 == (s1 == s2)) {
						getLogger().warn(
								"dependencies.contains: " + t1
										+ ", size before: " + s1
										+ ", size after: " + s2);
					}
					if (!t2) {
						removedDependencys.add(test);
					}
				}
				if (!addedBundles.isEmpty() || !removedBundles.isEmpty()
						|| !addedDependencys.isEmpty()
						|| !removedDependencys.isEmpty()) {
					saveDependencyChanges(addedBundles, removedBundles,
							addedDependencys, removedDependencys, objectFactory);
					if (overwriteChangedDependencies) {
						writeDependencies(features, dependencyFile);
					}
				} else {
					getLog().info(saveTreeListing());
				}

			} else {
				writeDependencies(features, dependencyFile);
			}

		}
	}

	protected void saveDependencyChanges(final Collection<Bundle> addedBundles,
			final Collection<Bundle> removedBundles,
			final Collection<Dependency> addedDependencys,
			final Collection<Dependency> removedDependencys,
			final ObjectFactory objectFactory) throws Exception {
		final File addedFile = new File(filteredDependencyFile.getParentFile(),
				"dependencies.added.xml");
		final Features added = toFeatures(addedBundles, addedDependencys,
				objectFactory);
		writeDependencies(added, addedFile);

		final File removedFile = new File(
				filteredDependencyFile.getParentFile(),
				"dependencies.removed.xml");
		final Features removed = toFeatures(removedBundles, removedDependencys,
				objectFactory);
		writeDependencies(removed, removedFile);

		final StringWriter out = new StringWriter();
		out.write(saveTreeListing());

		out.write("Dependencies have changed:\n");
		if (!addedBundles.isEmpty() || !addedDependencys.isEmpty()) {
			out.write("\tAdded dependencies are saved here: "
					+ addedFile.getAbsolutePath() + "\n");
			if (logDependencyChanges) {
				JaxbUtil.marshal(added, out);
			}
		}
		if (!removedBundles.isEmpty() || !removedDependencys.isEmpty()) {
			out.write("\tRemoved dependencies are saved here: "
					+ removedFile.getAbsolutePath() + "\n");
			if (logDependencyChanges) {
				JaxbUtil.marshal(removed, out);
			}
		}
		out.write("Delete " + dependencyFile.getAbsolutePath()
				+ " if you are happy with the dependency changes.");

		if (warnOnDependencyChange) {
			getLog().warn(out.toString());
		} else {
			throw new MojoFailureException(out.toString());
		}
	}

	private Features toFeatures(final Collection<Bundle> addedBundles,
			final Collection<Dependency> addedDependencys,
			final ObjectFactory objectFactory) {
		final Features features = objectFactory.createFeaturesRoot();
		final Feature feature = objectFactory.createFeature();
		feature.getBundle().addAll(addedBundles);
		feature.getFeature().addAll(addedDependencys);
		features.getFeature().add(feature);
		return features;
	}

	private void writeDependencies(final Features features, final File file)
			throws JAXBException, IOException {
		file.getParentFile().mkdirs();
		if (!file.getParentFile().exists()
				|| !file.getParentFile().isDirectory()) {
			throw new IOException("Cannot create directory at "
					+ file.getParent());
		}
		final FileOutputStream out = new FileOutputStream(file);
		try {
			JaxbUtil.marshal(features, out);
		} finally {
			out.close();
		}
	}

	protected void filter(final File sourceFile, final File targetFile)
			throws MojoExecutionException {
		try {

			if (StringUtils.isEmpty(encoding)) {
				getLog().warn(
						"File encoding has not been set, using platform encoding "
								+ ReaderFactory.FILE_ENCODING
								+ ", i.e. build is platform dependent!");
			}
			targetFile.getParentFile().mkdirs();
			final List filters = mavenFileFilter.getDefaultFilterWrappers(
					project, null, true, session, null);
			mavenFileFilter.copyFile(sourceFile, targetFile, true, filters,
					encoding, true);
		} catch (final MavenFilteringException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	protected String saveTreeListing() throws IOException {

		final File folder = filteredDependencyFile.getParentFile();

		folder.mkdirs();

		final File treeListFile = new File(folder, "treeListing.txt");

		final OutputStream os = new FileOutputStream(treeListFile);

		final BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(os));

		try {
			writer.write(treeListing);
		} finally {
			writer.close();
		}

		return "\tTree listing is saved here: "
				+ treeListFile.getAbsolutePath() + "\n";

	}

}
