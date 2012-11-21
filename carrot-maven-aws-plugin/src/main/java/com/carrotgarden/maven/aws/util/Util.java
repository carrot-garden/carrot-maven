package com.carrotgarden.maven.aws.util;

import java.util.List;

public class Util {

	public static String concatenate(final List<String> list,
			final String separator) {

		final StringBuilder text = new StringBuilder();

		for (final String item : list) {
			text.append(item);
			text.append(separator);
		}

		return text.toString();

	}

}
