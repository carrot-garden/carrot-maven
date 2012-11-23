/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.impl.MavenLoggerFactory;

/**
 */
public abstract class CarrotAws extends AbstractMojo {

	/** do not use during class init */
	protected Logger getLogger(final Class<?> klaz) {
		return MavenLoggerFactory.getLogger(klaz, getLog());
	}

	/**
	 * The Maven Session *
	 * 
	 * @required
	 * @readonly
	 * @parameter expression="${session}"
	 */
	private MavenSession session;

	protected MavenSession session() {
		return session;
	}

	/**
	 * @readonly
	 * @required
	 * @parameter expression="${project}"
	 */
	private MavenProject project;

	protected MavenProject project() {
		return project;
	}

	/**
	 * @readonly
	 * @required
	 * @parameter expression="${settings}"
	 */
	private Settings settings;

	protected Settings settings() {
		return settings;
	}

	/**
	 * AWS
	 * 
	 * <a href= "http://docs.amazonwebservices.com/general/latest/gr/rande.html"
	 * >region name, such as us-east-1,</a>
	 * 
	 * which controls amazon region selection;
	 * 
	 * @required
	 * @parameter default-value="us-east-1"
	 */
	private String amazonRegion;

	/**
	 * name of project.property which, if set dynamically, will be used instead
	 * of plugin property {@link #amazonRegion}
	 * 
	 * @parameter
	 */
	private String amazonRegionProperty;

	/** prefer project.properies over plug-in property */
	protected String amazonRegion() {
		if (amazonRegionProperty == null) {
			return amazonRegion;
		} else {
			return (String) project().getProperties().get(amazonRegionProperty);
		}
	}

}
