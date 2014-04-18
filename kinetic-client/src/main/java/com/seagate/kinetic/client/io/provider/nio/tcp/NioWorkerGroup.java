/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.client.io.provider.nio.tcp;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ThreadFactory;

import com.seagate.kinetic.client.io.provider.nio.NioClientThreadFactory;
import com.seagate.kinetic.client.io.provider.nio.udt.UdtWorkerGroup;

/**
 * 
 * Resource sharing manager for the client nio service.
 * 
 * @author chiaming
 * 
 */
public class NioWorkerGroup {

	// worker group
	private static EventLoopGroup workerGroup = null;

	// nio thread factory
	private static ThreadFactory tfactory = null;

	static {
		tfactory = new NioClientThreadFactory("kinetic.client.nio");
		workerGroup = new NioEventLoopGroup(0, tfactory);
	}

	/**
	 * Get worker group for nio service.
	 * 
	 * @return EventLoopGroup worker group for nio.
	 */
	public static synchronized EventLoopGroup getWorkerGroup() {
		return workerGroup;
	}

	/**
	 * reduce reference count and do close if reference count is 0.
	 */
	public static synchronized void close() {
		;
	}

	/**
	 * shutdown workers.
	 */
	public static void shutdown() {

		try {
			// shutdown workers
			workerGroup.shutdownGracefully();
			workerGroup.terminationFuture().await(50);

		} catch (Exception e) {
			e.printStackTrace();
		}

		UdtWorkerGroup.shutdown();

	}

}

