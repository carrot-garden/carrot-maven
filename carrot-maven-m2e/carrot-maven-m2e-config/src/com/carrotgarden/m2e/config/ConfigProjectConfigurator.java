/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.m2e.config;

import java.io.File;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.AbstractJavaProjectConfigurator;

public class ConfigProjectConfigurator extends AbstractJavaProjectConfigurator {

	@Override
	public AbstractBuildParticipant getBuildParticipant(
			final IMavenProjectFacade projectFacade,
			final MojoExecution execution,
			final IPluginExecutionMetadata executionMetadata) {

		return new ConfigBuildParticipant(execution);

	}

	/** this configuration does not generate java source files */
	@Override
	protected File[] getSourceFolders(
			final ProjectConfigurationRequest request,
			final MojoExecution mojoExecution) throws CoreException {

		// final List<String> sourceRoots = request.getMavenProject()
		// .getCompileSourceRoots();

		return new File[] {};

	}

	@Override
	protected String getOutputFolderParameterName() {

		throw new UnsupportedOperationException("should not use");

	}

}
