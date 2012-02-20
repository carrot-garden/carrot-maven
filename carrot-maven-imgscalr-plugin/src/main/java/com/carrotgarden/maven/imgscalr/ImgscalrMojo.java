package com.carrotgarden.maven.imgscalr;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;

/**
 * 
 * @goal scale
 * @phase generate-resources
 */
public class ImgscalrMojo extends AbstractMojo {

	/**
	 * @parameter
	 * @required
	 */
	protected File source;

	/**
	 * @parameter
	 * @required
	 */
	protected File target;

	/**
	 * @parameter
	 * @required
	 */
	protected int width;

	/**
	 * @parameter
	 * @required
	 */
	protected int height;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {

			getLog().info("imgscalr : source = " + source);
			getLog().info("imgscalr : target = " + target);
			getLog().info("imgscalr : w x h = " + width + " x " + height);

			final BufferedImage source = ImageIO.read(this.source);

			final BufferedImage target = Scalr.resize(source, Method.QUALITY,
					Mode.AUTOMATIC, width, height);

			final String format = getFileExtension(this.target);

			this.target.getParentFile().mkdirs();

			ImageIO.write(target, format, this.target);

		} catch (final Throwable exception) {
			throw new MojoExecutionException("imgscalr: scale failed",
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
