/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.m2e.config;

import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

@SuppressWarnings("restriction")
public class ConfigStartup implements IStartup {

	private ConfigLabel label;

	public ConfigStartup() {
	}

	public void earlyStartup() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				addStatusLineContribution();
			}
		});
	}

	private void addStatusLineContribution() {

		final IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
				.getWorkbenchWindows();

		final WorkbenchWindow window = (WorkbenchWindow) windows[0];

		final StatusLineManager statusLineManager = window
				.getStatusLineManager();

		label = new ConfigLabel("config");

		statusLineManager.add(new Separator("carrot"));

		statusLineManager.appendToGroup("carrot", label);

		statusLineManager.update(true);

	}

	void setStatus(final boolean isDone, final String status,
			final String tooltip) {

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {

				if (isDone) {
					label.setImage(ConfigPlugin
							.getImage("resources/icons/done.gif"));
				} else {
					label.setImage(ConfigPlugin
							.getImage("resources/icons/busy.gif"));
				}

				label.setText(status);
				label.setTooltip(tooltip);

			}
		});

	}

}
