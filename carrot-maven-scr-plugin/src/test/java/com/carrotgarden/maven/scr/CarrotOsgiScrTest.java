/**
 * Copyright (C) 2010-2013 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.scr;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Test;

public class CarrotOsgiScrTest {

	@Test
	public void testRegex() throws Exception {

		String regex = ".*-.*";

		Pattern pattern = Pattern.compile(regex);

		assertTrue(pattern.matcher("package-info.class").matches());
		
		assertFalse(pattern.matcher("PackageInfo.class").matches());

	}

}
