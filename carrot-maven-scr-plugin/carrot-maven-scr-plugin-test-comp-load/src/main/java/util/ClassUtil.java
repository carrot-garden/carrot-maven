/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassUtil {

	private static Logger log = LoggerFactory.getLogger(ClassUtil.class);

	public static class ClassTrace extends SecurityManager {

		public Class<?> getClassAt(final int index) {
			return getClassContext()[index];
		}

		public String getNameAt(final int index) {
			return getClassContext()[index].getName();
		}

		@Override
		public Class<?>[] getClassContext() {
			return super.getClassContext();
		}

		public void log(final int depth) {
			final Class<?>[] array = getClassContext();
			final int size = Math.min(array.length, depth);
			for (int k = 0; k < size; k++) {
				log.debug("klaz : {} {}", k, array[k].getName());
			}
		}

	}

}
