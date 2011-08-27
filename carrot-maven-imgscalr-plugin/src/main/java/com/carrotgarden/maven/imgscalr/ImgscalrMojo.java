package com.carrotgarden.maven.imgscalr;

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
