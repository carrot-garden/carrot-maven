package com.carrotgarden.m2e.config;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.jdt.AbstractJavaProjectConfigurator;

public class ConfigProjectConfigurator extends AbstractJavaProjectConfigurator {

	@Override
	public AbstractBuildParticipant getBuildParticipant(
			final IMavenProjectFacade projectFacade,
			final MojoExecution execution,
			final IPluginExecutionMetadata executionMetadata) {

		return new ConfigBuildParticipant(execution);

	}

}
