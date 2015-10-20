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
package com.seagate.kinetic.simulator.io.provider.nio.ssl;

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
public class SslNioTransportProvider implements TransportProvider, Runnable {

	public final Logger logger = Logger.getLogger(SslNioTransportProvider.class
			.getName());

	private int port = 8443;

	private ServerBootstrap bootstrap = null;

	private ChannelFuture channelFuture = null;

	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;

	private SslChannelInitializer sslChannelInitializer = null;

	private MessageService service = null;

	public SslNioTransportProvider()
			throws InterruptedException {
	}

	public void doInit() throws InterruptedException {

		// if use ssl as default system property is set, only ssl is running on
		// the TCP port.
		// this is to make it possible to run ssl mode for all unit tests.
		if (this.service.getServiceConfiguration().getUseSslAsDefault()) {
			this.port = this.service.getServiceConfiguration().getPort();
		} else {
			// ssl as a separate service
			this.port = this.service.getServiceConfiguration().getSslPort();
		}

		if (SimulatorConfiguration.getNioResourceSharing()) {
			// resource sharing within the same JVM
			bossGroup = NioSharedResourceManager.getBossGroup();
			workerGroup = NioSharedResourceManager.getWorkerGroup();
		} else {
			bossGroup = this.service.getNioEventLoopGroupManager()
					.getBossGroup();
			workerGroup = this.service.getNioEventLoopGroupManager()
					.getWorkerGroup();
		}

		sslChannelInitializer = new SslChannelInitializer(
				this.service);

		bootstrap = new ServerBootstrap();

		bootstrap.group(bossGroup, workerGroup)
		.channel(NioServerSocketChannel.class)
		.childHandler(sslChannelInitializer);

		logger.info("KineticClient ssl service binding on port =" + port);

		channelFuture = bootstrap.bind(port).sync();
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
		logger.info("KineticClient ssl nio service closed, port =" + port);
	}

	@Override
	public void init(MessageService messageService) {

		// init message service
		this.service = messageService;
	}

	@Override
	public void start() throws IOException {
		// do service init
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
				this.service.getNioEventLoopGroupManager().close();

				channelFuture.channel().closeFuture().sync();
			}

		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
		}

		logger.info("KineticClient ssl nio service stopped, port =" + port);
	}

}
