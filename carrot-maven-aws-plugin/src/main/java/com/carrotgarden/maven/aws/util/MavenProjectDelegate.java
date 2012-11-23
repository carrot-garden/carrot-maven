/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.util;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.model.Build;
import org.apache.maven.model.CiManagement;
import org.apache.maven.model.Contributor;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Developer;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.model.License;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Prerequisites;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.Resource;
import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.plexus.util.xml.Xpp3Dom;

@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class MavenProjectDelegate extends MavenProject {

	private final MavenProject delegate;

	public MavenProjectDelegate(final MavenProject project) {
		assert project != null;

		this.delegate = project;
	}

	public MavenProject getDelegate() {
		return delegate;
	}

	@Override
	public String getModulePathAdjustment(final MavenProject project)
			throws IOException {
		return getDelegate().getModulePathAdjustment(project);
	}

	@Override
	public Artifact getArtifact() {
		return getDelegate().getArtifact();
	}

	@Override
	public void setArtifact(final Artifact artifact) {
		getDelegate().setArtifact(artifact);
	}

	@Override
	public Model getModel() {
		return getDelegate().getModel();
	}

	@Override
	public MavenProject getParent() {
		return getDelegate().getParent();
	}

	@Override
	public void setParent(final MavenProject project) {
		getDelegate().setParent(project);
	}

	@Override
	public void setRemoteArtifactRepositories(final List list) {
		getDelegate().setRemoteArtifactRepositories(list);
	}

	@Override
	public List getRemoteArtifactRepositories() {
		return getDelegate().getRemoteArtifactRepositories();
	}

	@Override
	public boolean hasParent() {
		return getDelegate().hasParent();
	}

	@Override
	public File getFile() {
		return getDelegate().getFile();
	}

	@Override
	public void setFile(final File file) {
		getDelegate().setFile(file);
	}

	@Override
	public File getBasedir() {
		return getDelegate().getBasedir();
	}

	@Override
	public void setDependencies(final List list) {
		getDelegate().setDependencies(list);
	}

	@Override
	public List getDependencies() {
		return getDelegate().getDependencies();
	}

	@Override
	public DependencyManagement getDependencyManagement() {
		return getDelegate().getDependencyManagement();
	}

	@Override
	public void addCompileSourceRoot(final String root) {
		getDelegate().addCompileSourceRoot(root);
	}

	@Override
	public void addScriptSourceRoot(final String root) {
		getDelegate().addScriptSourceRoot(root);
	}

	@Override
	public void addTestCompileSourceRoot(final String root) {
		getDelegate().addTestCompileSourceRoot(root);
	}

	@Override
	public List getCompileSourceRoots() {
		return getDelegate().getCompileSourceRoots();
	}

	@Override
	public List getScriptSourceRoots() {
		return getDelegate().getScriptSourceRoots();
	}

	@Override
	public List getTestCompileSourceRoots() {
		return getDelegate().getTestCompileSourceRoots();
	}

	@Override
	public List getCompileClasspathElements()
			throws DependencyResolutionRequiredException {
		return getDelegate().getCompileClasspathElements();
	}

	@Override
	public List getCompileArtifacts() {
		return getDelegate().getCompileArtifacts();
	}

	@Override
	public List getCompileDependencies() {
		return getDelegate().getCompileDependencies();
	}

	@Override
	public List getTestClasspathElements()
			throws DependencyResolutionRequiredException {
		return getDelegate().getTestClasspathElements();
	}

	@Override
	public List getTestArtifacts() {
		return getDelegate().getTestArtifacts();
	}

	@Override
	public List getTestDependencies() {
		return getDelegate().getTestDependencies();
	}

	@Override
	public List getRuntimeClasspathElements()
			throws DependencyResolutionRequiredException {
		return getDelegate().getRuntimeClasspathElements();
	}

	@Override
	public List getRuntimeArtifacts() {
		return getDelegate().getRuntimeArtifacts();
	}

	@Override
	public List getRuntimeDependencies() {
		return getDelegate().getRuntimeDependencies();
	}

	@Override
	public List getSystemClasspathElements()
			throws DependencyResolutionRequiredException {
		return getDelegate().getSystemClasspathElements();
	}

	@Override
	public List getSystemArtifacts() {
		return getDelegate().getSystemArtifacts();
	}

	@Override
	public List getSystemDependencies() {
		return getDelegate().getSystemDependencies();
	}

	@Override
	public void setModelVersion(final String version) {
		getDelegate().setModelVersion(version);
	}

	@Override
	public String getModelVersion() {
		return getDelegate().getModelVersion();
	}

	@Override
	public String getId() {
		return getDelegate().getId();
	}

	@Override
	public void setGroupId(final String id) {
		getDelegate().setGroupId(id);
	}

	@Override
	public String getGroupId() {
		return getDelegate().getGroupId();
	}

	@Override
	public void setArtifactId(final String id) {
		getDelegate().setArtifactId(id);
	}

	@Override
	public String getArtifactId() {
		return getDelegate().getArtifactId();
	}

	@Override
	public void setName(final String name) {
		getDelegate().setName(name);
	}

	@Override
	public String getName() {
		return getDelegate().getName();
	}

	@Override
	public void setVersion(final String version) {
		getDelegate().setVersion(version);
	}

	@Override
	public String getVersion() {
		return getDelegate().getVersion();
	}

	@Override
	public String getPackaging() {
		return getDelegate().getPackaging();
	}

	@Override
	public void setPackaging(final String s) {
		getDelegate().setPackaging(s);
	}

	@Override
	public void setInceptionYear(final String s) {
		getDelegate().setInceptionYear(s);
	}

	@Override
	public String getInceptionYear() {
		return getDelegate().getInceptionYear();
	}

	@Override
	public void setUrl(final String url) {
		getDelegate().setUrl(url);
	}

	@Override
	public String getUrl() {
		return getDelegate().getUrl();
	}

	@Override
	public Prerequisites getPrerequisites() {
		return getDelegate().getPrerequisites();
	}

	@Override
	public void setIssueManagement(final IssueManagement management) {
		getDelegate().setIssueManagement(management);
	}

	@Override
	public CiManagement getCiManagement() {
		return getDelegate().getCiManagement();
	}

	@Override
	public void setCiManagement(final CiManagement management) {
		getDelegate().setCiManagement(management);
	}

	@Override
	public IssueManagement getIssueManagement() {
		return getDelegate().getIssueManagement();
	}

	@Override
	public void setDistributionManagement(
			final DistributionManagement management) {
		getDelegate().setDistributionManagement(management);
	}

	@Override
	public DistributionManagement getDistributionManagement() {
		return getDelegate().getDistributionManagement();
	}

	@Override
	public void setDescription(final String s) {
		getDelegate().setDescription(s);
	}

	@Override
	public String getDescription() {
		return getDelegate().getDescription();
	}

	@Override
	public void setOrganization(final Organization organization) {
		getDelegate().setOrganization(organization);
	}

	@Override
	public Organization getOrganization() {
		return getDelegate().getOrganization();
	}

	@Override
	public void setScm(final Scm scm) {
		getDelegate().setScm(scm);
	}

	@Override
	public Scm getScm() {
		return getDelegate().getScm();
	}

	@Override
	public void setMailingLists(final List list) {
		getDelegate().setMailingLists(list);
	}

	@Override
	public List getMailingLists() {
		return getDelegate().getMailingLists();
	}

	@Override
	public void addMailingList(final MailingList mailingList) {
		getDelegate().addMailingList(mailingList);
	}

	@Override
	public void setDevelopers(final List list) {
		getDelegate().setDevelopers(list);
	}

	@Override
	public List getDevelopers() {
		return getDelegate().getDevelopers();
	}

	@Override
	public void addDeveloper(final Developer developer) {
		getDelegate().addDeveloper(developer);
	}

	@Override
	public void setContributors(final List list) {
		getDelegate().setContributors(list);
	}

	@Override
	public List getContributors() {
		return getDelegate().getContributors();
	}

	@Override
	public void addContributor(final Contributor contributor) {
		getDelegate().addContributor(contributor);
	}

	@Override
	public void setBuild(final Build build) {
		getDelegate().setBuild(build);
	}

	@Override
	public Build getBuild() {
		return getDelegate().getBuild();
	}

	@Override
	public List getResources() {
		return getDelegate().getResources();
	}

	@Override
	public List getTestResources() {
		return getDelegate().getTestResources();
	}

	@Override
	public void addResource(final Resource resource) {
		getDelegate().addResource(resource);
	}

	@Override
	public void addTestResource(final Resource resource) {
		getDelegate().addTestResource(resource);
	}

	@Override
	public void setReporting(final Reporting reporting) {
		getDelegate().setReporting(reporting);
	}

	@Override
	public Reporting getReporting() {
		return getDelegate().getReporting();
	}

	@Override
	public void setLicenses(final List list) {
		getDelegate().setLicenses(list);
	}

	@Override
	public List getLicenses() {
		return getDelegate().getLicenses();
	}

	@Override
	public void addLicense(final License license) {
		getDelegate().addLicense(license);
	}

	@Override
	public void setArtifacts(final Set set) {
		getDelegate().setArtifacts(set);
	}

	@Override
	public Set getArtifacts() {
		return getDelegate().getArtifacts();
	}

	@Override
	public Map getArtifactMap() {
		return getDelegate().getArtifactMap();
	}

	@Override
	public void setPluginArtifacts(final Set set) {
		getDelegate().setPluginArtifacts(set);
	}

	@Override
	public Set getPluginArtifacts() {
		return getDelegate().getPluginArtifacts();
	}

	@Override
	public Map getPluginArtifactMap() {
		return getDelegate().getPluginArtifactMap();
	}

	@Override
	public void setReportArtifacts(final Set set) {
		getDelegate().setReportArtifacts(set);
	}

	@Override
	public Set getReportArtifacts() {
		return getDelegate().getReportArtifacts();
	}

	@Override
	public Map getReportArtifactMap() {
		return getDelegate().getReportArtifactMap();
	}

	@Override
	public void setExtensionArtifacts(final Set set) {
		getDelegate().setExtensionArtifacts(set);
	}

	@Override
	public Set getExtensionArtifacts() {
		return getDelegate().getExtensionArtifacts();
	}

	@Override
	public Map getExtensionArtifactMap() {
		return getDelegate().getExtensionArtifactMap();
	}

	@Override
	public void setParentArtifact(final Artifact artifact) {
		getDelegate().setParentArtifact(artifact);
	}

	@Override
	public Artifact getParentArtifact() {
		return getDelegate().getParentArtifact();
	}

	@Override
	public List getRepositories() {
		return getDelegate().getRepositories();
	}

	@Override
	public List getReportPlugins() {
		return getDelegate().getReportPlugins();
	}

	@Override
	public List getBuildPlugins() {
		return getDelegate().getBuildPlugins();
	}

	@Override
	public List getModules() {
		return getDelegate().getModules();
	}

	@Override
	public PluginManagement getPluginManagement() {
		return getDelegate().getPluginManagement();
	}

	@Override
	public List getCollectedProjects() {
		return getDelegate().getCollectedProjects();
	}

	@Override
	public void setCollectedProjects(final List list) {
		getDelegate().setCollectedProjects(list);
	}

	@Override
	public void setPluginArtifactRepositories(final List list) {
		getDelegate().setPluginArtifactRepositories(list);
	}

	@Override
	public List getPluginArtifactRepositories() {
		return getDelegate().getPluginArtifactRepositories();
	}

	@Override
	public ArtifactRepository getDistributionManagementArtifactRepository() {
		return getDelegate().getDistributionManagementArtifactRepository();
	}

	@Override
	public List getPluginRepositories() {
		return getDelegate().getPluginRepositories();
	}

	@Override
	public void setActiveProfiles(final List list) {
		getDelegate().setActiveProfiles(list);
	}

	@Override
	public List getActiveProfiles() {
		return getDelegate().getActiveProfiles();
	}

	@Override
	public void addAttachedArtifact(final Artifact artifact) {
		getDelegate().addAttachedArtifact(artifact);
	}

	@Override
	public List getAttachedArtifacts() {
		return getDelegate().getAttachedArtifacts();
	}

	@Override
	public Xpp3Dom getGoalConfiguration(final String s, final String s1,
			final String s2, final String s3) {
		return getDelegate().getGoalConfiguration(s, s1, s2, s3);
	}

	@Override
	public Xpp3Dom getReportConfiguration(final String s, final String s1,
			final String s2) {
		return getDelegate().getReportConfiguration(s, s1, s2);
	}

	@Override
	public MavenProject getExecutionProject() {
		return getDelegate().getExecutionProject();
	}

	@Override
	public void setExecutionProject(final MavenProject project) {
		getDelegate().setExecutionProject(project);
	}

	@Override
	public void writeModel(final Writer writer) throws IOException {
		getDelegate().writeModel(writer);
	}

	@Override
	public void writeOriginalModel(final Writer writer) throws IOException {
		getDelegate().writeOriginalModel(writer);
	}

	@Override
	public Set getDependencyArtifacts() {
		return getDelegate().getDependencyArtifacts();
	}

	@Override
	public void setDependencyArtifacts(final Set set) {
		getDelegate().setDependencyArtifacts(set);
	}

	@Override
	public void setReleaseArtifactRepository(final ArtifactRepository repository) {
		getDelegate().setReleaseArtifactRepository(repository);
	}

	@Override
	public void setSnapshotArtifactRepository(
			final ArtifactRepository repository) {
		getDelegate().setSnapshotArtifactRepository(repository);
	}

	@Override
	public void setOriginalModel(final Model model) {
		getDelegate().setOriginalModel(model);
	}

	@Override
	public Model getOriginalModel() {
		return getDelegate().getOriginalModel();
	}

	@Override
	public List getBuildExtensions() {
		return getDelegate().getBuildExtensions();
	}

	@Override
	public Set createArtifacts(final ArtifactFactory factory, final String s,
			final ArtifactFilter filter)
			throws InvalidDependencyVersionException {
		return getDelegate().createArtifacts(factory, s, filter);
	}

	@Override
	public void addProjectReference(final MavenProject project) {
		getDelegate().addProjectReference(project);
	}

	/** @noinspection deprecation */
	@Override
	public void attachArtifact(final String s, final String s1, final File file) {
		getDelegate().attachArtifact(s, s1, file);
	}

	@Override
	public Properties getProperties() {
		return getDelegate().getProperties();
	}

	@Override
	public List getFilters() {
		return getDelegate().getFilters();
	}

	@Override
	public Map getProjectReferences() {
		return getDelegate().getProjectReferences();
	}

	@Override
	public boolean isExecutionRoot() {
		return getDelegate().isExecutionRoot();
	}

	@Override
	public void setExecutionRoot(final boolean b) {
		getDelegate().setExecutionRoot(b);
	}

	@Override
	public String getDefaultGoal() {
		return getDelegate().getDefaultGoal();
	}

	@Override
	public Artifact replaceWithActiveArtifact(final Artifact artifact) {
		return getDelegate().replaceWithActiveArtifact(artifact);
	}

}
