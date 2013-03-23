/**
 * Copyright (C) 2010-2013 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package loader;

import java.net.URL;
import java.net.URLClassLoader;

public class LoaderRunner {

	public static void main(final String[] args) throws Exception {

		// final String path = "./target/classes";
		// final String path =
		// "/home/user1/Workspaces/github/carrot-maven/carrot-maven-scr-plugin-test/target/classes";
		final String path = "/home/user1/Temp";

		final URL url = new URL("file://" + path);

		System.out.println("url : " + url);

		final URL[] urlArray = new URL[] { url };

		final URLClassLoader loader = new URLClassLoader(urlArray);

		final String name = "bench.Comp0";

		final Class<?> klaz = loader.loadClass(name);

		System.out.println("klaz : " + klaz);

	}

}
