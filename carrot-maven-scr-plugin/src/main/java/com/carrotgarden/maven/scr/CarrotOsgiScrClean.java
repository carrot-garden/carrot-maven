/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.scr;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;

/**
 * Clean component descriptors from outputDirectorySCR
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

			if (!isProperPackaging()) {
				getLog().info("skip for packaging=" + project.getPackaging());
				return;
			}

			//

			final File folder = outputDirectorySCR();

			getLog().info("");

			if (folder.exists()) {
				getLog().info("folder delete : " + folder);
				FileUtils.deleteDirectory(folder);
			}

			if (!folder.exists()) {
				getLog().info("folder create : " + folder);
				if (!folder.mkdirs()) {
					throw new IllegalStateException("Folder create failure.");
				}
			}

		} catch (Throwable e) {
			throw new MojoFailureException("bada-boom", e);
		}

	}

}
