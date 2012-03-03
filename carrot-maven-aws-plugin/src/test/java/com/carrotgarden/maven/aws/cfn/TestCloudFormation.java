/**
 * Copyright (C) 2010 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.cfn;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCloudFormation {

	Logger logger = LoggerFactory.getLogger(TestCloudFormation.class);

	// @Test
	public void testStack() throws Exception {

		final File templateFile = new File(
				"./src/test/resources/ec2-builder.template");

		final String stackName = "builder";
		final String stackTemplate = FileUtils.readFileToString(templateFile);

		final Map<String, String> stackParams = new HashMap<String, String>();

		final long timeout = 6 * 60; // seconds

		/** admin-cfn */
		final String awsAccessKey = "";
		final String awsSecretKey = "";

		//

		final CloudFormation formation = new CloudFormation(logger, stackName,
				stackTemplate, stackParams, timeout, awsAccessKey, awsSecretKey);

		formation.stackCreate();

		assertTrue(true);

		formation.stackDelete();

	}
}
