/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package temp;

import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

public class MavenProjectProxy extends MavenProject {

	private final MavenSession session;
	private final MavenProject project;

	public MavenProjectProxy(final MavenSession session,
			final MavenProject project) {

		this.session = session;
		this.project = project;

	}

	@Override
	public Properties getProperties() {

		/** from System.getenv() and System.getProperty() */
		// final Properties propsSystem = session.getSystemProperties();

		/** from maven command line */
		// final Properties propsUser = session.getUserProperties();

		System.err.println("XXXXXXXXXXXXX");

		return project.getProperties();

	}

}
