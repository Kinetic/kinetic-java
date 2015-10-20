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
package com.seagate.kinetic.simulator.io.provider.nio.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.simulator.SimulatorConfiguration;

import com.seagate.kinetic.simulator.io.provider.nio.NioSharedResourceManager;
import com.seagate.kinetic.simulator.io.provider.spi.MessageService;
import com.seagate.kinetic.simulator.io.provider.spi.TransportProvider;

/**
 * @author chiaming
 */
public class TcpNioTransportProvider implements TransportProvider, Runnable {

	public final Logger logger = Logger.getLogger(TcpNioTransportProvider.class
			.getName());

	private int port = 8123;

	private ServerBootstrap bootstrap = null;

	private ChannelFuture channelFuture = null;

	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;

	private NioChannelInitializer msChannelInitializer = null;

	private MessageService service = null;

	public TcpNioTransportProvider()
			throws InterruptedException {
		;
	}

	public void doInit() throws InterruptedException {

		this.port = this.service.getServiceConfiguration().getPort();

		if (SimulatorConfiguration.getNioResourceSharing()) {
			// resource sharing within the same JVM
			bossGroup = NioSharedResourceManager.getBossGroup();
			workerGroup = NioSharedResourceManager.getWorkerGroup();
		} else {
			// resource usage independent per instance
			bossGroup = this.service.getNioEventLoopGroupManager()
					.getBossGroup();
			workerGroup = this.service.getNioEventLoopGroupManager()
					.getWorkerGroup();
		}

		msChannelInitializer = new NioChannelInitializer(
				this.service);

		bootstrap = new ServerBootstrap();

		bootstrap.group(bossGroup, workerGroup)
		.channel(NioServerSocketChannel.class)
		.childHandler(msChannelInitializer);

		logger.info("Kinetic nio service binding on port =" + port);

		channelFuture = bootstrap.bind(port).sync();

		logger.info("Kinetic nio service bound on port =" + port);
	}

	@Override
	public void run() {

		try {

			channelFuture.channel().closeFuture().sync();

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	@Override
	public void close() {
	    ;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(MessageService messageService) {
		// init service
		this.service = messageService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() throws IOException {
		try {
			this.doInit();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@SuppressWarnings("deprecation")
    @Override
	public void stop() {
		try {

			if (SimulatorConfiguration.getNioResourceSharing()) {
				channelFuture.channel().deregister();
				// close channel only
				channelFuture.channel().close();
			} else {
				// close and release resources
				this.service.getNioEventLoopGroupManager().close();

				// wait for close
				channelFuture.channel().closeFuture().sync();
			}
		} catch (Exception e) {

			logger.log(Level.WARNING, e.getMessage(), e);
		}

		logger.info("Kinetic nio transport service stopped, port =" + port);
	}

}
