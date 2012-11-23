/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.dns;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * route53:
 * 
 * <b><a href=
 * "http://docs.amazonwebservices.com/Route53/latest/DeveloperGuide/RRSchanges.html"
 * >ensure cname record</a></b>
 * 
 * @goal route53-ensure-cname
 * 
 * @phase prepare-package
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class CarrotAwsNameServEnsureCNAME extends CarrotAwsNameServ {

	/**
	 * source dns name, or left-hand side of CNAME record
	 * 
	 * @required
	 * @parameter default-value="source.default.example.com"
	 */
	protected String dnsSource;

	/**
	 * target dns name, or right-hand side of CNAME record
	 * 
	 * @required
	 * @parameter default-value="target.default.example.com"
	 */
	protected String dnsTarget;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {

			final String entry = dnsSource + " -> " + dnsTarget;

			getLog().info("dns cname init [" + entry + "]");

			final Route53 route53 = newRoute53();

			route53.ensureCNAME( //
					route53.canonical(dnsSource), route53.canonical(dnsTarget));

			getLog().info("dns cname done [" + entry + "]");

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}

}
