/**
 * Copyright (C) 2010-2013 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package it;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class VerifyIT {

	private void ensureMatch(String file) throws Exception {

		String sourcePath = "case-01/" + file;

		InputStream sourceInput = VerifyIT.class.getClassLoader()
				.getResourceAsStream(sourcePath);

		String sourceText = IOUtils.toString(sourceInput, "UTF-8");

		System.out.println("source\n" + sourceText);

		String targetPath = "OSGI-INF/service-component/" + file;

		InputStream targetInput = VerifyIT.class.getClassLoader()
				.getResourceAsStream(targetPath);

		String targetText = IOUtils.toString(targetInput, "UTF-8");

		System.out.println("target\n" + targetText);

		assertEquals(sourceText, targetText);

	}

	@Test
	public void descriptors() throws Exception {

		String artifact = System.getProperty("integration.artifact");

		System.out.println("artifact=" + artifact);

		ensureMatch("bench.Comp0.xml");
		ensureMatch("bench.Comp1.xml");
		ensureMatch("bench.Comp2.xml");
		ensureMatch("event.EventConsumer.xml");
		ensureMatch("null.xml");

	}

}
