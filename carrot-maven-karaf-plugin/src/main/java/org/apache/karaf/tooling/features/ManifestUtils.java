/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package org.apache.karaf.tooling.features;

import java.util.LinkedList;
import java.util.List;
import java.util.jar.Manifest;

import org.apache.felix.utils.manifest.Clause;
import org.apache.felix.utils.manifest.Parser;
import org.apache.felix.utils.version.VersionRange;
import org.osgi.framework.Constants;


/**
 * A set of utility methods to ease working with {@link org.apache.felix.utils.manifest.Parser} and
 * {@link org.apache.felix.utils.manifest.Clause}
 */

public class ManifestUtils {

    private ManifestUtils() {
        // hide the constructor
    }

    /**
     * Get the list of imports from the manifest.  If no imports have been defined, this method returns an empty list.
     *
     * @param manifest the manifest
     * @return the list of imports
     */
    public static List<Clause> getImports(Manifest manifest) {
    	List<Clause> result = new LinkedList<Clause>();
    	Clause[] clauses = Parser.parseHeader(getHeader(Constants.IMPORT_PACKAGE, manifest));
    	for (Clause clause : clauses) {
    		result.add(clause);
    	}
    	return result;
    }

    /**
     * Get the list of non-optional imports from the manifest.
     *
     * @param manifest the manifest
     * @return the list of non-optional imports
     */
    public static List<Clause> getMandatoryImports(Manifest manifest) {
        List<Clause> result = new LinkedList<Clause>();
        for (Clause clause : getImports(manifest)) {
            if (!isOptional(clause)) {
                result.add(clause);
            }
        }
        return result;
    }

    /**
     * Get the list of exports from the manifest.  If no exports have been defined, this method returns an empty list.
     *
     * @param manifest the manifest
     * @return the list of exports
     */
    public static List<Clause> getExports(Manifest manifest) {
    	List<Clause> result = new LinkedList<Clause>();
    	Clause[] clauses = Parser.parseHeader(getHeader(Constants.EXPORT_PACKAGE, manifest));
    	for (Clause clause : clauses) {
    		result.add(clause);
    	}
    	return result;
    }

    /**
     * Check if a given manifest clause represents an optional import
     *
     * @param clause the manifest clause
     * @return <code>true</code> for an optional import, <code>false</code> for mandatory imports
     */
    public static boolean isOptional(Clause clause) {
        return "optional".equals(clause.getDirective("resolution"));
    }

    /**
     * Check if the manifest contains the mandatory Bundle-Symbolic-Name
     *
     * @param manifest the manifest
     * @return <code>true</code> if the manifest specifies a Bundle-Symbolic-Name
     */
    public static boolean isBundle(Manifest manifest) {
        return getBsn(manifest) != null;
    }

    public static boolean matches(Clause requirement, Clause export) {
        if (requirement.getName().equals(export.getName())) {
        	VersionRange importVersionRange = getVersionRange(requirement); 
        	VersionRange exportVersionRange = getVersionRange(export);
        	VersionRange intersection = importVersionRange.intersect(exportVersionRange);
        	return intersection != null;
        }
        return false;
    }
    
    public static String getHeader(String name, Manifest manifest) {
    	String value = manifest.getMainAttributes().getValue(name);
    	return value;    	
    }
    
    public static String getBsn(Manifest manifest) {
    	String bsn = getHeader(Constants.BUNDLE_SYMBOLICNAME, manifest);
        return bsn;
    }
    
    public static VersionRange getVersionRange(Clause clause)
    {
        String v = clause.getAttribute(Constants.VERSION_ATTRIBUTE);
        if (v == null)
        {
            v = clause.getAttribute(Constants.PACKAGE_SPECIFICATION_VERSION);
        }
        if (v == null)
        {
            v = clause.getAttribute(Constants.BUNDLE_VERSION_ATTRIBUTE);
        }
        return VersionRange.parseVersionRange(v);
    }
}
