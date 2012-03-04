/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package org.apache.karaf.tooling.features;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.apache.karaf.tooling.utils.MojoSupport;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.handler.manager.DefaultArtifactHandlerManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

/**
 * @version $Rev: 1212423 $ $Date: 2011-12-09 07:54:06 -0600 (Fri, 09 Dec 2011) $
 */
public class BundleToArtifactTest extends MojoSupport {

    public BundleToArtifactTest() throws NoSuchFieldException, IllegalAccessException {
        factory = new DefaultArtifactFactory();
        ArtifactHandlerManager artifactHandlerManager = new DefaultArtifactHandlerManager();
        Field f = factory.getClass().getDeclaredField("artifactHandlerManager");
        f.setAccessible(true);
        f.set(factory, artifactHandlerManager);
        f.setAccessible(false);

        f = artifactHandlerManager.getClass().getDeclaredField("artifactHandlers");
        f.setAccessible(true);
        f.set(artifactHandlerManager, new HashMap());
        f.setAccessible(false);
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
    }
    
    @Test
    public void testSimpleURL() throws Exception {
        Artifact artifact = resourceToArtifact("mvn:org.foo/bar/1.0/kar", false);
        assert artifact.getGroupId().equals("org.foo");
        assert artifact.getArtifactId().equals("bar");
        assert artifact.getBaseVersion().equals("1.0");
        assert artifact.getType().equals("kar");
        assert artifact.getRepository() == null;
        assert artifact.getClassifier() == null;
    }

    @Test
    public void testURLWithClassifier() throws Exception {
        Artifact artifact = resourceToArtifact("mvn:org.foo/bar/1.0/kar/type", false);
        assert artifact.getGroupId().equals("org.foo");
        assert artifact.getArtifactId().equals("bar");
        assert artifact.getBaseVersion().equals("1.0");
        assert artifact.getType().equals("kar");
        assert artifact.getRepository() == null;
        assert artifact.getClassifier().equals("type");
    }

    @Test
    public void testRemoteRepoURL() throws Exception {
        Artifact artifact = resourceToArtifact("mvn:http://baz.com!org.foo/bar/1.0/kar", false);
        assert artifact.getGroupId().equals("org.foo");
        assert artifact.getArtifactId().equals("bar");
        assert artifact.getBaseVersion().equals("1.0");
        assert artifact.getType().equals("kar");
        assert artifact.getRepository().getUrl().equals("http://baz.com");
        assert artifact.getClassifier() == null;
    }
}
