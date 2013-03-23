/**
 * Copyright (C) 2010-2013 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.scr;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Clean component descriptors from {@link #outputDirectorySCR}.
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
		try {

			logInfo("clean");
			logInfo("incremental: " + isContextIncremental());

			if (!isProperPackaging()) {
				logInfo("skip for packaging=" + project.getPackaging());
				return;
			}

			final File folder = outputDirectorySCR();

			logDebug("");

			if (folder.exists()) {
				logDebug("folder delete : " + folder);
				FileUtils.deleteDirectory(folder);
			}

			if (!folder.exists()) {
				logDebug("folder create : " + folder);
				if (!folder.mkdirs()) {
					throw new IllegalStateException("folder create failure");
				}
			}

		} catch (final Throwable e) {
			throw new MojoFailureException("bada-boom", e);
		}
	}

}
