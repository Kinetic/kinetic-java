/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.client.io.provider.nio.udt;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.udt.nio.NioUdtProvider;

import java.util.concurrent.ThreadFactory;

import com.seagate.kinetic.client.io.provider.nio.NioClientThreadFactory;

/**
 * 
 * Please note: This class is for evaluation only and in prototype state.
 * 
 * @author chiaming
 * 
 */
public class UdtWorkerGroup {

	private static EventLoopGroup workerGroup = null;

	private static ThreadFactory tfactory = null;

	public static EventLoopGroup getWorkerGroup() {

		if (workerGroup == null) {
			initWorkerGroup();
		}

		return workerGroup;
	}

	public static synchronized void initWorkerGroup() {

		if (workerGroup == null) {
			tfactory = new NioClientThreadFactory("kinetic.client.nio.udt");

			workerGroup = new NioEventLoopGroup(0, tfactory,
					NioUdtProvider.MESSAGE_PROVIDER);
		}
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
			if (workerGroup != null) {
				workerGroup.shutdownGracefully();
				workerGroup.terminationFuture().await(50);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
