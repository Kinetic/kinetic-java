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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import io.netty.handler.ssl.SslHandler;

import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;

import com.seagate.kinetic.client.io.provider.spi.ClientMessageService;
import com.seagate.kinetic.common.lib.TlsUtil;
import com.seagate.kinetic.common.protocol.codec.KineticDecoder;
import com.seagate.kinetic.common.protocol.codec.KineticEncoder;
//import com.seagate.kinetic.proto.Kinetic;

/**
 *
 * @author chiaming
 *
 */
public class SslChannelInitializer extends
ChannelInitializer<SocketChannel> {

	private static final Logger logger = Logger
			.getLogger(SslChannelInitializer.class.getName());

	private ClientMessageService mservice = null;

	public SslChannelInitializer(ClientMessageService mservice) {
		this.mservice = mservice;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {

		if (mservice.getConfiguration().getConnectTimeoutMillis() > 0) {
			ch.config()
			.setConnectTimeoutMillis(
					mservice.getConfiguration()
					.getConnectTimeoutMillis());
		}

		ChannelPipeline pipeline = ch.pipeline();

		SSLEngine engine = SslContextFactory.getClientContext()
				.createSSLEngine();

		engine.setUseClientMode(true);

		/**
		 * enable TLS V1.x protocols.
		 */
		TlsUtil.enableSupportedProtocols(engine);

		// add ssl handler
		pipeline.addLast("ssl", new SslHandler(engine));

		// decoder
		pipeline.addLast("decoder", new KineticDecoder());
		// encoder
		pipeline.addLast("encoder", new KineticEncoder());
		
		pipeline.addLast("handler", new SslMessageServiceHandler(mservice));

		logger.info("ssl channel initialized ... ");
	}
}
