package com.carrotgarden.maven.aws.dns;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * route53:
 * 
 * <b><a href= "http://invalid" >ensure cname record</a></b>
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
	 * name of the maven property that will contain dns name list
	 * 
	 * @required
	 * @parameter
	 */
	protected String dnsSource;

	/**
	 * name of the maven property that will contain dns name list
	 * 
	 * @required
	 * @parameter
	 */
	protected String dnsTarget;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {

			getLog().info("dns cname init [" + "]");

			final Route53 route53 = getRoute53();

			route53.ensureCNAME(dnsSource, dnsTarget);

			getLog().info("dns cname done [" + "]");

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}

}
