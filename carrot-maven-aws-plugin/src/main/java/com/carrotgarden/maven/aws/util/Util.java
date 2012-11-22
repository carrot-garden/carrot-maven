package com.carrotgarden.maven.aws.util;

import java.lang.reflect.Field;
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

	public static Field findField(final Class<?> klaz, final String fieldName) {

		Class<?> type = klaz;

		do {

			for (final Field field : type.getDeclaredFields()) {

				final String name = field.getName();

				if (!name.equals(fieldName)) {
					continue;
				}

				field.setAccessible(true);

				return field;

			}

			type = type.getSuperclass();

		} while (klaz != null);

		return null;

	}

}
