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
