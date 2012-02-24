package com.carrotgarden.maven.scr;

/**
 */

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

import com.carrotgarden.osgi.anno.scr.make.Maker;

/**
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 */
public abstract class CarrotOsgiScr extends AbstractMojo {

	/**
	 * @readonly
	 * @parameter expression="${project}"
	 */
	protected MavenProject project;

	/**
	 * map of key/value settings for eclipse m2e connector
	 * 
	 * @parameter
	 */
	protected Map<String, String> eclipseSettings = new HashMap<String, String>();

	/**
	 * location of generated scr component descriptor files in final bundle
	 * 
	 * @parameter default-value= "OSGI-INF/service-component"
	 */
	protected String targetDirectorySCR;

	/**
	 * "${project.build.outputDirectory}/OSGI-INF/service-component"
	 */
	protected File outputDirectorySCR() {

		return new File(outputMainClasses, targetDirectorySCR);

	}

	/**
	 * default extension used for generated scr component descriptor files
	 * 
	 * @parameter default-value="xml"
	 */
	protected String outputExtensionSCR;

	/**
	 * location of compiled "main" class files
	 * 
	 * @parameter default-value="${project.build.outputDirectory}"
	 */
	protected File outputMainClasses;

	/**
	 * location of compiled "test" class files
	 * 
	 * @parameter default-value="${project.build.testOutputDirectory}"
	 */
	protected File outputTestClasses;

	/**
	 * collection of names of unwanted component service interfaces
	 * 
	 * @parameter
	 */
	protected Set<String> excludedServices = new HashSet<String>();

	/**
	 * should "main" classes be processed?
	 * 
	 * @parameter default-value="true"
	 */
	protected boolean isProcessMainClasses;

	/**
	 * should "test" classes be processed?
	 * 
	 * @parameter default-value="false"
	 */
	protected boolean isProcessTestClasses;

	/**
	 * should include an empty component descriptor?
	 * 
	 * @parameter default-value="true"
	 */
	protected boolean isIncludeEmptyDescriptor;

	/**
	 * should generated descriptor resource files be included in final bundle?
	 * 
	 * @parameter default-value="true"
	 */
	protected boolean isIncludeGeneratedDescritors;

	/**
	 * collection of names of unwanted maven project packaging types for which
	 * to skip invocation of this plugin; by defaul includes "pom";
	 * 
	 * @parameter
	 */
	protected Set<String> improperPackaging = new HashSet<String>();
	{
		improperPackaging.add("pom");
	}

	// ####################################################
	// ####################################################
	// ####################################################

	protected boolean isImproperPackaging() {

		final String packaging = project.getPackaging();

		if (improperPackaging.contains(packaging)) {
			getLog().info(
					"execution is improper for project packaging '" + packaging
							+ "'; ignoring plugin invocation");
			return true;
		}

		return false;

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

}
