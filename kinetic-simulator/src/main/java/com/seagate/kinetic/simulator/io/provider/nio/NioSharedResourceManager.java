/**
 * Copyright 2013-2015 Seagate Technology LLC.
 *
 * This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at
 * https://mozilla.org/MP:/2.0/.
 * 
 * This program is distributed in the hope that it will be useful,
 * but is provided AS-IS, WITHOUT ANY WARRANTY; including without 
 * the implied warranty of MERCHANTABILITY, NON-INFRINGEMENT or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the Mozilla Public 
 * License for more details.
 *
 * See www.openkinetic.org for more project information
 */
package com.seagate.kinetic.simulator.io.provider.nio;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * Nio Event Loop Group management class.
 * 
 * 
 * @author chiaming
 * 
 */
public class NioSharedResourceManager {

	public final static Logger logger = Logger
			.getLogger(NioSharedResourceManager.class.getName());

	private static EventLoopGroup bossGroup = null;
	private static EventLoopGroup workerGroup = null;

	private static boolean isClosed = false;

	// threads for boss
	private static NioThreadFactory bossTreadFactory = null;

	// threads for workers
	private static NioThreadFactory workerThreadFactory = null;

	static {
		// boss thread factory
		bossTreadFactory = new NioThreadFactory("Simulator-nio-boss", true);

		// worker thread factory
		workerThreadFactory = new NioThreadFactory("Simulator-nio-worker", true);

		// construct boss group
		bossGroup = new NioEventLoopGroup(0, bossTreadFactory);

		// construct worker group
		workerGroup = new NioEventLoopGroup(0, workerThreadFactory);
	}

	/**
	 * Get boss group
	 * 
	 * @return boss group
	 */
	public static EventLoopGroup getBossGroup() {
		return bossGroup;
	}

	/**
	 * Get worker group.
	 * 
	 * @return worker group.
	 * 
	 */
	public static EventLoopGroup getWorkerGroup() {
		return workerGroup;
	}

	/**
	 * Close boss and worker groups. This shuts down the Nio Services.
	 * 
	 */
	public static synchronized void close() {

		if (isClosed) {
			return;
		}

		try {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();

			long awaitTimeout = 20;

			bossGroup.terminationFuture().await(awaitTimeout);
			workerGroup.terminationFuture().await(awaitTimeout);

		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}

		isClosed = true;

		logger.info("NioEventLoopGroupManager nio service closed ...");
	}

}
