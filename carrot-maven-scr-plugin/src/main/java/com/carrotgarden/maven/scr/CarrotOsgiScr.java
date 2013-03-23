/**
 * Copyright (C) 2010-2013 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.scr;

import static com.carrotgarden.maven.scr.MojoUtil.*;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.Scanner;
import org.sonatype.plexus.build.incremental.BuildContext;

import com.carrotgarden.osgi.anno.scr.make.Maker;

/**
 * Base for Maven goals.
 */
public abstract class CarrotOsgiScr extends AbstractMojo {

	/**
	 * Class selector: compile vs testing class path.
	 */
	protected enum ClassesSelector {

		/**
		 * Main classes.
		 */
		COMPILE() {

			@Override
			public File getClassesDirectory(final CarrotOsgiScr mojo) {
				return absolute(mojo.outputMainClasses);
			}

			@Override
			public List<String> getClasspathElements(final MavenProject project)
					throws DependencyResolutionRequiredException {
				return project.getCompileClasspathElements();
			}

		},

		/**
		 * Test classes.
		 */
		TESTING() {

			@Override
			public File getClassesDirectory(final CarrotOsgiScr mojo) {
				return absolute(mojo.outputTestClasses);
			}

			@Override
			public List<String> getClasspathElements(final MavenProject project)
					throws DependencyResolutionRequiredException {
				return project.getTestClasspathElements();
			}

		},

		;

		/**
		 * Maven build output folder for a selector.
		 */
		public abstract File getClassesDirectory(CarrotOsgiScr mojo);

		/**
		 * List of dependency class path elements for a project.
		 */
		public abstract List<String> getClasspathElements(MavenProject project)
				throws DependencyResolutionRequiredException;

	}

	/**
	 * Prefix to differentiate plugin entries in Eclipse log.
	 */
	protected static final String LOG_PREFIX = "[DS]";

	/**
	 * Eclipse build integration context.
	 * 
	 * @see http://wiki.eclipse.org/M2E_compatible_maven_plugins
	 * 
	 * @component
	 */
	private BuildContext buildContext;

	/**
	 * Enabled DEBUG level logging in eclipse console.
	 * 
	 * @required
	 * @parameter default-value= "false"
	 */
	protected boolean enableDebugLogging;

	/**
	 * Enabled ERROR level logging in eclipse console.
	 * 
	 * @required
	 * @parameter default-value= "true"
	 */
	protected boolean enableErrorLogging;

	/**
	 * Enabled INFO level logging in eclipse console.
	 * 
	 * @required
	 * @parameter default-value= "false"
	 */
	protected boolean enableInfoLogging;

	/**
	 * Collection of names of unwanted component service interfaces.
	 * 
	 * @required
	 * @parameter default-value="java.lang.Cloneable"
	 */
	protected Set<String> excludedServices;

	/**
	 * Exclude java *.class files with this file name regex expression.
	 * 
	 * @required
	 * @parameter default-value= ".*-.*"
	 */
	protected String excludeFileNameRegex;

	/**
	 * Should include an empty component descriptor?
	 * 
	 * @required
	 * @parameter default-value="true"
	 */
	protected boolean isIncludeEmptyDescriptor;

	/**
	 * Should generated descriptor resource files be included in final bundle?
	 * 
	 * @required
	 * @parameter default-value="true"
	 */
	protected boolean isIncludeGeneratedDescritors;

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

	private Maker maker;

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
	 * Current maven pom.xml.
	 * 
	 * @required
	 * @parameter property="project"
	 */
	protected MavenProject project;

	/**
	 * Collection of names of known maven project packaging types for which to
	 * invoke this plug-in, while ignoring all others.
	 * 
	 * @required
	 * @parameter default-value="bundle"
	 */
	protected Set<String> properPackaging;

	/**
	 * Location of generated DS component descriptor files in the final bundle
	 * jar. This is a relative path to jar root or target/classes.
	 * 
	 * @required
	 * @parameter default-value= "OSGI-INF/service-component"
	 */
	protected String targetDirectorySCR;

	/**
	 * Find changed files.
	 * 
	 * @return list of relative paths
	 */
	protected String[] contextChanged(final File folder,
			final String... includes) {
		final boolean ignoreDelta = !buildContext.isIncremental();
		final Scanner scanner = buildContext.newScanner(folder, ignoreDelta);
		scanner.setIncludes(includes);
		scanner.scan();
		return scanner.getIncludedFiles();
	}

	/**
	 * Find deleted files.
	 * 
	 * @return list of relative paths
	 */
	protected String[] contextDeleted(final File folder,
			final String... includes) {
		final Scanner scanner = buildContext.newDeleteScanner(folder);
		scanner.setIncludes(includes);
		scanner.scan();
		return scanner.getIncludedFiles();
	}

	/**
	 * Context file iterator.
	 */
	protected Iterator<File> contextIterator(final File folder,
			final String... relativePathArray) {
		return new Iterator<File>() {

			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < relativePathArray.length;
			}

			@Override
			public File next() throws NoSuchElementException {
				if (hasNext())
					return absolute(new File(absolute(folder),
							relativePathArray[index++]));
				else
					throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};

	}

	/**
	 * Report generated file back to Eclipse.
	 */
	protected void contextRefresh(final File file) {
		buildContext.refresh(absolute(file));
	}

	/**
	 * Clear Eclipse error log entry.
	 */
	protected void contextMessageClear(final File file) {
		buildContext.removeMessages(absolute(file));
	}

	/**
	 * Make entry in Eclipse error log.
	 */
	protected void contextMessageError(final File file, final String message,
			final Throwable cause) {
		buildContext.addMessage(absolute(file), 0, 0, message,
				BuildContext.SEVERITY_ERROR, cause);
	}

	/**
	 * Check if build is full.
	 */
	protected boolean isContextFull() {
		return !buildContext.isIncremental();
	}

	/**
	 * Check if build is incremental.
	 */
	protected boolean isContextIncremental() {
		return buildContext.isIncremental();
	}

	/**
	 * Check if current project packaging is proper for goal execution.
	 */
	protected boolean isProperPackaging() {
		final String packaging = project.getPackaging();
		return properPackaging.contains(packaging);
	}

	/**
	 * Log for plug-in debug level.
	 */
	protected void logDebug(final String text) {
		if (enableDebugLogging) {
			getLog().info(LOG_PREFIX + "[DEBUG] " + text);
		}
	}

	/**
	 * Log for plug-in error level.
	 */
	protected void logError(final String text) {
		if (enableErrorLogging) {
			getLog().info(LOG_PREFIX + "[ERROR] " + text);
		}
	}

	/**
	 * Log for plug-in info level.
	 */
	protected void logInfo(final String text) {
		if (enableInfoLogging) {
			getLog().info(LOG_PREFIX + "[INFO ] " + text);
		}
	}

	/**
	 * Maker of DS component descriptors.
	 */
	protected Maker maker() {
		if (maker == null) {
			maker = new Maker(excludedServices);
		}
		return maker;
	}

	/**
	 * Build output directory, such as
	 * "${basedir}/target/OSGI-INF/service-component".
	 */
	protected File outputDirectorySCR() {
		return absolute(new File(absolute(outputMainClasses),
				targetDirectorySCR));
	}

}
