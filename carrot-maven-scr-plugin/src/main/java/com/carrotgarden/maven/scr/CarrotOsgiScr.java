/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.scr;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

import com.carrotgarden.osgi.anno.scr.make.Maker;

/**
 */
public abstract class CarrotOsgiScr extends AbstractMojo {

	/**
	 * @readonly
	 * @required
	 * @parameter expression="${project}"
	 */
	protected MavenProject project;

	/**
	 * map of key/value settings for eclipse m2e scr connector
	 * 
	 * @parameter
	 */
	protected Map<String, String> eclipseSettings = new HashMap<String, String>();

	/**
	 * location of generated scr component descriptor files in final bundle
	 * 
	 * @required
	 * @parameter default-value= "OSGI-INF/service-component"
	 */
	protected String targetDirectorySCR;

	/**
	 * default extension used for generated scr component descriptor files
	 * 
	 * @required
	 * @parameter default-value="xml"
	 */
	protected String outputExtensionSCR;

	/**
	 * location of compiled "main" class files
	 * 
	 * @required
	 * @parameter default-value="${project.build.outputDirectory}"
	 */
	protected File outputMainClasses;

	/**
	 * location of compiled "test" class files
	 * 
	 * @required
	 * @parameter default-value="${project.build.testOutputDirectory}"
	 */
	protected File outputTestClasses;

	/**
	 * collection of names of unwanted component service interfaces
	 * 
	 * @required
	 * @parameter default-value="java.lang.Cloneable"
	 */
	protected Set<String> excludedServices;

	/**
	 * should "main" classes be processed?
	 * 
	 * @required
	 * @parameter default-value="true"
	 */
	protected boolean isProcessMainClasses;

	/**
	 * should "test" classes be processed?
	 * 
	 * @required
	 * @parameter default-value="false"
	 */
	protected boolean isProcessTestClasses;

	/**
	 * should include an empty component descriptor?
	 * 
	 * @required
	 * @parameter default-value="true"
	 */
	protected boolean isIncludeEmptyDescriptor;

	/**
	 * should generated descriptor resource files be included in final bundle?
	 * 
	 * @required
	 * @parameter default-value="true"
	 */
	protected boolean isIncludeGeneratedDescritors;

	/**
	 * collection of names of known maven project packaging types for which to
	 * invoke this plug-in
	 * 
	 * @required
	 * @parameter default-value="bundle"
	 */
	protected Set<String> properPackaging;

	// ####################################################

	/**
	 * such as "./target/OSGI-INF/service-component"
	 */
	protected File outputDirectorySCR() {

		return new File(outputMainClasses, targetDirectorySCR);

	}

	protected boolean isProperPackaging() {

		final String packaging = project.getPackaging();

		if (properPackaging.contains(packaging)) {
			getLog().debug("proper packaging=" + packaging);
			return true;
		} else {
			getLog().debug("proper packaging=" + packaging);
			return false;
		}

	}

	private Maker maker;

	protected Maker getMaker() {
		if (maker == null) {
			maker = new Maker(excludedServices);
		}
		return maker;
	}

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

	//

	/** java class extension during class discovery */
	protected static final String[] EXTENSIONS = new String[] { "class" };

	/** find classes from all packages during class discovery */
	protected static final boolean IS_RECURSIVE = true;

	//

}
