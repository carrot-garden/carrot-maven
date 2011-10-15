package com.carrotgarden.m2e.config;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;

public class LaunchTabGroup extends AbstractLaunchConfigurationTabGroup {

	public void createTabs(final ILaunchConfigurationDialog dialog,
			final String mode) {

		final ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				//
				new LaunchMavenTab(), //
				//
				new JavaMainTab(), //
				new JavaArgumentsTab(), //
				new JavaJRETab(),//
				new JavaClasspathTab(), //
				new SourceLookupTab(), //
				new EnvironmentTab(), //
				new CommonTab() //
		};

		setTabs(tabs);

	}

}
