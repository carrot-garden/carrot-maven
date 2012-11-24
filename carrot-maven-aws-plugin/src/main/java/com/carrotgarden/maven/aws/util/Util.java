/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.jackson.map.ObjectMapper;

public class Util {

	public static void assertNotNull(final Object instance, final String message) {
		if (instance == null) {
			throw new IllegalStateException(message);
	
		}
	}

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

	public static <T> T jsonLoad(final File file, final Class<T> klaz)
			throws Exception {

		final ObjectMapper mapper = new ObjectMapper();

		return mapper.readValue(file, klaz);

	}

	public static void overrideInstanceProps(final Log log,
			final Object instance, final String prefix,
			final Map<String, String> props) {

		final Set<Map.Entry<String, String>> entrySet = //
		new HashSet<Entry<String, String>>(props.entrySet());

		final Class<?> klaz = instance.getClass();

		for (final Map.Entry<String, String> entry : entrySet) {

			final String key = entry.getKey();
			final String value = entry.getValue();

			if (!key.startsWith(prefix)) {
				continue;
			}

			props.remove(key);

			try {

				final Field field = findField(klaz, key);

				field.set(instance, value);

				log.info("override : " + key + "=" + value);

			} catch (final Exception e) {

				log.warn("override : invalid stack param=" + key, e);

			}

		}

	}

	public static Properties propsLoad(final Log log, final File file)
			throws Exception {

		final Properties props = new Properties();

		if (file == null || !file.exists()) {
			log.debug("file == null || !file.exists()");
			return props;
		}

		final Reader reader = new FileReader(file);

		props.load(reader);

		reader.close();

		return props;

	}

	public static void propsReport(final Log log, final String title,
			final Properties props) {

		log.info("properties : " + title);

		final Object[] keyArray = props.keySet().toArray();

		Arrays.sort(keyArray);

		for (final Object key : keyArray) {
			log.info("\t" + key + "=" + props.get(key));
		}

	}

	public static void propsSave(final Log log, final Properties props,
			final File file) throws Exception {

		if (props == null || file == null) {
			log.debug("props == null || file == null");
			return;
		}

		final File folder = file.getParentFile();

		if (!folder.exists()) {
			folder.mkdirs();
		}

		final Writer writer = new FileWriter(file);

		props.store(writer, null);

	}

	public static Map<String, String> safeMap(final Map<String, String> props) {

		final Map<String, String> map = new TreeMap<String, String>();

		if (props == null) {
			return map;
		}

		return props;

	}

	public static Map<String, String> safeMap(final Properties props) {

		final Map<String, String> map = new TreeMap<String, String>();

		if (props == null) {
			return map;
		}

		for (final Map.Entry<Object, Object> entry : props.entrySet()) {

			final String key = entry.getKey().toString();
			final String value = entry.getValue().toString();

			map.put(key, value);

		}

		return map;

	}

	public static long safeNumber(final Log log, final String numberText,
			final long numberDefault) {
		try {
			return Long.parseLong(numberText);
		} catch (final Throwable e) {
			log.warn("using numberDefault=" + numberDefault);
			return numberDefault;
		}
	}

	public static String stringNullOrValue(final Object object) {
		return object == null ? null : object.toString();
	}

}
