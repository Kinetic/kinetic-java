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
package com.seagate.kinetic.simulator.io.provider.nio;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

/**
 * 
 * Nio Event Loop Group management class.
 * 
 * 
 * @author chiaming
 * 
 */
public class NioEventLoopGroupManager {

	public final Logger logger = Logger
			.getLogger(NioEventLoopGroupManager.class.getName());

	// service config
	private final SimulatorConfiguration config;

	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;

	private boolean isClosed = false;

	// threads for boss
	private NioThreadFactory bossTreadFactory = null;

	// threads for workers
	private NioThreadFactory workerThreadFactory = null;

	public NioEventLoopGroupManager(SimulatorConfiguration config) {

		// simulator config
		this.config = config;

		// boss threads
		int nBossThreads = config.getNioServiceBossThreads();

		// worker threads
		int nWorkerThreads = config.getNioServiceWorkerThreads();

		// boss thread factory
		this.bossTreadFactory = new NioThreadFactory("Simulator-nio-boss", true);

		// worker thread factory
		this.workerThreadFactory = new NioThreadFactory("Simulator-nio-worker",
				true);

		logger.info("Nio event loop threads = " + nBossThreads);

		// construct boss group
		bossGroup = new NioEventLoopGroup(nBossThreads, bossTreadFactory);

		// construct worker group
		workerGroup = new NioEventLoopGroup(nWorkerThreads, workerThreadFactory);
	}

	/**
	 * Get boss group
	 * 
	 * @return boss group
	 */
	public EventLoopGroup getBossGroup() {

		if (this.isClosed) {
			throw new java.lang.IllegalStateException(
					"nio event loop is closed");
		}

		return this.bossGroup;
	}

	/**
	 * Get worker group.
	 * 
	 * @return worker group.
	 * 
	 */
	public EventLoopGroup getWorkerGroup() {

		if (this.isClosed) {
			throw new java.lang.IllegalStateException(
					"nio event loop is closed");
		}

		return this.workerGroup;
	}

	/**
	 * Close boss and worker groups. This shuts down the Nio Services.
	 * 
	 */
	public synchronized void close() {

		if (this.isClosed) {
			return;
		}

		try {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();

			long awaitTimeout = this.config.getThreadPoolAwaitTimeout();

			bossGroup.terminationFuture().await(awaitTimeout);
			workerGroup.terminationFuture().await(awaitTimeout);

		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}

		this.isClosed = true;

		logger.info("NioEventLoopGroupManager nio service closed ...");
	}

}
