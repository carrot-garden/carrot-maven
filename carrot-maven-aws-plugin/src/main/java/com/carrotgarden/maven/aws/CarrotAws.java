package com.carrotgarden.maven.aws;

/**
 */

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.impl.MavenLoggerFactory;

/**
 */
public abstract class CarrotAws extends AbstractMojo {

	/** do not use during class init */
	protected Logger getLogger(final Class<?> klaz) {
		return MavenLoggerFactory.getLogger(klaz, getLog());
	}

	/**
	 * @readonly
	 * @required
	 * @parameter expression="${project}"
	 */
	protected MavenProject project;

	/**
	 * @readonly
	 * @required
	 * @parameter expression="${settings}"
	 */
	protected Settings settings;

	// ############################################
	// ############################################
	// ############################################

	protected static boolean isValidDirectory(final File file) {

		if (file == null) {
			return false;
		}

		if (!file.exists()) {
			return false;
		}

		if (!file.isDirectory()) {
			return false;
		}

		if (!file.canRead()) {
			return false;
		}

		if (!file.canWrite()) {
			return false;
		}

		return true;

	}

}
