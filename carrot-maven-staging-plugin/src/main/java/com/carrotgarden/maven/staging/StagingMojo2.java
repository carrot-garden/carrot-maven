/*
 * Copyright 2008-2011 Don Brown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.carrotgarden.maven.staging;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

/**
 * 
 * @goal sonatype-staging2
 */
public class StagingMojo2 extends BaseMojo {

	/**
	 * @parameter
	 * @required
	 */
	protected Plugin wagonPlugin;

	/**
	 * @parameter
	 * @required
	 */
	protected Plugin nexusPlugin;

	/**
	 * @parameter default-value="${project.groupId}"
	 * @required
	 */
	protected String stagingGroupId;
	/**
	 * @parameter default-value="${project.artifactId}"
	 * @required
	 */
	protected String stagingArtifactId;

	/**
	 * @parameter default-value="${project.version}"
	 * @required
	 */
	protected String stagingVersion;

	/**
	 * @parameter default-value="jar"
	 * @required
	 */
	protected String stagingExtension;

	/**
	 * @parameter default-value="pom,jar,sources:jar,javadoc:jar"
	 * @required
	 */
	protected String stagingSearchList;

	/**
	 * @parameter default-value="copy"
	 * @required
	 */
	protected String wagonGoal;

	/**
	 * @parameter default-value="staging-close"
	 * @required
	 */
	protected String nexusGoal;

	/**
	 * @parameter default-value="${project.build.directory}/sonatype-staging"
	 * @required
	 */
	protected File stagingFolder;

	/**
	 * @parameter default-value= "https://oss.sonatype.org/"
	 * @required
	 */
	protected String stagingNexusURL;

	/**
	 * @parameter default-value=
	 *            "https://oss.sonatype.org/content/groups/public"
	 * @required
	 */
	protected String sourceServerURL;

	/**
	 * @parameter default-value=
	 *            "https://oss.sonatype.org/service/local/staging/deploy/maven2"
	 * @required
	 */
	protected String targetServerURL;

	/**
	 * @parameter default-value="sonatype-nexus-staging"
	 * @required
	 */
	protected String sourceServerId;

	/**
	 * @parameter default-value="sonatype-nexus-staging"
	 * @required
	 */
	protected String targetServerId;

	//

	protected List<Artifact> resolveArtifactList() {

		final List<Artifact> artifactList = new ArrayList<Artifact>();

		final List<Tuple> tupleList = Tuple.fromList(stagingSearchList);

		for (final Tuple tuple : tupleList) {

			final Artifact artifact = new DefaultArtifact( //
					stagingGroupId, //
					stagingArtifactId, //
					tuple.classifier, //
					tuple.extension, //
					stagingVersion //
			);

			final Artifact result = resolved(artifact);

			if (result == null) {
				continue;
			}

			artifactList.add(result);

		}

		return artifactList;

	}

	protected Artifact stagingPom() {

		final Artifact artifact = new DefaultArtifact( //
				stagingGroupId, //
				stagingArtifactId, //
				null, //
				"pom", //
				stagingVersion //
		);

		return artifact;

	}

	protected Artifact stagingArtifact() {

		final Artifact artifact = new DefaultArtifact( //
				stagingGroupId, //
				stagingArtifactId, //
				"", //
				stagingExtension, //
				stagingVersion //
		);

		return artifact;

	}

	protected void assertStagingPom() throws MojoExecutionException {
		if (isResolved(stagingPom())) {
			return;
		}
		final String message = "" + stagingPom();
		getLog().error(message);
		throw new MojoExecutionException(message);
	}

	@Override
	public void execute() throws MojoExecutionException {

		assertStagingPom();

		getLog().info("### init");

		executeCopy();

		executeNexus();

		getLog().info("### done");

	}

	protected void executeCopy() throws MojoExecutionException {

		getLog().info("### copy");

		final String groupPath = stagingGroupId.replaceAll("\\.", "/");
		final String artifactPath = stagingArtifactId + "/" + stagingVersion;
		final String folderPath = groupPath + "/" + artifactPath;

		final String sourceURL = sourceServerURL + "/" + folderPath;
		final String targetURL = targetServerURL + "/" + folderPath;

		getLog().info("### sourceURL=" + sourceURL);
		getLog().info("### targetURL=" + targetURL);

		final Element[] config = new Element[] {
				//
				element("source", sourceURL), //
				element("target", targetURL), //
				element("sourceId", targetServerId), //
				element("targetId", targetServerId) //
		};

		executeMojo(wagonPlugin, wagonGoal, configuration(config),
				executionEnvironment(project, session, manager) //
		);

	}

	protected void executeGet() throws MojoExecutionException {

		getLog().info("### get");

		final String groupPath = stagingGroupId.replaceAll("\\.", "/");
		final String artifactPath = stagingArtifactId + "/" + stagingVersion;
		final String folderPath = groupPath + "/" + artifactPath;

		final String sourceURL = sourceServerURL + "/" + folderPath;

		getLog().info("### sourceURL=" + sourceURL);

		final Element[] config = new Element[] {
				//
				element("url", sourceURL), //
				element("sourceId", targetServerId), //
				element("targetId", targetServerId) //
		};

		executeMojo(wagonPlugin, wagonGoal, configuration(config),
				executionEnvironment(project, session, manager) //
		);

	}

	protected void executeNexus() throws MojoExecutionException {

		getLog().info("### nexus");

		executeMojo(nexusPlugin, nexusGoal, //

				configuration( //
						//
						element("automatic", "true"), //
						//
						element("groupId", stagingGroupId), //
						element("artifactId", stagingArtifactId), //
						element("version", stagingVersion), //
						//
						element("nexusUrl", stagingNexusURL), //
						element("serverAuthId", targetServerId) //
				), //

				executionEnvironment(project, session, manager) //

		);

	}

}
