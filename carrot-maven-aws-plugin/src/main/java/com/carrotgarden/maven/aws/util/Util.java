package com.carrotgarden.maven.aws.util;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

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

	public static <T> T loadJson(final File file, final Class<T> klaz)
			throws Exception {

		final ObjectMapper mapper = new ObjectMapper();

		return mapper.readValue(file, klaz);

	}

	public static String stringNullOrValue(final Object object) {
		return object == null ? null : object.toString();
	}

}
