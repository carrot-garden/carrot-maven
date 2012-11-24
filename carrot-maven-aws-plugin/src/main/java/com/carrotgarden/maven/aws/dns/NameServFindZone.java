/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.dns;

import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.amazonaws.services.route53.model.HostedZone;

/**
 * route53:
 * 
 * <b><a href=
 * "http://docs.amazonwebservices.com/Route53/latest/DeveloperGuide/ListInfoOnHostedZone.html"
 * >find dns zone name from dns host name</a></b>
 * 
 * @goal route53-find-zone
 * 
 * @phase prepare-package
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class NameServFindZone extends NameServ {

	/**
	 * dns host name which should be resolved into dns zone name
	 * 
	 * @required
	 * @parameter default-value="default.example.com"
	 */
	private String dnsHostName;

	/**
	 * name of the maven project.property that will contain dns zone name after
	 * execution of this maven goal
	 * 
	 * @required
	 * @parameter default-value="dnsZoneName"
	 */
	private String dnsResultProperty;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {

			getLog().info("dns find init [" + dnsHostName + "]");

			final CarrotRoute53 route53 = newRoute53();

			final HostedZone zone = route53.findZone( //
					route53.canonical(dnsHostName));

			final Properties props = project().getProperties();

			final String zoneName;

			if (zone == null) {
				zoneName = null;
				props.remove(dnsResultProperty);
			} else {
				zoneName = zone.getName();
				props.put(dnsResultProperty, zoneName);
			}

			getLog().info("dns zone name : " + zoneName);

			getLog().info("dns find done [" + dnsHostName + "]");

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}

}
