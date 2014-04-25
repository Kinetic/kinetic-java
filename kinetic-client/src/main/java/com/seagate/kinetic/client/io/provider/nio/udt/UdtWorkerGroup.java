/**
 * Copyright (C) 2014 Seagate Technology.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
