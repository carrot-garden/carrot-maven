/**
 * Copyright (c) 2004-2011 QOS.ch
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.slf4j.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * An implementation of {@link ILoggerFactory} which always returns
 * {@link MavenLogger} instances.
 * 
 */
public class MavenLoggerFactory implements ILoggerFactory {

	final Map<String, MavenLogger> loggerMap;

	public MavenLoggerFactory() {
		loggerMap = new HashMap<String, MavenLogger>();
	}

	public static Logger getLogger(final Class<?> klaz, final Log log) {
		return getLogger(klaz.getName(), log);
	}

	public static Logger getLogger(final String name, final Log log) {

		final MavenLoggerFactory factory = (MavenLoggerFactory) StaticLoggerBinder
				.getSingleton().getLoggerFactory();

		MavenLogger logger = null;

		synchronized (factory) {

			logger = factory.loggerMap.get(name);

			if (logger == null) {
				logger = new MavenLogger(name, log);
				factory.loggerMap.put(name, logger);
			}

		}

		return logger;

	}

	@Override
	public Logger getLogger(final String name) {
		throw new UnsupportedOperationException(
				"you must use another factory method:"
						+ " getLogger(String name, Log log)");
	}

}
