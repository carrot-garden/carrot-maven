package com.carrotgarden.maven.scr;

/*
 * The MIT License
 *
 * Copyright 2006-2008 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.carrotgarden.osgi.anno.scr.make.Maker;

/**
 * @description make scr descriptors form java classes
 * 
 * 
 * @goal scr
 * 
 * @phase process-classes
 * 
 * @requiresDependencyResolution compile
 * 
 */
public class CarrotMojo extends AbstractMojo {

	private final Maker maker = new Maker();

	/**
	 * POM
	 * 
	 * @parameter expression="${project}"
	 * @readonly
	 * @required
	 */
	protected MavenProject project;

	/**
	 * Location of the file.
	 * 
	 * @parameter default-value="${project.build.outputDirectory}"
	 * @required
	 */
	protected File outputDirectory;

	/**
	 * Location of the file.
	 * 
	 * @parameter default-value="${project.build.testOutputDirectory}"
	 * @required
	 */
	protected File testOutputDirectory;

	static final String[] EXTENSIONS = new String[] { "class" };

	static final boolean RECURSIVE = true;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {

			final ClassLoader loader = getClassloader();

			@SuppressWarnings("unchecked")
			final Iterator<File> iter = FileUtils.iterateFiles(outputDirectory,
					EXTENSIONS, RECURSIVE);

			while (iter.hasNext()) {

				final File file = iter.next();

				final String name = getClassName(file);

				getLog().info("name : " + name);

				// final Class<?> klaz = Class.forName(name, true, loader);

				final Class<?> klaz = loader.loadClass(name);

			}

		} catch (final Throwable exception) {
			final String message = "failed";
			getLog().error(message);
			throw new MojoExecutionException(message, exception);
		}

	}

	String getClassName(final File file) {

		final String path = outputDirectory.toURI().relativize(file.toURI())
				.getPath();

		final int index = path.lastIndexOf(".");

		String name = path.substring(0, index);

		name = name.replace("/", ".");

		return name;

	}

	ClassLoader getClassloader() throws Exception {

		@SuppressWarnings("unchecked")
		final List<String> list = project.getCompileClasspathElements();

		final URL[] urlArray = new URL[list.size()];

		int k = 0;
		for (final String path : list) {
			final URL url = new URL("file://" + path);
			urlArray[k++] = url;
			getLog().info("\t url : " + url);
		}

		final URLClassLoader loader = new URLClassLoader(urlArray);

		return loader;

	}

}
