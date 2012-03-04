/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.carrotgarden.maven.aws.ssh;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.slf4j.Logger;

import com.carrotgarden.maven.aws.ssh.PathMaker.Entry;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;

/**
 * @author Andrei Pozolotin
 */
public class SecureShell {

	private final Logger logger;

	private final File keyFile;

	private final String user;
	private final String host;
	private final int port;

	public SecureShell(final Logger logger, final File keyFile,
			final String user, final String host) {

		this.logger = logger;

		this.keyFile = keyFile;

		this.user = user;
		this.host = host;
		this.port = 22;

	}

	private Session getSession() throws Exception {

		final JSch jsch = new JSch();

		jsch.addIdentity(keyFile.getAbsolutePath());

		final Session session = jsch.getSession(user, host, port);

		session.setConfig("StrictHostKeyChecking", "no");

		return session;

	}

	public int execute(final String command) throws Exception {

		logger.info("exec command: " + command);

		final Session session = getSession();

		session.connect();

		final ChannelExec channel = (ChannelExec) session.openChannel("exec");

		channel.setCommand(command);

		channel.connect();

		//

		final InputStream input = channel.getInputStream();

		final Reader reader = new InputStreamReader(input);

		final BufferedReader buffered = new BufferedReader(reader);

		while (true) {

			final String line = buffered.readLine();

			if (line == null) {
				break;
			}

			logger.info(">>> " + line);

		}

		//

		channel.disconnect();

		final int status = channel.getExitStatus();

		session.disconnect();

		logger.info("exec exit status: " + status);

		return status;

	}

	private String makePath(final String root, final String base) {
		// logger.debug("sftp root={} base={}", root, base);
		if ("/".equals(root)) {
			return "/" + base;
		} else {
			return root + "/" + base;
		}
	}

	private void ensureTargetFolder(final ChannelSftp channel,
			final String folder) throws Exception {

		logger.debug("sftp ensure: " + folder);

		final String[] pathArray = folder.split("/");

		String root = "/";

		for (final String path : pathArray) {

			if (path.length() == 0) {
				continue;
			}

			if (".".equals(path)) {
				continue;
			}

			/** absolute */
			channel.cd(root);

			/** absolute */
			final String next = makePath(root, path);

			boolean isPresent = false;

			try {
				channel.stat(next);
				isPresent = true;
			} catch (final Exception e) {
				isPresent = false;
			}

			if (isPresent) {
				logger.debug("sftp present: " + next);
			} else {
				/** relative to root */
				channel.mkdir(path);
				logger.debug("sftp created: " + next);
			}

			root = next;

		}

	}

	public int publish(final String source, final String target)
			throws Exception {

		logger.info("sftp source: " + source);
		logger.info("sftp target: " + target);

		final Session session = getSession();

		session.connect();

		final ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");

		channel.connect();

		//

		final SftpProgressMonitor monitor = new SftpProgressMonitor() {

			@Override
			public void init(final int op, final String source,
					final String target, final long max) {
				logger.info("sftp upload: " + target);
			}

			@Override
			public boolean count(final long count) {
				logger.debug("sftp bytes: " + count);
				return true;
			}

			@Override
			public void end() {
				logger.debug("sftp done");
			}

		};

		final PathMaker maker = new PathMaker(logger, source, target);

		final List<Entry> entryList = maker.getEntryList();

		ensureTargetFolder(channel, target);

		for (final Entry entry : entryList) {

			final String file = entry.target;
			final int index = file.lastIndexOf("/");
			final String folder = file.substring(0, index);

			ensureTargetFolder(channel, folder);

			channel.put(entry.source, entry.target, monitor,
					ChannelSftp.OVERWRITE);

		}

		//

		channel.disconnect();

		final int status = channel.getExitStatus();

		session.disconnect();

		logger.info("sftp exit status: " + status);

		return status;

	}

}
