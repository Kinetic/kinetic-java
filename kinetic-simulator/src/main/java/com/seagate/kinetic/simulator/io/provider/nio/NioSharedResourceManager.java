/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
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
