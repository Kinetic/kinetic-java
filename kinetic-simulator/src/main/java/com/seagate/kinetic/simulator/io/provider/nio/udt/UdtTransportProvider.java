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
package com.seagate.kinetic.simulator.io.provider.nio.udt;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.udt.nio.NioUdtProvider;

import java.io.IOException;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.seagate.kinetic.simulator.io.provider.nio.NioThreadFactory;
import com.seagate.kinetic.simulator.io.provider.spi.MessageService;
import com.seagate.kinetic.simulator.io.provider.spi.TransportProvider;

/**
 * 
 * Please note: This class is for evaluation only and in prototype state.
 * 
 * @author chiaming
 */
public class UdtTransportProvider implements TransportProvider, Runnable {

	public final Logger logger = Logger.getLogger(UdtTransportProvider.class
			.getName());

	private int port = 8123;

	private ServerBootstrap bootstrap = null;

	private ChannelFuture channelFuture = null;

	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;

	private UdtChannelInitializer msChannelInitializer = null;

	private MessageService service = null;

	public UdtTransportProvider()
			throws InterruptedException {
		;
	}

	public void doInit() throws InterruptedException {

		this.port = this.service.getServiceConfiguration().getPort();

		final ThreadFactory acceptFactory = new NioThreadFactory("UdtAccept");

		final ThreadFactory connectFactory = new NioThreadFactory("UdtConnect");

		bossGroup = new NioEventLoopGroup(10, acceptFactory,
				NioUdtProvider.MESSAGE_PROVIDER);

		workerGroup = new NioEventLoopGroup(10, connectFactory,
				NioUdtProvider.MESSAGE_PROVIDER);

		msChannelInitializer = new UdtChannelInitializer(
				this.service);

		bootstrap = new ServerBootstrap();

		bootstrap.group(bossGroup, workerGroup)
		.channelFactory(NioUdtProvider.MESSAGE_ACCEPTOR)
		.option(ChannelOption.SO_BACKLOG, 1000)
		.option(ChannelOption.SO_REUSEADDR, true)
		.childHandler(msChannelInitializer);

		logger.info("Kinetic udt service binding on port =" + port);

		channelFuture = bootstrap.bind(port).sync();

		logger.info("Kinetic udt service bound on port =" + port);
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
		logger.info("udt service closed ...");
	}

	@Override
	public void init(MessageService messageService) {

		this.service = messageService;

	}

	@Override
	public void start() throws IOException {
		try {
			this.doInit();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public void stop() {
		try {

			// stop and release resources
			this.workerGroup.shutdownGracefully();
			this.bossGroup.shutdownGracefully();

			// close channel
			channelFuture.channel().close();

			/**
			 * UDT implementation bug:
			 * https://github.com/netty/netty/issues/1752
			 */
			Thread.sleep(5000);

		} catch (Exception e) {

			logger.log(Level.WARNING, e.getMessage(), e);
		}

		logger.info("Kinetic udt service stopped, port =" + port);
	}

}
