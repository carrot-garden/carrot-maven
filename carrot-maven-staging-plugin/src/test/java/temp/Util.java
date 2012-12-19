package temp;

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

		if (Util.hasClassifier(artifact)) {
			text.append("-");
			text.append(artifact.getClassifier());
		}

		text.append(".");
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

	/**
	 * http://maven.apache.org/plugins/maven-dependency-plugin/examples/copying-
	 * artifacts.html
	 */
	public static Element dependArtifactItem(final File folder,
			final Artifact artifact) {

		final String outputDirectory = folder.getAbsolutePath();
		final String destFileName = artifactName(artifact);

		return element("artifactItem", //
				//
				element("groupId", artifact.getGroupId()), //
				element("artifactId", artifact.getArtifactId()), //
				element("version", artifact.getVersion()), //
				element("classifier", artifact.getClassifier()), //
				element("type", artifact.getExtension()), //
				//
				element("outputDirectory", outputDirectory), //
				element("destFileName", destFileName) //
		);

	}

	public static Element dependoutputDirectory(final String stagingFolder) {

		return element("outputDirectory", stagingFolder);

	}

	public static Element dependArtifactItemList(final File folder,
			final Artifact artifact) {

		final List<Artifact> artifactList = new ArrayList<Artifact>();
		artifactList.add(artifact);

		return dependArtifactItemList(folder, artifactList);

	}

	public static Element dependArtifactItemList(final File folder,
			final List<Artifact> artifactList) {

		final List<Element> elementList = new ArrayList<Element>();

		for (final Artifact artifact : artifactList) {

			final Element element = dependArtifactItem(folder, artifact);

			elementList.add(element);

		}

		final Element[] elementArray = elementList.toArray(new Element[0]);

		return element("artifactItems", elementArray);

	}

}
