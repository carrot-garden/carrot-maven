package com.carrotgarden.maven.aws.dns;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.carrotgarden.maven.aws.util.Util;

/**
 * route53:
 * 
 * <b><a href= "http://invalid" >list dns zone entires</a></b>
 * 
 * @goal route53-list-zone
 * 
 * @phase prepare-package
 * 
 * @inheritByDefault true
 * 
 * @requiresDependencyResolution test
 * 
 */
public class CarrotAwsNameServListZone extends CarrotAwsNameServ {

	/**
	 * dns zone name to be listed
	 * 
	 * @required
	 * @parameter default-value="default.example.com"
	 */
	protected String dnsZoneName;

	/**
	 * name of the maven property that will contain dns name list after
	 * execution of this maven goal
	 * 
	 * @required
	 * @parameter default-value="dnsNameList"
	 */
	protected String dnsResultProperty;

	/**
	 * separator to use when building result name list
	 * 
	 * @required
	 * @parameter default-value=";"
	 */
	protected String dnsResultSeparator;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {

			getLog().info("dns list init [" + dnsZoneName + "]");

			final Route53 route53 = newRoute53();

			final List<String> list = route53.listZone(dnsZoneName);

			final String dnsResultNameList = Util.concatenate(list,
					dnsResultSeparator);

			getLog().info("dns name list : " + dnsResultNameList);

			project().getProperties().put(dnsResultProperty, dnsResultNameList);

			getLog().info("dns list done [" + dnsZoneName + "]");

		} catch (final Exception e) {

			throw new MojoFailureException("bada-boom", e);

		}

	}

}
