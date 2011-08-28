package com.carrotgarden.maven.imgscalr;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.thebuzzmedia.imgscalr.Scalr;
import com.thebuzzmedia.imgscalr.Scalr.Method;
import com.thebuzzmedia.imgscalr.Scalr.Mode;
import com.thebuzzmedia.imgscalr.Scalr.Rotation;

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

			BufferedImage source = ImageIO.read(this.source);

			BufferedImage target = Scalr.resize(source, Method.QUALITY,
					Mode.AUTOMATIC, Rotation.NONE, width, height);

			String format = getFileExtension(this.target);

			this.target.getParentFile().mkdirs();

			ImageIO.write(target, format, this.target);

		} catch (Throwable exception) {
			throw new MojoExecutionException("imgscalr: scale failed",
					exception);
		}

	}

	protected String getFileExtension(File file) {
		String name = file.getName();
		int pos = name.lastIndexOf('.');
		String extension = name.substring(pos + 1);
		return extension;
	}

}
