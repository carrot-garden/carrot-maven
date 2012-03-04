/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.m2e.config;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;

public class DocIndexUpdater implements IResourceChangeListener {

	private TableViewer table; // assume this gets initialized somewhere

	private static final IPath DOC_PATH = new Path("MyProject/doc");

	public void resourceChanged(final IResourceChangeEvent event) {

		// we are only interested in POST_CHANGE events
		if (event.getType() != IResourceChangeEvent.POST_CHANGE)
			return;

		final IResourceDelta rootDelta = event.getDelta();

		// get the delta, if any, for the documentation directory
		final IResourceDelta docDelta = rootDelta.findMember(DOC_PATH);

		if (docDelta == null)
			return;

		final ArrayList changed = new ArrayList();

		final IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {

			public boolean visit(final IResourceDelta delta) {

				// only interested in changed resources (not added or removed)
				if (delta.getKind() != IResourceDelta.CHANGED)
					return true;

				// only interested in content changes
				if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
					return true;

				final IResource resource = delta.getResource();

				// only interested in files with the "txt" extension
				if (resource.getType() == IResource.FILE
						&& "txt".equalsIgnoreCase(resource.getFileExtension())) {
					changed.add(resource);
				}

				return true;

			}
		};
		try {
			docDelta.accept(visitor);
		} catch (final CoreException e) {
			// open error dialog with syncExec or print to plugin log file
		}
		// nothing more to do if there were no changed text files
		if (changed.size() == 0)
			return;
		// post this update to the table
		final Display display = table.getControl().getDisplay();
		if (!display.isDisposed()) {
			display.asyncExec(new Runnable() {
				public void run() {
					// make sure the table still exists
					if (table.getControl().isDisposed())
						return;
					table.update(changed.toArray(), null);
				}
			});
		}
	}
}