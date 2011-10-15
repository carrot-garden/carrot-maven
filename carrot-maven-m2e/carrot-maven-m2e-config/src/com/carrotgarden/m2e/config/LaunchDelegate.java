package com.carrotgarden.m2e.config;

import java.io.File;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;

/**
 * http://www.eclipse.org/articles/Article-Launch-Framework/launch.html
 * 
 * */
public class LaunchDelegate extends JavaLaunchDelegate {

	@Override
	public void launch(final ILaunchConfiguration configuration,
			final String mode, final ILaunch launch,
			final IProgressMonitor monitor) throws CoreException {

		final int workMaven = projectArray(configuration).length;
		final int workJava = 3;
		final int workTotal = workMaven + workJava;

		monitor.beginTask(
				MessageFormat.format("{0}...",
						new Object[] { configuration.getName() }), workTotal);

		if (monitor.isCanceled()) {
			return;
		}

		try {

			launchMaven(configuration, mode, launch, monitor);

			launchJava(configuration, mode, launch, monitor);

		} finally {

			monitor.done();

		}

	}

	@SuppressWarnings("restriction")
	private void launchJava(final ILaunchConfiguration configuration,
			final String mode, final ILaunch launch,
			final IProgressMonitor monitor) throws CoreException {

		monitor.subTask(LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_Verifying_launch_attributes____1);

		final String mainTypeName = verifyMainTypeName(configuration);
		final IVMRunner runner = getVMRunner(configuration, mode);

		final File workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = null;
		if (workingDir != null) {
			workingDirName = workingDir.getAbsolutePath();
		}

		// Environment variables
		final String[] envp = getEnvironment(configuration);

		// Program & VM arguments
		final String pgmArgs = getProgramArguments(configuration);
		final String vmArgs = getVMArguments(configuration);
		final ExecutionArguments execArgs = new ExecutionArguments(vmArgs,
				pgmArgs);

		// VM-specific attributes
		final Map vmAttributesMap = getVMSpecificAttributesMap(configuration);

		// Classpath
		final String[] classpath = getClasspath(configuration);

		// Create VM config
		final VMRunnerConfiguration runConfig = new VMRunnerConfiguration(
				mainTypeName, classpath);
		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setEnvironment(envp);
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDirName);
		runConfig.setVMSpecificAttributesMap(vmAttributesMap);

		// Bootpath
		runConfig.setBootClassPath(getBootpath(configuration));

		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}

		// stop in main
		prepareStopInMain(configuration);

		// done the verification phase
		monitor.worked(1);

		monitor.subTask(LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_Creating_source_locator____2);
		// set the default source locator if required
		setDefaultSourceLocator(launch, configuration);
		monitor.worked(1);

		// Launch the configuration - 1 unit of work
		runner.run(runConfig, launch, monitor);

	}

	private String[] commandArray(final ILaunchConfiguration configuration)
			throws CoreException {

		final String command = configuration.getAttribute(
				LaunchConst.ATTR_MAVEN_COMMAND, "");

		final String[] commandArray = command.split("\\W+");

		return commandArray;

	}

	private String[] projectArray(final ILaunchConfiguration configuration)
			throws CoreException {

		final String projects = configuration.getAttribute(
				LaunchConst.ATTR_MAVEN_PROJECTS, "");

		final String[] projectArray = projects.split("\\W+");

		return projectArray;

	}

	private void launchMaven(final ILaunchConfiguration configuration,
			final String mode, final ILaunch launch,
			final IProgressMonitor monitor) throws CoreException {

		monitor.subTask("Maven Goals");

		ConfigPlugin.log(IStatus.INFO, "HELLO");

		//

		final String[] commandArray = commandArray(configuration);
		final String[] projectArray = projectArray(configuration);

		for (final String project : projectArray) {

			launchMaven(project, commandArray, monitor);

			monitor.worked(1);

		}

	}

	private void launchMaven(final String project, final String[] commandArray,
			final IProgressMonitor monitor) throws CoreException {

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();

		final IWorkspaceRoot root = workspace.getRoot();

		IProject eclipseProject = null;

		for (final IProject projectFound : root.getProjects()) {

			final String nameFound = projectFound.getName();

			if (nameFound.equals(project)) {
				eclipseProject = projectFound;
				break;
			}
		}

		if (eclipseProject == null) {
			ConfigPlugin.log(IStatus.WARNING, "not found : " + project);
			return;
		}

		final File projectFolder = eclipseProject.getLocation().toFile();

		final File pomFile = new File(projectFolder, "pom.xml");

		if (!pomFile.exists()) {
			ConfigPlugin.log(IStatus.WARNING, "not found : " + pomFile);
			return;
		}

		//

		launchMaven(pomFile, commandArray, monitor);

	}

	private void launchMaven(final File pomFile, final String[] commandArray,
			final IProgressMonitor monitor) throws CoreException {

		final IMaven maven = MavenPlugin.getMaven();

		final Model mavenModel = maven.readModel(pomFile);

		final MavenProject mavenProject = new MavenProject(mavenModel);

		final Properties mavenProps = mavenProject.getProperties();

		for (final Map.Entry<?, ?> entry : mavenProps.entrySet()) {
			final String key = entry.getKey().toString();
			final String value = entry.getValue().toString();
			// ConfigPlugin.logInfo("key= " + key + " value=" + value);
		}

		//

		final List<String> goalList = new LinkedList<String>();

		for (final String goal : commandArray) {
			goalList.add(goal);
		}

		final MavenExecutionRequest request = maven
				.createExecutionRequest(monitor);

		request.setPom(pomFile);

		request.setGoals(goalList);

		//

		ConfigPlugin.log(IStatus.INFO,
				"maven execute : " + mavenProject.getId());

		maven.execute(request, monitor);

	}

}
