package com.carrotgarden.maven.aws.ssh;

import java.io.File;
import java.util.Vector;

import org.slf4j.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpProgressMonitor;

public class PathFetcher {

	private final Logger logger;

	public PathFetcher(final Logger logger) {

		this.logger = logger;

	}

	public void fetchFolder( //
			final ChannelSftp channel, //
			final String remoteFolder, //
			final String localFolder, //
			final SftpProgressMonitor monitor //
	) throws Exception {

		final String source = remoteFolder;
		final File target = new File(localFolder);

		fetchFolder(channel, source, target, monitor);

	}

	public void fetchFolder( //
			final ChannelSftp channel, //
			final String remoteFolder, //
			final File localFolder, //
			final SftpProgressMonitor monitor //
	) throws Exception {

		if (!localFolder.exists()) {
			localFolder.mkdirs();
		}

		channel.cd(remoteFolder);

		@SuppressWarnings("unchecked")
		final Vector<ChannelSftp.LsEntry> remoteList = channel.ls(remoteFolder);

		for (int index = 0; index < remoteList.size(); index++) {

			final ChannelSftp.LsEntry remoteEntry = remoteList.elementAt(index);

			final String remotePath = remoteEntry.getFilename();

			if (remoteEntry.getAttrs().isDir()) {

				if (remotePath.equals(".") || remotePath.equals("..")) {
					continue;
				}

				final String nextRemoteFolder = //
				channel.pwd() + "/" + remotePath + "/";

				final File nextLocalFolder = new File(localFolder, remotePath);

				fetchFolder(channel, nextRemoteFolder, nextLocalFolder, monitor);

			} else {

				final File localFile = new File(localFolder, remotePath);

				final String localPath = localFile.getAbsolutePath();

				channel.get(remotePath, localPath, monitor,
						ChannelSftp.OVERWRITE);

			}

		}

		channel.cd("..");

	}

}
