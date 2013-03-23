/**
 * Copyright (C) 2010-2013 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.scr;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Generate component descriptors form annotated java classes.
 * 
 * @goal generate
 * 
 * @phase process-classes
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class CarrotOsgiScrGenerate extends CarrotOsgiScr {

	/**
	 * Empty DS descriptor included in plugin jar.
	 */
	protected static final String NULL_XML = "null.xml";

	/**
	 * Progress counter.
	 */
	private int allclassesCounter;

	/**
	 * Progress counter.
	 */
	private int descriptorCounter;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoFailureException {
		try {

			logInfo("generate");
			logInfo("incremental: " + isContextIncremental());

			if (!isProperPackaging()) {
				logInfo("skip for packaging=" + project.getPackaging());
				return;
			}

			logDebug("");
			logDebug("excludedServices");
			for (final String service : excludedServices) {
				logDebug("\t service=" + service);
			}

			logDebug("");
			logDebug("properPackaging");
			for (final String packaging : properPackaging) {
				logDebug("\t packaging=" + packaging);
			}

			//

			descriptorCounter = 0;
			allclassesCounter = 0;

			final long timeStart = System.nanoTime();

			if (!isProcessMainClasses && !isProcessTestClasses) {
				logError("you have not selected neither main nor test classes");
			}

			if (isProcessMainClasses) {
				processClassFolder(ClassesSelector.COMPILE);
			}

			if (isProcessTestClasses) {
				processClassFolder(ClassesSelector.TESTING);
			}

			if (isIncludeEmptyDescriptor) {
				includeEmptyDescriptor();
			}

			if (isIncludeGeneratedDescritors) {
				if (isContextFull()) {
					includeDescriptorResource();
				} else {
					/** Do not register resource on incremental build. */
				}
			}

			final long timeFinish = System.nanoTime();

			//

			logDebug("");

			final long timeDiff = timeFinish - timeStart;
			final long timeRate = descriptorCounter == 0 ? 0 : timeDiff
					/ descriptorCounter;
			logDebug("combined classes count = " + allclassesCounter);
			logDebug("descriptor class count = " + descriptorCounter);
			logDebug("time, millis total     = " + timeDiff / 1000 / 1000);
			logDebug("rate, millis per descr = " + timeRate / 1000 / 1000);

		} catch (final Throwable exception) {
			throw new MojoFailureException("bada-boom", exception);
		}
	}

	/**
	 * Attach descriptor resource to the final jar.
	 */
	protected void includeDescriptorResource() {

		final Resource resource = new Resource();

		final String sourcePath = outputDirectorySCR().getPath();
		final String targetPath = targetDirectorySCR;

		resource.setDirectory(sourcePath);
		resource.setTargetPath(targetPath);

		logDebug("");
		logDebug("including descriptor resource = " + resource);

		project.addResource(resource);

	}

	/**
	 * Attach empty place holder DS component descriptor to the final jar.
	 */
	protected void includeEmptyDescriptor() throws MojoFailureException {

		final URL source = getClass().getResource(NULL_XML);

		final File target = new File(outputDirectorySCR(), NULL_XML);

		try {
			FileUtils.copyURLToFile(source, target);
		} catch (final Exception e) {
			throw new MojoFailureException("can not get " + NULL_XML, e);
		}

		logDebug("");
		logDebug("including empty descriptor = " + target);

	}

	/**
	 * Generate extended class loader.
	 * 
	 * @return class loader that will include both project and plug-in
	 *         dependencies
	 **/
	protected ClassLoader makeClassLoader(final ClassesSelector selector)
			throws Exception {

		final List<String> pathList = selector.getClasspathElements(project);

		final URL[] entryUrlArray = new URL[pathList.size()];

		int index = 0;
		for (final String path : pathList) {
			final URL entryURL = new File(path).toURI().toURL();
			logDebug("\t dependency = " + entryURL);
			entryUrlArray[index++] = entryURL;
		}

		/** Maven plug-in class loader. */
		final ClassLoader parentLoader = Thread.currentThread()
				.getContextClassLoader();

		/** Combo class path loader for a selector. */
		final URLClassLoader customLoader = new URLClassLoader(entryUrlArray,
				parentLoader);

		return customLoader;

	}

	/**
	 * Generate full java class name.
	 * 
	 * @return java class FQN
	 */
	protected String makeClassName(final File classesDirectory,
			final File classFile) {

		final URI folderURI = classesDirectory.toURI();
		final URI fileURI = classFile.toURI();

		final String path = folderURI.relativize(fileURI).getPath();

		/**
		 * Cut out file extension and convert to java class FQN.
		 * <p>
		 * from: com/carrotgarden/test/TestComp.class
		 * <p>
		 * into: com.carrotgarden.test.TestComp
		 */

		final int index = path.lastIndexOf(".");

		final String name = path.substring(0, index).replace("/", ".");

		return name;

	}

	/**
	 * Generate DS descriptor file name.
	 */
	protected String outputFileSCR(final String name) {
		return name + "." + outputExtensionSCR;
	}

	/**
	 * Generate DS component descriptors for given class path type.
	 */
	protected void processClassFolder(final ClassesSelector selector)
			throws Throwable {

		final Pattern excludePattern = Pattern.compile(excludeFileNameRegex);

		final File classesDirectory = selector.getClassesDirectory(this);

		MojoUtil.ensureFolder(classesDirectory);

		logDebug("");
		logDebug("input classes = " + classesDirectory);

		/** Collect all class files. */
		final Iterator<File> iter = processIterator(classesDirectory);

		final ClassLoader loader = makeClassLoader(selector);

		logDebug("");
		logDebug("output directory = " + outputDirectorySCR());

		while (iter.hasNext()) {

			/** Discovered *.class file. */
			final File file = iter.next();

			// logDebug("\t file : " + file);

			/** Ignore excluded files. */
			if (excludePattern.matcher(file.getName()).matches()) {
				continue;
			}

			/** Resolved class name. */
			final String name = makeClassName(classesDirectory, file);

			// logDebug("\t class : " + name);

			/** Make individual descriptor. */
			final String text = maker().make(loader, name);

			/** Non components returns null. */
			final boolean isComponent = text != null;

			allclassesCounter++;

			if (isComponent) {

				final String outputFile = outputFileSCR(name);

				logDebug("\t descriptor = " + outputFile);

				saveDescriptor(name, text);

				descriptorCounter++;

			} else {

				logDebug("\t class is not a component: " + name);

			}

		}

		logInfo("");

		if (descriptorCounter == 0) {
			logInfo("did not find any active scr components.");
		} else {
			logInfo("active components count = " + descriptorCounter);
		}

	}

	/**
	 * Find changed class files in class folder.
	 */
	protected Iterator<File> processIterator(final File folder) {
		return contextIterator(folder, contextChanged(folder, "**/*.class"));
	}

	/**
	 * Save generated DS descriptor and report changes to Eclipse.
	 * <p>
	 * Descriptor file name convention:
	 * <p>
	 * from: com.carrotgarden.test.TestComp
	 * <p>
	 * into: com.carrotgarden.test.TestComp.xml
	 */
	protected void saveDescriptor(final String name, final String text)
			throws Exception {

		final File file = new File(outputDirectorySCR(), outputFileSCR(name));

		FileUtils.writeStringToFile(file, text);

		contextRefresh(file);

	}

}
