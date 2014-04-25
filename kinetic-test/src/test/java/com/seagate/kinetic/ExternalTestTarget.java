/**
 * 
 * Copyright (C) 2014 Seagate Technology.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package com.seagate.kinetic;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import kinetic.client.KineticException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;

public class ExternalTestTarget extends AbstractIntegrationTestTarget {

	private static final Logger logger = LoggerFactory
			.getLogger(ExternalTestTarget.class);

	public ExternalTestTarget(boolean clearExistingDatabase, String host,
			int port, int tlsPort) throws JSchException, InterruptedException,
			UnsupportedEncodingException, ExecutionException, KineticException {
		super(host, port, tlsPort);

		if (clearExistingDatabase) {
			if (Boolean.getBoolean("FAST_CLEAN_UP")) {
				clearDatabaseUsingSSH(host);
			} else {
				performISE();
			}
		}
	}

	// Cache SSH sessions to avoid the overhead of setting up and tearing down
	// SSH connections to the target
	// before and after every test as part of the erase process
	@SuppressWarnings("unused")
	private static final LoadingCache<String, Session> sessionCache = CacheBuilder
			.<String, Session> newBuilder().build(
					new CacheLoader<String, Session>() {
						@Override
						public Session load(String host) throws Exception {
							JSch jsch = new JSch();
							Session session = jsch.getSession("root", host);
							session.setPassword("leafcutter");
							session.setConfig("StrictHostKeyChecking", "no");
							session.connect();

							return session;
						}
					});

	private void clearDatabaseUsingSSH(String host) throws JSchException,
			UnsupportedEncodingException, ExecutionException {
		ChannelExec channel = (ChannelExec) sessionCache.get(host).openChannel(
				"exec");
		channel.setCommand("rm -rf /mnt/store/* && killall kineticd");
		channel.setInputStream(null);

		ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
		ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();

		channel.setOutputStream(stdoutStream);
		channel.setErrStream(stderrStream);
		channel.connect();

		while (!channel.isClosed()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw Throwables.propagate(e);
			}
		}

		logger.info(
				"Erase command finished with status={} stdout=<{}> stderr=<{}>",
				channel.getExitStatus(),
				stdoutStream.toString(Charsets.UTF_8.name()),
				stderrStream.toString(Charsets.UTF_8.name()));

		if (channel.getExitStatus() != 0) {
			throw new RuntimeException("Unable to erase target");
		}

		channel.disconnect();
	}

	@Override
	public void shutdown() throws Exception {

	}
}
