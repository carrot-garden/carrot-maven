/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.batik;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.batik.apps.rasterizer.Main;
import org.apache.batik.apps.rasterizer.SVGConverter;
import org.apache.batik.apps.rasterizer.SVGConverterSource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * 
 * @goal rasterize
 * @phase generate-resources
 * 
 */
public class BatikMojo extends AbstractMojo {

	/**
	 * @parameter
	 * @required
	 */
	protected String arguments;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {

			final String[] args = arguments.split("\\s+");

			getLog().info("batik: args = " + Arrays.toString(args));

			final Main main = new Main(args) {

				@Override
				public boolean proceedOnSourceTranscodingFailure(
						final SVGConverterSource source, final File dest,
						final String errorCode) {

					super.proceedOnSourceTranscodingFailure(source, dest,
							errorCode);

					final String message = "batik: convert failed";
					getLog().error(message);
					throw new RuntimeException(message);

				}

				@Override
				public void validateConverterConfig(final SVGConverter c) {

					@SuppressWarnings("unchecked")
					final List<String> expandedSources = c.getSources();

					if ((expandedSources == null)
							|| (expandedSources.size() < 1)) {

						getLog().info(USAGE);

						final String message = "batik: invalid config";

						getLog().error(message);

						throw new RuntimeException(message);

					}

				}

			};

			main.execute();

		} catch (final Throwable exception) {

			final String help = "http://xmlgraphics.apache.org/batik/tools/rasterizer.html";

			final String message = "batik: help @ " + help;

			getLog().error(message);

			throw new MojoExecutionException("batik: rasterize failed",
					exception);

		}

	}

}
