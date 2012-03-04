/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package org.apache.karaf.tooling.features;

import org.junit.Test;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import static org.apache.karaf.tooling.features.MavenUtil.aetherToMvn;
import static org.apache.karaf.tooling.features.MavenUtil.artifactToMvn;
import static org.apache.karaf.tooling.features.MavenUtil.mvnToAether;
import static org.apache.karaf.tooling.features.MavenUtil.pathFromAether;
import static org.apache.karaf.tooling.features.MavenUtil.pathFromMaven;
import static org.junit.Assert.assertEquals;

/**
 * @version $Rev: 1243427 $ $Date: 2012-02-13 02:11:08 -0600 (Mon, 13 Feb 2012) $
 */
public class MavenUtilTest {

    @Test
    public void testMvnToAether() throws Exception {
        assertEquals("org.foo:org.foo.bar:1.0-SNAPSHOT", mvnToAether("mvn:org.foo/org.foo.bar/1.0-SNAPSHOT"));
        assertEquals("org.foo:org.foo.bar:kar:1.0-SNAPSHOT", mvnToAether("mvn:org.foo/org.foo.bar/1.0-SNAPSHOT/kar"));
        assertEquals("org.foo:org.foo.bar:xml:features:1.0-SNAPSHOT", mvnToAether("mvn:org.foo/org.foo.bar/1.0-SNAPSHOT/xml/features"));
    }

    @Test
    public void testAetherToMvn() throws Exception {
        assertEquals("mvn:org.foo/org.foo.bar/1.0-SNAPSHOT", aetherToMvn("org.foo:org.foo.bar:1.0-SNAPSHOT"));
        assertEquals("mvn:org.foo/org.foo.bar/1.0-SNAPSHOT/kar", aetherToMvn("org.foo:org.foo.bar:kar:1.0-SNAPSHOT"));
        assertEquals("mvn:org.foo/org.foo.bar/1.0-SNAPSHOT/xml/features", aetherToMvn("org.foo:org.foo.bar:xml:features:1.0-SNAPSHOT"));
    }

    @Test
    public void testPathFromMvn() throws Exception {
        assertEquals("org/foo/org.foo.bar/1.0-SNAPSHOT/org.foo.bar-1.0-SNAPSHOT.jar", pathFromMaven("mvn:org.foo/org.foo.bar/1.0-SNAPSHOT"));
        assertEquals("org/foo/org.foo.bar/1.0-SNAPSHOT/org.foo.bar-1.0-SNAPSHOT.kar", pathFromMaven("mvn:org.foo/org.foo.bar/1.0-SNAPSHOT/kar"));
        assertEquals("org/foo/org.foo.bar/1.0-SNAPSHOT/org.foo.bar-1.0-SNAPSHOT-features.xml", pathFromMaven("mvn:org.foo/org.foo.bar/1.0-SNAPSHOT/xml/features"));
    }

    @Test
    public void testPathFromAether() throws Exception {
        assertEquals("org/foo/org.foo.bar/1.0-SNAPSHOT/org.foo.bar-1.0-SNAPSHOT.jar", pathFromAether("org.foo:org.foo.bar:1.0-SNAPSHOT"));
        assertEquals("org/foo/org.foo.bar/1.0-SNAPSHOT/org.foo.bar-1.0-SNAPSHOT.kar", pathFromAether("org.foo:org.foo.bar:kar:1.0-SNAPSHOT"));
        assertEquals("org/foo/org.foo.bar/1.0-SNAPSHOT/org.foo.bar-1.0-SNAPSHOT-features.xml", pathFromAether("org.foo:org.foo.bar:xml:features:1.0-SNAPSHOT"));
    }

    @Test
    public void testArtifactToMvn() throws Exception {
        assertEquals("mvn:org.foo/org.foo.bar/1.0-SNAPSHOT", artifactToMvn(new DefaultArtifact("org.foo:org.foo.bar:1.0-SNAPSHOT")));
        assertEquals("mvn:org.foo/org.foo.bar/1.0-SNAPSHOT/kar", artifactToMvn(new DefaultArtifact("org.foo:org.foo.bar:kar:1.0-SNAPSHOT")));
        assertEquals("mvn:org.foo/org.foo.bar/1.0-SNAPSHOT/xml/features", artifactToMvn(new DefaultArtifact("org.foo:org.foo.bar:xml:features:1.0-SNAPSHOT")));
    }

}
