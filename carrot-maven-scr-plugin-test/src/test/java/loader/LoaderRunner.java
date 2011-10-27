package loader;

import java.net.URL;
import java.net.URLClassLoader;

public class LoaderRunner {

	public static void main(final String[] args) throws Exception {

		// final String path = "./target/classes";
		final String path = "/home/user1/Workspaces/github/carrot-maven/carrot-maven-scr-plugin-test/target/classes";

		final URL url = new URL("file://" + path);

		System.out.println("url : " + url);

		final URL[] urlArray = new URL[] { url };

		final URLClassLoader loader = new URLClassLoader(urlArray);

		final String name = "bench.Comp1";

		final Class<?> klaz = loader.loadClass(name);

		System.out.println("klaz : " + klaz);

	}

}
