/** Do NOT modify or remove this copyright and confidentiality notice!
 *
 * Copyright (c) 2001 - $Date: 2012/06/27 $ Seagate Technology, LLC.
 *
 * The code contained herein is CONFIDENTIAL to Seagate Technology, LLC.
 * Portions are also trade secret. Any use, duplication, derivation, distribution
 * or disclosure of this code, for any reason, not expressly authorized is
 * prohibited. All other rights are expressly reserved by Seagate Technology, LLC.
 */
package com.seagate.kinetic.client.io.provider.nio.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
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
 * This class provides TCP nio transport support for the Kinetic client runtime.
 *
 * @auther James Hughes.
 * @author Chiaming Yang
 */
public class TcpNioTransportProvider implements ClientTransportProvider {

	// logger
	public final Logger logger = Logger.getLogger(TcpNioTransportProvider.class
			.getName());

	// default port
	private int port = 8123;

	private Bootstrap bootstrap = null;

	// private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;

	private NioChannelInitializer nioChannelInitializer = null;

	private ClientConfiguration config = null;

	private ClientMessageService mservice = null;

	private Channel channel = null;

	private String host = null;

	private static ShutdownHook shook = new ShutdownHook();

	static {
		Runtime.getRuntime().addShutdownHook(shook);
	}

	/**
	 * Default constructor.
	 */
	public TcpNioTransportProvider() {
		;
	}

	private void initTransport() throws KineticException {

		this.port = this.config.getPort();
		this.host = this.config.getHost();

		try {

			workerGroup = NioWorkerGroup.getWorkerGroup();

			nioChannelInitializer = new NioChannelInitializer(this.mservice);

			bootstrap = new Bootstrap();

			bootstrap.group(workerGroup).channel(NioSocketChannel.class)
			.handler(nioChannelInitializer);

			channel = bootstrap.connect(host, port).sync().channel();

		} catch (Exception e) {
			// release allocated resources
			this.close();
			throw new KineticException(e);
		}

		logger.info("TcpNio client transport provider connecting to host:port ="
				+ host
				+ ":"
				+ port);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {

		logger.info("closing tcp nio transport provider., host=" + this.host
				+ ", port=" + this.port);

		try {

			// close message handler
			this.mservice.close();

			// close channel
			if (this.channel != null) {
				this.channel.close();
			}

			// release resources
			NioWorkerGroup.close();

		} catch (Exception e) {

			logger.log(Level.WARNING, e.getMessage(), e);
		}

		logger.info("Kinetic nio client transport provider closed, url ="
				+ host
				+ ":" + port);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(ClientMessageService mservice)
			throws KineticException {

		this.mservice = mservice;

		this.config = mservice.getConfiguration();

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
