/**
 * Copyright (C) 2010-2013 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.scr;

import java.io.File;

public class MojoUtil {

	/**
	 * Deletes all files and subdirectories under dir. Returns true if all
	 * deletions were successful. If a deletion fails, the method stops
	 * attempting to delete and returns false.
	 */
	public static boolean deleteDir(final File dir) {

		if (dir.isDirectory()) {

			final String[] children = dir.list();

			for (int i = 0; i < children.length; i++) {

				final boolean success = deleteDir(new File(dir, children[i]));

				if (!success) {
					return false;
				}

			}

		}

		return dir.delete();

	}

	public static void ensureFolder(final File path) {

		if (path == null) {
			throw new IllegalStateException("path == null");
		}

		if (path.exists() && path.isDirectory()) {
			return;
		}

		if (path.exists() && path.isFile()) {
			path.delete();
		}

		if (path.exists() && path.isFile()) {
			throw new IllegalStateException("Can not remove file.");
		}

		path.mkdirs();

		if (path.exists() && path.isDirectory()) {
			return;
		} else {
			throw new IllegalStateException("Can not create folder.");
		}

	}

	/**
	 * Verify directory.
	 */
	public static boolean isValidFolder(final File file) {

		if (file == null) {
			return false;
		}

		if (!file.exists()) {
			return false;
		}

		if (!file.isDirectory()) {
			return false;
		}

		if (!file.canRead()) {
			return false;
		}

		if (!file.canWrite()) {
			return false;
		}

		return true;

	}

	private MojoUtil() {

	}

	/**
	 * Ensure absolute, not relative path.
	 */
	public static File absolute(final File file) {
		if (file.isAbsolute()) {
			return file;
		} else {
			return file.getAbsoluteFile();
		}
	}

	/**
	 * Ensure absolute, not relative path.
	 */
	public static File absolute(final String file) {
		return new File(file).getAbsoluteFile();
	}

}
