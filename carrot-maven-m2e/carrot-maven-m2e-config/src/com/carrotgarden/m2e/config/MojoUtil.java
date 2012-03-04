/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.m2e.config;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecution;

class MojoUtil {

	static final String FILE_JAVA_EXTENSION = "java";
	static final String FILE_MAVEN_POM_XML = "pom.xml";

	/**
	 * http://felix.apache.org/site/scr-annotations.html
	 */
	static final String SCR_PACKACKAGE_ANNOTATIONS = "org.apache.felix.scr.annotations";

	/**
	 * http://felix.apache.org/site/apache-felix-maven-scr-plugin.html
	 */
	static final String SCR_GROUP_ID = "org.apache.felix";
	static final String SCR_ARTIFACT_ID = "maven-scr-plugin";

	/**
	 * http://felix.apache.org/site/apache-felix-maven-bundle-plugin-bnd.html
	 */
	static final String BND_GROUP_ID = "org.apache.felix";
	static final String BND_ARTIFACT_ID = "maven-bundle-plugin";

	static boolean isMojoSCR(final MavenContext context) {

		final MojoExecution exec = context.getExecution();

		if (!SCR_GROUP_ID.equals(exec.getGroupId())) {
			return false;
		}

		if (!SCR_ARTIFACT_ID.equals(exec.getArtifactId())) {
			return false;
		}

		return true;

	}

	static boolean isMojoBND(final MavenContext context) {

		final MojoExecution exec = context.getExecution();

		if (!BND_GROUP_ID.equals(exec.getGroupId())) {
			return false;
		}

		if (!BND_ARTIFACT_ID.equals(exec.getArtifactId())) {
			return false;
		}

		return true;

	}

	static boolean isValid(final String text) {
		return text != null && text.length() > 0;
	}

	static boolean isValid(final List<?> list) {
		return list != null && list.size() > 0;
	}

	static boolean isValid(final Object[] array) {
		return array != null && array.length > 0;
	}

	static boolean hasAnnotations(final String text) {
		return text.contains(SCR_PACKACKAGE_ANNOTATIONS);
	}

	static boolean isFileJavaSource(final File file) throws Exception {

		final String name = file.getName();

		if (name.endsWith(FILE_JAVA_EXTENSION)) {
			return true;
		}

		return false;

	}

	static boolean isFileMavenPom(final File file) {

		final String name = file.getName();

		if (FILE_MAVEN_POM_XML.equals(name)) {
			return true;
		}

		return false;
	}

	static boolean isInterestSCR(final File file) throws Exception {

		if (!isFileJavaSource(file)) {
			return false;
		}

		final String text = FileUtil.readTextFile(file);

		if (!hasAnnotations(text)) {
			return false;
		}

		return true;
	}

	static boolean isInterestBND(final File file) throws Exception {

		if (!isFileJavaSource(file) && !isFileMavenPom(file)) {
			return false;
		}

		return true;
	}

	static String join(final List<String> list, final String sepa) {

		final StringBuilder text = new StringBuilder(256);

		final int size = list.size();

		for (int k = 0; k < size; k++) {
			text.append(list.get(k));
			if (k == size - 1) {
				break;
			}
			text.append(sepa);
		}

		return text.toString();

	}

}
