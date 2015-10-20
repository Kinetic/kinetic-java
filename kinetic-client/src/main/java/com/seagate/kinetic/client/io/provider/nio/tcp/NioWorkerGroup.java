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

