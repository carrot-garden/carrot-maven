/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.image4j;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import net.sf.image4j.codec.ico.ICOEncoder;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * 
 * @goal multiple
 * @phase generate-resources
 * 
 */
public class Image4jMojoMultiple extends AbstractMojo {

	/**
	 * @parameter
	 * @required
	 */
	protected List<File> sources;

	/**
	 * @parameter
	 * @required
	 */
	protected File target;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {

			getLog().info("image4j : sources = " + sources);
			getLog().info("image4j : target = " + target);

			final List<BufferedImage> sources = new LinkedList<BufferedImage>();

			for (final File file : this.sources) {
				final BufferedImage source = ImageIO.read(file);
				sources.add(source);
			}

			this.target.getParentFile().mkdirs();

			ICOEncoder.write(sources, this.target);

		} catch (final Throwable exception) {

			throw new MojoExecutionException("image4j: convert failed",
					exception);

		}

	}

	protected String getFileExtension(final File file) {

		final String name = file.getName();

		final int pos = name.lastIndexOf('.');

		final String extension = name.substring(pos + 1);

		return extension;

	}

}
