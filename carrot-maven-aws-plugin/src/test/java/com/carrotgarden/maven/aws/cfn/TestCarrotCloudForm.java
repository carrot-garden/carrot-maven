/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.cfn;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.settings.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

public class TestCarrotCloudForm {

	Logger logger = LoggerFactory.getLogger(TestCarrotCloudForm.class);

	// @Test
	public void testStack() throws Exception {

		final File templateFile = new File(
				"./src/test/resources/ec2-builder.template");

		final String stackName = "builder";
		final String stackTemplate = FileUtils.readFileToString(templateFile);

		final Map<String, String> stackParams = new HashMap<String, String>();

		final long timeout = 6 * 60; // seconds

		/** admin-cfn */
		final String accessKey = "";
		final String secretKey = "";

		final AWSCredentials credentials = new BasicAWSCredentials(accessKey,
				secretKey);

		//

		final CarrotCloudForm formation = new CarrotCloudForm(logger, stackName,
				stackTemplate, stackParams, timeout, credentials, null, 
				Collections.<String>emptyList(), Collections.<Proxy>emptyList());

		formation.stackCreate();

		assertTrue(true);

		formation.stackDelete();

	}

}
