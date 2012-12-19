/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.staging;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * copy existing released artifacts form private nexus server to sonatype
 * staging server
 * 
 * @inheritByDefault true
 * 
 * @goal sonatype-staging-wagon
 */
public class StagingWagonMojo extends BaseMojo {

	private List<String> artifactList;

	/**
	 * settings.xml credentials entry for source server
	 * 
	 * @parameter default-value="sonatype-nexusm-staging"
	 * @required
	 */
	protected String sourceServerId;

	/**
	 * source artifact server url
	 * 
	 * @parameter default-value=
	 *            "https://oss.sonatype.org/content/groups/public"
	 * @required
	 */
	protected String sourceServerURL;

	/**
	 * TODO
	 * 
	 * @parameter default-value="pom,jar,sources:jar,javadoc:jar"
	 * @required
	 */
	protected String stagingSearchList;

	/**
	 * settings.xml credentials entry for target server
	 * 
	 * @parameter default-value="sonatype-nexus-staging"
	 * @required
	 */
	protected String targetServerId;

	/**
	 * source artifact server url
	 * 
	 * 
	 * @parameter default-value=
	 *            "https://oss.sonatype.org/service/local/staging/deploy/maven2"
	 * @required
	 */
	protected String targetServerURL;

	/**
	 * * provide dependency : http://mojo.codehaus.org/wagon-maven-plugin/
	 * 
	 * @parameter
	 * @required
	 */
	protected Plugin wagonPlugin;

	//

	/**
	 * discover artifact list assuming remote is a nexus server serving index
	 */
	protected List<String> artifactList() throws MojoExecutionException {

		try {

			if (artifactList == null) {

				artifactList = new ArrayList<String>();

				final WebClient webClient = new WebClient();

				final String indexPath = localPath("index.html");

				final Element[] config = new Element[] {
						element("url", sourceServerURL), //
						element("fromFile", remotePath()), //
						element("toFile", indexPath), //
						element("serverId", sourceServerId) //
				};

				executeMojo(wagonPlugin, "download-single",
						configuration(config),
						executionEnvironment(project, session, manager) //
				);

				final String indexURL = "file:" + indexPath;

				final HtmlPage page = webClient.getPage(indexURL);

				@SuppressWarnings("unchecked")
				final List<HtmlAnchor> list = (List<HtmlAnchor>) page
						.getByXPath("//a");

				for (final HtmlAnchor anchor : list) {

					final String artifact = anchor.getTextContent().trim();

					if (artifact.startsWith(artifactPrefix())) {
						artifactList.add(artifact);
					}

				}

			}

			return artifactList;

		} catch (final Throwable e) {

			throw new MojoExecutionException("can not get list", e);

		}

	}

	/**
	 * plugin invocation
	 */
	@Override
	public void execute() throws MojoExecutionException {

		getLog().info("### init");

		executeGet();

		executePut();

		getLog().info("### done");

	}

	/**
	 * fetch from source
	 */
	protected void executeGet() throws MojoExecutionException {

		for (final String artifact : artifactList()) {

			getLog().info("### get " + artifact);

			final Element[] config = new Element[] {
					element("url", sourceServerURL), //
					element("fromFile", remotePath(artifact)), //
					element("toFile", localPath(artifact)), //
					element("serverId", sourceServerId) //
			};

			executeMojo(wagonPlugin, "download-single", configuration(config),
					executionEnvironment(project, session, manager) //
			);

		}

	}

	/**
	 * upload into target
	 */
	protected void executePut() throws MojoExecutionException {

		for (final String artifact : artifactList()) {

			getLog().info("### put " + artifact);

			final Element[] config = new Element[] {
					element("url", targetServerURL), //
					element("fromFile", localPath(artifact)), //
					element("toFile", remotePath(artifact)), //
					element("serverId", targetServerId) //
			};

			executeMojo(wagonPlugin, "upload-single", configuration(config),
					executionEnvironment(project, session, manager) //
			);

		}

	}

}
