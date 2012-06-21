/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.scr;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.classworlds.ClassRealm;
import org.codehaus.plexus.component.configurator.AbstractComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.special.ClassRealmConverter;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * A custom ComponentConfigurator which adds the project's runtime classpath
 * elements to the
 * 
 * @author Brian Jackson
 * @since Aug 1, 2008 3:04:17 PM
 * 
 * @plexus.component 
 *                   role="org.codehaus.plexus.component.configurator.ComponentConfigurator"
 *                   role-hint="include-project-dependencies"
 * 
 * @plexus.requirement role=
 *                     "org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup"
 *                     role-hint="default"
 */
public class IncludeProjectDependenciesComponentConfigurator extends
		AbstractComponentConfigurator {

	// private static final Logger LOGGER = Logger
	// .getLogger(IncludeProjectDependenciesComponentConfigurator.class);

	// @Override
	@SuppressWarnings("deprecation")
	public void configureComponent(final Object component,
			final PlexusConfiguration configuration,
			final ExpressionEvaluator expressionEvaluator,
			final ClassRealm containerRealm,
			final ConfigurationListener listener)
			throws ComponentConfigurationException {

		addProjectDependenciesToClassRealm(expressionEvaluator, containerRealm);

		converterLookup.registerConverter(new ClassRealmConverter(
				containerRealm));

		final ObjectWithFieldsConverter converter = new ObjectWithFieldsConverter();

		converter.processConfiguration(converterLookup, component,
				containerRealm.getClassLoader(), configuration,
				expressionEvaluator, listener);
	}

	@SuppressWarnings("unchecked")
	private void addProjectDependenciesToClassRealm(
			final ExpressionEvaluator expressionEvaluator,
			final ClassRealm containerRealm)
			throws ComponentConfigurationException {

		List<String> runtimeClasspathElements;

		try {
			// noinspection unchecked
			runtimeClasspathElements = (List<String>) expressionEvaluator
					.evaluate("${project.runtimeClasspathElements}");
		} catch (final ExpressionEvaluationException e) {
			throw new ComponentConfigurationException(
					"There was a problem evaluating: ${project.runtimeClasspathElements}",
					e);
		}

		// Add the project dependencies to the ClassRealm
		final URL[] urls = buildURLs(runtimeClasspathElements);
		for (final URL url : urls) {
			containerRealm.addConstituent(url);
		}
	}

	private URL[] buildURLs(final List<String> runtimeClasspathElements)
			throws ComponentConfigurationException {

		// Add the projects classes and dependencies
		final List<URL> urls = new ArrayList<URL>(
				runtimeClasspathElements.size());

		for (final String element : runtimeClasspathElements) {

			try {

				final URL url = new File(element).toURI().toURL();

				urls.add(url);

				// if (LOGGER.isDebugEnabled()) {
				// LOGGER.debug("Added to project class loader: " + url);
				// }

			} catch (final MalformedURLException e) {
				throw new ComponentConfigurationException(
						"Unable to access project dependency: " + element, e);
			}
		}

		// Add the plugin's dependencies (so Trove stuff works if Trove isn't on
		return urls.toArray(new URL[urls.size()]);

	}

}
