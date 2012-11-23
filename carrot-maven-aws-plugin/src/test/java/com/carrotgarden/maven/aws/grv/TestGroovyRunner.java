package com.carrotgarden.maven.aws.grv;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

public class TestGroovyRunner {

	@Test
	public void testFile() throws Exception {

		final Properties properties = new Properties();
		properties.put("prop-key", "prop-value-1");

		assertEquals(properties.get("prop-key"), "prop-value-1");

		final MavenProject project = mock(MavenProject.class);

		when(project.getProperties()).thenReturn(properties);

		final GroovyRunner runner = new GroovyRunner(project);

		final File script = new File("./src/test/resources/script.groovy");

		final Object result = runner.execute(script);

		assertEquals(result, "result");

		assertEquals(properties.get("prop-key"), "prop-value-2");

	}

	@Test
	public void testString() throws Exception {

		final Properties properties = new Properties();
		properties.put("prop-key", "prop-value-1");

		assertEquals(properties.get("prop-key"), "prop-value-1");

		final MavenProject project = mock(MavenProject.class);

		when(project.getProperties()).thenReturn(properties);

		final GroovyRunner runner = new GroovyRunner(project);

		final File file = new File("./src/test/resources/script.groovy");

		final String script = FileUtils.readFileToString(file);

		final Object result = runner.execute(script);

		assertEquals(result, "result");

		assertEquals(properties.get("prop-key"), "prop-value-2");

	}

}
