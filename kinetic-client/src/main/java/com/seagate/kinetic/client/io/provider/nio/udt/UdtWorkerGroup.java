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
