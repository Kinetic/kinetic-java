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
