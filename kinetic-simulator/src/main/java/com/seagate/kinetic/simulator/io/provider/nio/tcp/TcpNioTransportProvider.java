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
