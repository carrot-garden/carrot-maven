/**
 * Copyright (C) 2010-2013 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
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
 * Base for Maven goals.
 */
public abstract class CarrotOsgiScr extends AbstractMojo {

	/**
	 * Java class extension during class discovery.
	 */
	protected static final String[] EXTENSIONS = new String[] { "class" };

	/** Find classes from all packages during class discovery */
	protected static final boolean IS_RECURSIVE = true;

	/**
	 * Exclude java *.class files with this file name regex expression.
	 * 
	 * @required
	 * @parameter default-value= ".*-.*"
	 */
	protected String excludeFileNameRegex;

	/**
	 * Current maven pom.xml.
	 * 
	 * @readonly
	 * @required
	 * @parameter property="project"
	 */
	protected MavenProject project;

	/**
	 * Map of "key=value" settings for Eclipse m2e-scr connector plugin.
	 * 
	 * @parameter
	 */
	protected Map<String, String> eclipseSettings = new HashMap<String, String>();

	/**
	 * Location of generated DS component descriptor files in final bundle.
	 * 
	 * @required
	 * @parameter default-value= "OSGI-INF/service-component"
	 */
	protected String targetDirectorySCR;

	/**
	 * Default extension used for generated DS component descriptor files.
	 * 
	 * @required
	 * @parameter default-value="xml"
	 */
	protected String outputExtensionSCR;

	/**
	 * Location of compiled "main" class files.
	 * 
	 * @required
	 * @parameter default-value="${project.build.outputDirectory}"
	 */
	protected File outputMainClasses;

	/**
	 * Location of compiled "test" class files.
	 * 
	 * @required
	 * @parameter default-value="${project.build.testOutputDirectory}"
	 */
	protected File outputTestClasses;

	/**
	 * Collection of names of unwanted component service interfaces.
	 * 
	 * @required
	 * @parameter default-value="java.lang.Cloneable"
	 */
	protected Set<String> excludedServices;

	/**
	 * Should "main" classes be processed?
	 * 
	 * @required
	 * @parameter default-value="true"
	 */
	protected boolean isProcessMainClasses;

	/**
	 * Should "test" classes be processed?
	 * 
	 * @required
	 * @parameter default-value="false"
	 */
	protected boolean isProcessTestClasses;

	/**
	 * Should include an empty component descriptor?
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
	 * Collection of names of known maven project packaging types for which to
	 * invoke this plug-in.
	 * 
	 * @required
	 * @parameter default-value="bundle"
	 */
	protected Set<String> properPackaging;

	// ####################################################

	/**
	 * Build output directory, such as "./target/OSGI-INF/service-component".
	 */
	protected File outputDirectorySCR() {

		return new File(outputMainClasses, targetDirectorySCR);

	}

	/**
	 * Check if current project packaging is proper for goal execution.
	 */
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

	/**
	 * Maker of DS component descriptors.
	 */
	protected Maker maker() {
		if (maker == null) {
			maker = new Maker(excludedServices);
		}
		return maker;
	}

}
