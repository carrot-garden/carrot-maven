package com.carrotgarden.m2e.config;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class FileUtil {

	// private static Logger log = LoggerFactory.getLogger(FileUtil.class);

	private static final Charset UTF_8 = Charset.forName("UTF-8");

	public static <T> String readTextResource(final Class<?> klaz,
			final String resource) throws Exception {

		final StringBuilder text = new StringBuilder(8 * 1024);

		final InputStream stream = klaz.getResourceAsStream(resource);

		final BufferedInputStream input = new BufferedInputStream(stream);

		final byte[] array = new byte[1024];

		while (true) {
			final int count = input.read(array);
			if (count < 0) {
				break;
			}
			text.append(new String(array, 0, count));
		}

		input.close();

		return text.toString();

	}

	public static String readTextFile(final File file) throws Exception {

		final StringBuilder text = new StringBuilder(8 * 1024);

		final InputStream stream = new FileInputStream(file);

		final BufferedInputStream input = new BufferedInputStream(stream);

		final InputStreamReader reader = new InputStreamReader(input, UTF_8);

		final char[] array = new char[1024];

		while (true) {
			final int count = reader.read(array);
			if (count < 0) {
				break;
			}
			text.append(new String(array, 0, count));
		}

		reader.close();

		return text.toString();

	}

	public static void writeTextFile(final File file, final String text)
			throws Exception {

		final OutputStream stream = new FileOutputStream(file);

		final BufferedOutputStream output = new BufferedOutputStream(stream);

		final OutputStreamWriter writer = new OutputStreamWriter(output, UTF_8);

		writer.write(text);

		writer.flush();

		writer.close();

	}

}
