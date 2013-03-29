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
	 * Empty DS descriptor included in the plugin jar.
	 */
	protected static final String NULL_XML = "null.xml";

	/**
	 * Progress counter for all classes.
	 */
	private int allclassesCounter;

	/**
	 * Progress counter for DS component classes.
	 */
	private int descriptorCounter;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoFailureException {
		try {

			contextMessageClear(pomFile());

			logInfo("generate");
			logInfo("incremental: " + isContextIncremental());

			if (!isProperPackaging()) {
				logInfo("skip for packaging=" + project.getPackaging());
				return;
			}

			final File folder = outputDirectorySCR();
			if (!folder.exists()) {
				logDebug("");
				if (folder.mkdirs()) {
					logDebug("folder created : " + folder);
				} else {
					logError("failed to create folder : " + folder);
				}
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
				includeDescriptorResource();
			}

			final long timeFinish = System.nanoTime();

			logDebug("");

			final long timeDiff = timeFinish - timeStart;
			final long timeRate = descriptorCounter == 0 ? 0 : timeDiff
					/ descriptorCounter;
			logDebug("combined classes count = " + allclassesCounter);
			logDebug("descriptor class count = " + descriptorCounter);
			logDebug("time, millis total     = " + timeDiff / 1000 / 1000);
			logDebug("rate, millis per descr = " + timeRate / 1000 / 1000);

		} catch (final Throwable e) {
			final String message = "generate failure: " + e;
			logError(message);
			contextMessageError(pomFile(), message, e);
			throw new MojoFailureException("bada-boom", e);
		}
	}

	/**
	 * Check if resource with given target path is present in the list.
	 */
	protected boolean hasResource(final Resource resource,
			final List<Resource> resourceList) {
		for (final Resource existing : resourceList) {
			if (resource.getTargetPath().equals(existing.getTargetPath())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Attach DS descriptor resource folder to the final jar.
	 */
	protected void includeDescriptorResource() {

		if (isContextIncremental()) {
			logDebug("do not include descriptor resource for incremental build");
			return;
		}

		final String sourcePath = outputDirectorySCR().getPath();
		final String targetPath = targetDirectorySCR;

		final Resource resource = new Resource();
		resource.setDirectory(sourcePath);
		resource.setTargetPath(targetPath);

		final List<Resource> resourceList = project.getResources();

		logDebug("");
		if (hasResource(resource, resourceList)) {
			logDebug("use existing descriptor resource = " + resource);
			return;
		} else {
			logDebug("include created descriptor resource = " + resource);
			resourceList.add(resource);
		}

	}

	/**
	 * Attach empty place holder DS component descriptor to the final jar.
	 */
	protected void includeEmptyDescriptor() throws Exception {

		if (isContextIncremental()) {
			logDebug("skip including empty descriptor for incremental build");
			return;
		}

		final URL source = getClass().getResource(NULL_XML);

		final File target = absolute(new File(outputDirectorySCR(), NULL_XML));

		logDebug("");
		logDebug("including empty descriptor = " + target);

		FileUtils.copyURLToFile(source, target);

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
			final URL entryURL = absolute(path).toURI().toURL();
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

		final URI folderURI = absolute(classesDirectory).toURI();
		final URI fileURI = absolute(classFile).toURI();

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

		final File classesDirectory = selector.getClassesDirectory(this);

		logDebug("");
		if (!classesDirectory.exists()) {
			logDebug("skip for missing classes directory");
			return;
		} else {
			logDebug("input classes = " + classesDirectory);
		}

		/** Collect all class files. */
		final Iterator<File> iter = processIterator(classesDirectory);

		if (!iter.hasNext()) {
			logDebug("");
			logDebug("skip for no changes in classes directory");
			return;
		}

		final ClassLoader loader = makeClassLoader(selector);

		logDebug("");
		logDebug("output directory = " + outputDirectorySCR());

		final Pattern excludePattern = Pattern.compile(excludeFileNameRegex);

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

			/** Non components return null. */
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

		FileUtils.writeStringToFile(absolute(file), text);

		contextRefresh(file);

	}

}
