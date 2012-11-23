/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package temp;

import org.apache.maven.project.MavenProject;

public class TestMavenProjectProxy {

	// @Test
	public void setUp() throws Exception {

		final MavenProject instance = new MavenProject();

		final MavenProject instanceProxy = new MavenProjectProxy(null, instance);

		final MavenProject project = ProxyFactory.makeProxy(MavenProject.class,
				instance, instanceProxy);

	}

}
