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
