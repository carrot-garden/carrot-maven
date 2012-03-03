/**
 * Copyright (C) 2010 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.ssh;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

public class PathMaker {

	public static class Entry {

		public final String source;
		public final String target;

		public Entry(final String source, final String target) {
			this.source = source;
			this.target = target;
		}

	}

	//

	private final Logger logger;

	private final String source;
	private final String target;

	public PathMaker(final Logger logger, final String source,
			final String target) {

		this.logger = logger;

		this.source = source;
		this.target = target;

	}

	private List<String> makeList(final String source) {

		final File sourceDir = new File(source);

		if (!sourceDir.exists()) {
			throw new IllegalArgumentException("source does not exist");
		}

		if (!sourceDir.isDirectory()) {
			throw new IllegalArgumentException("source is not a directory");
		}

		final File[] entryArray = sourceDir.listFiles();

		final List<String> fileList = new ArrayList<String>();

		for (final File entry : entryArray) {

			final String path = entry.getAbsolutePath();

			if (entry.isDirectory()) {
				fileList.addAll(makeList(path));
			} else {
				fileList.add(path);
			}

		}

		return fileList;

	}

	public List<Entry> getEntryList() {

		final List<String> sourceList = makeList(source);

		final List<Entry> entryList = new ArrayList<Entry>();

		for (final String sourceFile : sourceList) {

			final String targetFile = sourceFile.replaceFirst(source, target);

			final Entry entry = new Entry(sourceFile, targetFile);

			entryList.add(entry);

			logger.debug("sourceFile={} targetFile={}", sourceFile, targetFile);

		}

		return entryList;

	}

}
