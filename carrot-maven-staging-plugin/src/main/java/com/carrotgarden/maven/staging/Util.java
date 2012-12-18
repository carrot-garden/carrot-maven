package com.carrotgarden.maven.staging;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.aether.artifact.Artifact;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

public class Util {

	public static String artifactName(final Artifact artifact) {

		final StringBuilder text = new StringBuilder();

		text.append(artifact.getArtifactId());
		text.append("-");

		text.append(artifact.getVersion());
		text.append("-");

		if (Util.hasClassifier(artifact)) {
			text.append(artifact.getClassifier());
		}

		text.append("-");
		text.append(artifact.getExtension());

		return text.toString();
	}

	public static boolean hasClassifier(final Artifact artifact) {
		final String classifier = artifact.getClassifier();
		return classifier != null && classifier.length() != 0;
	}

	public static String artifactFile(final File folder, final Artifact artifact) {

		final File file = new File(folder, artifactName(artifact));

		return file.getAbsolutePath();

	}

	public static Element artifactItem(final File folder,
			final Artifact artifact) {
	
		final String destFileName = artifactFile(folder, artifact);
	
		return element("artifactItem", //
				element("groupId", artifact.getGroupId()), //
				element("artifactId", artifact.getArtifactId()), //
				element("version", artifact.getVersion()), //
				element("classifier", artifact.getClassifier()), //
				element("type", artifact.getExtension()), //
				element("destFileName", destFileName) //
		);
	
	}

	public static Element artifactItemList(final File folder,
			final List<Artifact> artifactList) {
	
		final List<Element> elementList = new ArrayList<Element>();
	
		for (final Artifact artifact : artifactList) {
	
			final Element element = artifactItem(folder, artifact);
	
			elementList.add(element);
	
		}
	
		final Element[] elementArray = elementList.toArray(new Element[0]);
	
		return element("artifactItems", elementArray);
	
	}

}
