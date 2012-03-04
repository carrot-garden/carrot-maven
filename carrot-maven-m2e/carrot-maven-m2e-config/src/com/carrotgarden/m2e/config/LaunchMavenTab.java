/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.m2e.config;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class LaunchMavenTab extends JavaLaunchTab {

	private Text commandText;
	private Text projectText;

	public LaunchMavenTab() {
		setHelpContextId("carrot config maven tab");
	}

	@SuppressWarnings("restriction")
	@Override
	public Image getImage() {
		return JavaDebugImages.get(JavaDebugImages.IMG_OBJS_THREAD_GROUP);
	}

	public void createControl(final Composite parent) {

		final Composite comp = new Composite(parent, SWT.NONE);

		setControl(comp);

		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(getControl(), getHelpContextId());

		final Font font = parent.getFont();

		comp.setLayout(new GridLayout(1, true));

		comp.setFont(font);

		//

		final Group commandGroup = new Group(comp, SWT.NONE);
		commandGroup.setFont(font);
		commandGroup.setLayout(new GridLayout());
		commandGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		commandGroup.setText("Command");

		commandText = new Text(commandGroup, SWT.MULTI | SWT.WRAP | SWT.BORDER
				| SWT.V_SCROLL);

		final GridData commandGrid = new GridData(GridData.FILL_BOTH);
		commandGrid.heightHint = 40;
		commandGrid.widthHint = 100;
		commandText.setLayoutData(commandGrid);
		commandText.setFont(font);

		commandText.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent evt) {
				scheduleUpdateJob();
			}
		});

		//

		final Group projectGroup = new Group(comp, SWT.NONE);
		projectGroup.setFont(font);
		projectGroup.setLayout(new GridLayout());
		projectGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		projectGroup.setText("Projects");

		projectText = new Text(projectGroup, SWT.MULTI | SWT.WRAP | SWT.BORDER
				| SWT.V_SCROLL);

		final GridData projectGrid = new GridData(GridData.FILL_BOTH);
		projectGrid.heightHint = 40;
		projectGrid.widthHint = 100;
		projectText.setLayoutData(projectGrid);
		projectText.setFont(font);

		projectText.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent evt) {
				scheduleUpdateJob();
			}
		});

	}

	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {

		configuration.setAttribute(LaunchConst.ATTR_MAVEN_COMMAND,
				(String) null);

		configuration.setAttribute(LaunchConst.ATTR_MAVEN_PROJECTS,
				(String) null);

	}

	@Override
	public void initializeFrom(final ILaunchConfiguration configuration) {

		try {

			final String command = configuration.getAttribute(
					LaunchConst.ATTR_MAVEN_COMMAND, "");

			commandText.setText(command);

			//

			final String projects = configuration.getAttribute(
					LaunchConst.ATTR_MAVEN_PROJECTS, "");

			projectText.setText(projects);

		} catch (final CoreException e) {
			e.printStackTrace();
		}

	}

	public void performApply(final ILaunchConfigurationWorkingCopy configuration) {

		{
			final String text = commandText.getText();
			configuration.setAttribute(LaunchConst.ATTR_MAVEN_COMMAND, text);
		}

		{
			final String text = projectText.getText();
			configuration.setAttribute(LaunchConst.ATTR_MAVEN_PROJECTS, text);
		}

	}

	public String getName() {

		return "Maven";

	}

}
