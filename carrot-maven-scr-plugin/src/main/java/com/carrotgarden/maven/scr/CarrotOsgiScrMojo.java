package com.carrotgarden.maven.scr;

/**
 */

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.carrotgarden.osgi.anno.scr.make.Maker;

/**
 * @description make scr descriptors form annotated java classes
 * 
 * @goal scr
 * 
 * @phase process-classes
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class CarrotOsgiScrMojo extends AbstractMojo {

	/**
	 * internal
	 * 
	 * @required
	 * @parameter expression="${project}"
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * location of generated scr component descriptor files in final bundle
	 * 
	 * @parameter default-value= "OSGI-INF/service-component"
	 * 
	 */
	protected String targetDirectorySCR;

	/**
	 * location of generated scr component descriptor files in maven build
	 * folder
	 * 
	 * @required
	 * @parameter default-value= "${project.build.directory}/service-component"
	 */
	protected File outputDirectorySCR;

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
	 * @parameter default-value="${project.build.outputDirectory}"
	 * @required
	 */
	protected File outputMainClasses;

	/**
	 * location of compiled "test" class files
	 * 
	 * @parameter default-value="${project.build.testOutputDirectory}"
	 * @required
	 */
	protected File outputTestClasses;

	/**
	 * optional collection of names of unwanted component service interfaces
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
	 * should generated descriptor resource files be included in final bundle?
	 * 
	 * @parameter default-value="true"
	 */
	protected boolean isIncludeGeneratedDescritors;

	//

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

	//

	/** java class extension during class discovery */
	protected static final String[] EXTENSIONS = new String[] { "class" };

	/** find classes from all packages during class discovery */
	protected static final boolean IS_RECURSIVE = true;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoFailureException {

		getLog().info("");

		if (!isProcessMainClasses && !isProcessTestClasses) {
			getLog().error(
					"you have not selected neither main nor test classes");
			return;
		}

		if (isProcessMainClasses) {
			processClassesDirectory(ClassesSelector.COMPILE);
		}

		if (isProcessTestClasses) {
			processClassesDirectory(ClassesSelector.TESTING);
		}

		if (isIncludeGeneratedDescritors) {
			includeDescriptorResource();
		}

	}

	protected void processClassesDirectory(final ClassesSelector selector)
			throws MojoFailureException {

		try {

			final File classesDirectory = selector.getClassesDirectory(this);

			if (isValidDirectory(classesDirectory)) {
				getLog().info("");
				getLog().info(
						"processing classes directory = " + classesDirectory);
			} else {
				throw new MojoFailureException("classes directory invalid");
			}

			/** collect all class files */
			@SuppressWarnings("unchecked")
			final Iterator<File> iter = FileUtils.iterateFiles(
					classesDirectory, EXTENSIONS, IS_RECURSIVE);

			final ClassLoader loader = getClassloader(selector);

			getLog().info("");
			getLog().info("output descriptor directory = " + outputDirectorySCR);

			int classCount = 0;

			while (iter.hasNext()) {

				/** discovered *.class file */
				final File file = iter.next();

				/** resolved class name */
				final String name = getClassName(classesDirectory, file);

				/** load and resolve class and all super classes */
				final Class<?> klaz = Class.forName(name, true, loader);

				/** make individual descriptor */
				final String text = getMaker().make(klaz);

				/** non component returns null */
				final boolean isComponent = text != null;

				if (isComponent) {

					saveDescriptor(klaz, text);

					classCount++;

				}

			}

			getLog().info("");

			if (classCount == 0) {
				getLog().warn("did not find any active scr components");
			} else {
				getLog().info("active components count = " + classCount);
			}

		} catch (final MojoFailureException exception) {

			throw exception;

		} catch (final Throwable exception) {

			throw new MojoFailureException("execution failure", exception);

		}

	}

	/**
	 * 
	 * descriptor file name convention:
	 * 
	 * from: com.carrotgarden.test.TestComp
	 * 
	 * into: com.carrotgarden.test.TestComp.xml
	 * 
	 */
	protected void saveDescriptor(final Class<?> klaz, final String text)
			throws Exception {

		final String name = klaz.getName() + "." + outputExtensionSCR;

		final File file = new File(outputDirectorySCR, name);

		getLog().info("\t descriptor : " + file);

		FileUtils.writeStringToFile(file, text);

	}

	/**
	 * @return java class FQN
	 */
	protected String getClassName(final File classesDirectory,
			final File classFile) {

		final URI folderURI = classesDirectory.toURI();
		final URI fileURI = classFile.toURI();

		final String path = folderURI.relativize(fileURI).getPath();

		/**
		 * cut out file extension and convert to java class FQN
		 * 
		 * from: com/carrotgarden/test/TestComp.class
		 * 
		 * into: com.carrotgarden.test.TestComp
		 * 
		 */

		final int index = path.lastIndexOf(".");

		final String name = path.substring(0, index).replace("/", ".");

		return name;

	}

	/**
	 * @return class loader that will include both project and plugin
	 *         dependencies
	 **/
	protected ClassLoader getClassloader(final ClassesSelector selector)
			throws Exception {

		final List<String> pathList = selector.getClasspathElements(project);

		final URL[] entryUrlArray = new URL[pathList.size()];

		int index = 0;
		for (final String path : pathList) {
			final URL entryURL = new File(path).toURI().toURL();
			getLog().info("\t found class path entry = " + entryURL);
			entryUrlArray[index++] = entryURL;
		}

		/** maven plugin class loader */
		final ClassLoader TCCL = Thread.currentThread().getContextClassLoader();

		/** class path loader for a selector */
		final URLClassLoader loader = new URLClassLoader(entryUrlArray, TCCL);

		return loader;

	}

	protected enum ClassesSelector {

		COMPILE() {

			@SuppressWarnings("unchecked")
			@Override
			public List<String> getClasspathElements(final MavenProject project)
					throws DependencyResolutionRequiredException {
				return project.getCompileClasspathElements();
			}

			@Override
			public File getClassesDirectory(final CarrotOsgiScrMojo mojo) {
				return mojo.outputMainClasses;
			}

		},

		TESTING() {

			@SuppressWarnings("unchecked")
			@Override
			public List<String> getClasspathElements(final MavenProject project)
					throws DependencyResolutionRequiredException {
				return project.getTestClasspathElements();
			}

			@Override
			public File getClassesDirectory(final CarrotOsgiScrMojo mojo) {
				return mojo.outputTestClasses;
			}

		},

		;

		public abstract List<String> getClasspathElements(MavenProject project)
				throws DependencyResolutionRequiredException;

		public abstract File getClassesDirectory(CarrotOsgiScrMojo mojo);

	}

	protected void includeDescriptorResource() {

		final Resource resource = new Resource();

		final String sourcePath = outputDirectorySCR.getPath();
		final String targetPath = targetDirectorySCR;

		resource.setDirectory(sourcePath);
		resource.setTargetPath(targetPath);

		getLog().info("");
		getLog().info("including descriptor resource = " + resource);

		project.addResource(resource);

	}

}
