/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package org.apache.karaf.tooling.features;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class CustomBundleURLStreamHandlerFactory implements URLStreamHandlerFactory {

	private static final String MVN_URI_PREFIX = "mvn";
	private static final String WRAP_URI_PREFIX = "wrap";
    private static final String FEATURE_URI_PREFIX = "feature";
    private static final String SPRING_URI_PREFIX = "spring";
    private static final String BLUEPRINT_URI_PREFIX = "blueprint";
    private static final String WAR_URI_PREFIX = "war";

    public URLStreamHandler createURLStreamHandler(String protocol) {
		if (protocol.equals(MVN_URI_PREFIX)) {
			return new org.ops4j.pax.url.mvn.Handler();
		} else if (protocol.equals(WRAP_URI_PREFIX)){
			return new org.ops4j.pax.url.wrap.Handler();
		} else if (protocol.equals(FEATURE_URI_PREFIX)){
			return new FeatureURLHandler();
		} else if (protocol.equals(SPRING_URI_PREFIX)){
			return new SpringURLHandler();
		} else if (protocol.equals(BLUEPRINT_URI_PREFIX)){
			return new BlueprintURLHandler();
        } else if (protocol.equals(WAR_URI_PREFIX)) {
            return new WarURLHandler();
		} else {
			return null;
		}
	}

}
