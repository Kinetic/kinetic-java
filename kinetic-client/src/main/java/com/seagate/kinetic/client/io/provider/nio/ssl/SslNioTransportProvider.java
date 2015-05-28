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
package com.seagate.kinetic.client.io.provider.nio.ssl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.KineticException;

import com.seagate.kinetic.client.io.provider.nio.tcp.NioWorkerGroup;
import com.seagate.kinetic.client.io.provider.spi.ClientMessageService;
import com.seagate.kinetic.client.io.provider.spi.ClientTransportProvider;
import com.seagate.kinetic.common.lib.KineticMessage;

/**
 *
 * Kinetic TLS/SSL transport provider.
 * <p>
 *
 * @see ClientTransportProvider
 * @see ClientMessageService
 *
 * @author chiaming
 */
public class SslNioTransportProvider implements ClientTransportProvider {

	public final Logger logger = Logger.getLogger(SslNioTransportProvider.class
			.getName());

	private int port = 8443;

	private Bootstrap bootstrap = null;

	private EventLoopGroup workerGroup = null;

	private SslChannelInitializer sslChannelInitializer = null;

	private ClientConfiguration config = null;

	private ClientMessageService mservice = null;

	private Channel channel = null;

	private String host = null;

	public SslNioTransportProvider() {
		;
	}

	private void initTransport() throws KineticException {

		this.port = this.config.getPort();
		this.host = this.config.getHost();

		try {
            workerGroup = NioWorkerGroup.getWorkerGroup();

			sslChannelInitializer = new SslChannelInitializer(this.mservice);

			bootstrap = new Bootstrap();

			bootstrap.group(workerGroup).channel(NioSocketChannel.class)
			.handler(sslChannelInitializer);

			channel = bootstrap.connect(host, port).sync().channel();

		} catch (Exception e) {
			this.close();
			throw new KineticException(e);
		}

		logger.info("Kinetic ssl client transport provider connecting to host:port ="
				+ host
				+ ":"
				+ port);
	}


	@Override
	public void close() {

		try {
			// close message handler
			this.mservice.close();

			// close channel
			if (this.channel != null) {
				this.channel.close();
			}

			// release resources
            // release resources
            NioWorkerGroup.close();

		} catch (Exception e) {

			logger.log(Level.WARNING, e.getMessage(), e);
		}

		logger.info("Kinetic ssl client transport provider closed, url ="
				+ host
				+ ":" + port);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(ClientMessageService mservice)
			throws KineticException {

		this.config = mservice.getConfiguration();
		this.mservice = mservice;

		this.initTransport();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(KineticMessage message) throws IOException {
		this.channel.writeAndFlush(message);
	}

}
