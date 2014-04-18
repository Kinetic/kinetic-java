/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.client.io.provider.nio.ssl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import kinetic.client.ClientConfiguration;
import kinetic.client.KineticException;

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
			workerGroup = new NioEventLoopGroup();

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
			workerGroup.shutdownGracefully();

			workerGroup.terminationFuture().await(
					this.config.getThreadPoolAwaitTimeout());

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
