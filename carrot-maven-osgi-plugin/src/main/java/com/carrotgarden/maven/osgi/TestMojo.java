package com.carrotgarden.maven.osgi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal test
 * 
 */
public class TestMojo extends AbstractMojo {

	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	@Override
	public void execute() throws MojoExecutionException {

		File f = outputDirectory;

		if (!f.exists()) {
			f.mkdirs();
		}

		File touch = new File(f, "touch." + new Date().getTime() + ".txt");

		FileWriter w = null;

		try {

			w = new FileWriter(touch);

			w.write("touch.txt : " + new Date());

		} catch (IOException e) {

			throw new MojoExecutionException("Error creating file " + touch, e);

		} finally {

			if (w != null) {
				try {
					w.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

	}

}
