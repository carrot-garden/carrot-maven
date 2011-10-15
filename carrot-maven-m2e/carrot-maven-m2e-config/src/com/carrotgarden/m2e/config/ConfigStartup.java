/*
 * Copyright (c) 2009
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
