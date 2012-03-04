/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.scr;

import java.io.File;

import org.apache.maven.plugin.MojoFailureException;

/**
 * clean component descriptors from outputDirectorySCR
 * 
 * @goal clean
 * 
 * @phase clean
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class CarrotOsgiScrClean extends CarrotOsgiScr {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoFailureException {

		if (isImproperPackaging()) {
			return;
		}

		//

		getLog().info("");

		getLog().info("delete " + outputDirectorySCR());

		final boolean isDelete = deleteDir(outputDirectorySCR());

		if (!isDelete) {
			getLog().warn("delete failed");
		}

		getLog().info("create " + outputDirectorySCR());

		final boolean isCreate = outputDirectorySCR().mkdirs();

		if (!isCreate) {
			getLog().warn("create failed");
		}

	}

	/**
	 * Deletes all files and subdirectories under dir. Returns true if all
	 * deletions were successful. If a deletion fails, the method stops
	 * attempting to delete and returns false.
	 */
	protected static boolean deleteDir(final File dir) {

		if (dir.isDirectory()) {

			final String[] children = dir.list();

			for (int i = 0; i < children.length; i++) {

				final boolean success = deleteDir(new File(dir, children[i]));

				if (!success) {
					return false;
				}

			}

		}

		return dir.delete();

	}

}
