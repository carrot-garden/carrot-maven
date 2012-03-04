/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package org.apache.karaf.tooling.features;

import static org.apache.karaf.tooling.features.ManifestUtils.matches;

import junit.framework.TestCase;

import java.util.HashMap;

import org.apache.felix.utils.manifest.Attribute;
import org.apache.felix.utils.manifest.Clause;
import org.apache.felix.utils.manifest.Directive;
import org.osgi.framework.Constants;

/**
 * Test cased for {@link org.apache.karaf.tooling.features.ManifestUtils} 
 */
public class ManifestUtilsTest extends TestCase {

    public void testIsOptional() {
    	Directive[] directive = new Directive[0];
    	Attribute[] attribute = new Attribute[0];
        Clause clause = new Clause("org.apache.karaf.test", directive, attribute);
        assertFalse(ManifestUtils.isOptional(clause));

        directive = new Directive[1];
        directive[0] = new Directive("resolution", "mandatory");
        clause = new Clause("org.apache.karaf.test", directive, attribute);
        
        assertFalse(ManifestUtils.isOptional(clause));

        directive[0] = new Directive("resolution", "optional");
        clause = new Clause("org.apache.karaf.test", directive, attribute);
        assertTrue(ManifestUtils.isOptional(clause));
    }

    public void testMatches() {
        assertFalse(matches(clause("org.apache.karaf.dev"), clause("org.apache.karaf.test")));
        assertTrue(matches(clause("org.apache.karaf.test"), clause("org.apache.karaf.test")));

        assertFalse(matches(clause("org.apache.karaf.test", "1.2.0"), clause("org.apache.karaf.test", "[1.1.0, 1.1.0]")));
        assertTrue(matches(clause("org.apache.karaf.test", "1.1.0"), clause("org.apache.karaf.test", "[1.1.0, 1.1.0]")));

        // a single version means >= 1.0.0, so 1.1.O should be a match
        assertTrue(matches(clause("org.apache.karaf.test", "1.0.0"), clause("org.apache.karaf.test", "1.1.0")));
        assertTrue(matches(clause("org.apache.karaf.test", "1.0.0"), clause("org.apache.karaf.test")));

        assertFalse(matches(clause("org.apache.karaf.test", "[1.1.0, 1.2.0)"), clause("org.apache.karaf.test", "[1.0.0, 1.0.0]")));
        assertFalse(matches(clause("org.apache.karaf.test", "[1.1.0, 1.2.0)"), clause("org.apache.karaf.test", "[1.2.0, 1.2.0]")));
        assertTrue(matches(clause("org.apache.karaf.test", "[1.1.0, 1.2.0)"), clause("org.apache.karaf.test", "[1.1.0, 1.1.0]")));
        assertTrue(matches(clause("org.apache.karaf.test", "[1.1.0, 1.2.0)"), clause("org.apache.karaf.test", "[1.1.1, 1.1.1]")));
        assertTrue(matches(clause("org.apache.karaf.test", "[1.1.0, 1.1.0]"), clause("org.apache.karaf.test", "[1.1.0, 1.1.0]")));
        assertFalse(matches(clause("org.apache.karaf.test", "[1.1.0, 1.1.0]"), clause("org.apache.karaf.test", "1.1.1")));
        assertTrue(matches(clause("org.apache.karaf.test", "[1.1.0, 1.1.0]"), clause("org.apache.karaf.test", "1.0.0")));
    }

    private Clause clause(String name) {
        return new Clause(name, new Directive[0], new Attribute[0]);
    }

    private Clause clause(String name, String version) {
    	Attribute[] attribute = {new Attribute(Constants.VERSION_ATTRIBUTE, version)};
        return new Clause(name, new Directive[0], attribute);
    }
}
