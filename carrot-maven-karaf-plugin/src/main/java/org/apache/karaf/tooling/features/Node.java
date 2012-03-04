/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package org.apache.karaf.tooling.features;

import java.util.Set;
import java.util.HashSet;

import org.apache.maven.artifact.Artifact;

/**
 * @version $Revision: 1086746 $
*/
public class Node {
    private Set children = new HashSet();
    private Set parents = new HashSet();
    private Artifact artifact;

    public Set getChildren() {
        return children;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public Set getParents() {
        return parents;
    }

    public void setChildren(Set children) {
        this.children = children;
    }

    public void setParents(Set parents) {
        this.parents = parents;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }
}
